import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.ArrayList;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.SignatureException;
import java.security.InvalidAlgorithmParameterException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.InvalidAlgorithmParameterException;



// A client sends messages to the server, the server spawns a thread to communicate with the client.
// Each communication with a client is added to an array list so any message sent gets sent to every other client
// by looping through it.

public class Client {

    // A client has a socket to connect to the server and a reader and writer to receive and send messages respectively.
    private Socket socket;
    private ObjectOutputStream objOutput;
    private ObjectInputStream objInput;
    private String username;
    public RSA keyPair;
    public static ArrayList<ClientInfo> clients = new ArrayList<ClientInfo>();
    private X509Certificate certificate;
    private PublicKey CAPublicKey;


    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.username = username;
            this.objOutput = new ObjectOutputStream(socket.getOutputStream());
            this.objInput = new ObjectInputStream(socket.getInputStream());
            this.keyPair = new RSA();
        } catch (IOException e) {
            // Gracefully close everything.
            closeEverything(socket, objInput, objOutput);
        }
    }

    // Sending a message isn't blocking and can be done without spawning a thread, unlike waiting for a message.
    public void sendMessage()throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,SignatureException,InvalidAlgorithmParameterException {
        try {
            // Initially send the username of the client.
            // Create a scanner for user input.
            Scanner scanner = new Scanner(System.in);
            // While there is still a connection with the server, continue to scan the terminal and then send the message.
            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                for (ClientInfo client : clients) {
                    if (!client.getUsername().equals(this.username)) {
                        // Only messages Client if public key is trused by CA
                        if (client.isTrusted()) {
                            PGPmessages message = PGPmessages.sendMessage(messageToSend,client.getUsername(),this.username,keyPair.getPrivate(),client.getPublicKey());
                            objOutput.writeObject(message);
                            objOutput.flush();
                            System.out.println("Encrypted message sent");
                        }
                        else {
                            System.out.println("LOG: :"+client.getUsername()+"'s Public Key is not valid");
                            System.out.println();
                        }
                        
                    }
                }
            }
        } catch (IOException e) {
            // Gracefully close everything.
            closeEverything(socket, objInput, objOutput);
        }
    }

    public void sendPublicKey() {
        try {
            // Initially send the username of the client.
            objOutput.writeObject(username);
            objOutput.flush();
            objOutput.writeObject(keyPair.getPublic());
            objOutput.flush();
            System.out.println("Log: Public Key sent to server");
        } catch (IOException e) {
            // Gracefully close everything.
            closeEverything(socket, objInput, objOutput);
        }
    }

    // Listening for a message is blocking so need a separate thread for that.
    public void listenForMessage()throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,SignatureException,InvalidAlgorithmParameterException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;
                // While there is still a connection with the server, continue to listen for messages on a separate thread.
                while (socket.isConnected()) {
                    try {
                        // Get the messages sent from other users and print it to the console.
                        Object in = objInput.readObject();
                        handleMessage(in);
                    } catch (IOException |NoSuchAlgorithmException |NoSuchPaddingException |InvalidKeyException |IllegalBlockSizeException |BadPaddingException |SignatureException |InvalidAlgorithmParameterException| ClassNotFoundException e) {
                        // Close everything gracefully.
                        closeEverything(socket, objInput, objOutput);
                    }
                }
            }
        }).start();
    }

    public void handleMessage(Object message)throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,SignatureException,InvalidAlgorithmParameterException,IOException {
        //get message type
        switch (message.getClass().toString()){
            case "class java.lang.String":
                String msgFromGroupChat = (String) message;
                System.out.println(msgFromGroupChat);
                System.out.println();
                break;
            case "class sun.security.rsa.RSAPublicKeyImpl":
                System.out.println("LOG: Certificate Authority Public Key saved");
                System.out.println();
                PublicKey k = (PublicKey) message;
                CAPublicKey = k;
                break;
            case "class java.util.ArrayList":
                Client.clients = (ArrayList<ClientInfo>) message;
                // Validates all Clent's public key with CA
                for (ClientInfo client : clients) {
                    // if certificate is validation by CA/Server then the public key can be trusted
                    if (Certificates.validateCertificate(client.getCertificate(), CAPublicKey)){
                        client.trustPublicKey();
                        System.out.println("LOG: "+client.getUsername()+"\'s Public Key Verified");
                    }
                    else {
                        System.out.println("LOG: "+client.getUsername()+"\'s Public Key is not valid");
                        System.out.println();
                    }
                }
                System.out.println();
                System.out.println("Clients list recieved with size: "+ Client.clients.size());
                break;
            case "class PGPmessages":
                PGPmessages m = (PGPmessages) message;
                for (ClientInfo client : clients) {
                    if (client.getUsername() == m.getSenderUsername()){
                        // Only receive messages from Clients whose public key is trused by CA
                        if (client.isTrusted()) {
                            String decryptedMessage = PGPmessages.receiveMessage(m,keyPair.getPrivate(),client.getPublicKey());
                            System.out.println(client.getUsername() +": " +decryptedMessage);
                        }
                    }
                }
                break;
        }
        //get message command  
    }

    // Helper method to close everything so you don't have to repeat yourself.
    public void closeEverything(Socket socket, ObjectInputStream in, ObjectOutputStream out) {
        // Note you only need to close the outer wrapper as the underlying streams are closed when you close the wrapper.
        // Note you want to close the outermost wrapper so that everything gets flushed.
        // Note that closing a socket will also close the socket's InputStream and OutputStream.
        // Closing the input stream closes the socket. You need to use shutdownInput() on socket to just close the input stream.
        // Closing the socket will also close the socket's input stream and output stream.
        // Close the socket after closing the streams.
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Run the program.
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,SignatureException,InvalidAlgorithmParameterException{

        // Get a username for the user and a socket connection.
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your username for the group chat: ");
        String username = scanner.nextLine();
        // Create a socket to connect to the server.
        Socket socket = new Socket("localhost", 1234);

        // Pass the socket and give the client a username.
        Client client = new Client(socket, username);
        // Infinite loop to read and send messages.
        client.sendPublicKey();
        client.listenForMessage();
        client.sendMessage();
    }
}

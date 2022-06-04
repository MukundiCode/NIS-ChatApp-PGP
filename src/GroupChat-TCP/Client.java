import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.ArrayList;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

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
    public void sendMessage()throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        try {
            // Initially send the username of the client.
            // Create a scanner for user input.
            Scanner scanner = new Scanner(System.in);
            // While there is still a connection with the server, continue to scan the terminal and then send the message.
            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                objOutput.writeObject("SEND: " + messageToSend);
                objOutput.flush();
                objOutput.writeObject(asymmetricEncrypt(messageToSend,keyPair.getPrivate()));
                objOutput.flush();
            }
        } catch (IOException e) {
            // Gracefully close everything.
            closeEverything(socket, objInput, objOutput);
        }
    }

    private static byte[] asymmetricEncrypt(String text,Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
        byte[] textByte = text.getBytes();
        //encrypt
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key); //private key or public key
        byte[] encryptedText = cipher.doFinal(textByte);
        return encryptedText;
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
    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;
                // While there is still a connection with the server, continue to listen for messages on a separate thread.
                while (socket.isConnected()) {
                    try {
                        // Get the messages sent from other users and print it to the console.
                        //msgFromGroupChat = (String) objInput.readObject();
                        Object in = objInput.readObject();
                        handleMessage(in);
                        //System.out.println(msgFromGroupChat);
                    } catch (IOException | ClassNotFoundException e) {
                        // Close everything gracefully.
                        closeEverything(socket, objInput, objOutput);
                    }
                }
            }
        }).start();
    }

    public void handleMessage(Object message){
        //get message type
        System.out.println(message.getClass());
        switch (message.getClass().toString()){
            case "class java.lang.String":
                String msgFromGroupChat = (String) message;
                System.out.println(msgFromGroupChat);
                System.out.println();
                break;
            case "class java.util.ArrayList":
                Client.clients = (ArrayList<ClientInfo>) message;
                System.out.println("Clients list recieved with size: "+ Client.clients.size());
                //System.out.println(asymmetricDecrypt(msgBytes,ClientHandler.publicKeys.get(0)));
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
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{

        // Get a username for the user and a socket connection.
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your username for the group chat: ");
        String username = scanner.nextLine();
        // Create a socket to connect to the server.
        Socket socket = new Socket("localhost", 100);

        // Pass the socket and give the client a username.
        Client client = new Client(socket, username);
        // Infinite loop to read and send messages.
        System.out.println(Client.clients.getClass());
        client.sendPublicKey();
        client.listenForMessage();
        client.sendMessage();
    }
}

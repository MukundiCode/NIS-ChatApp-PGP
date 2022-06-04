import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

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
    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;
                // While there is still a connection with the server, continue to listen for messages on a separate thread.
                while (socket.isConnected()) {
                    try {
                        // Get the messages sent from other users and print it to the console.
                        msgFromGroupChat = (String) objInput.readObject();
                        System.out.println(msgFromGroupChat);
                    } catch (IOException | ClassNotFoundException e) {
                        // Close everything gracefully.
                        closeEverything(socket, objInput, objOutput);
                    }
                }
            }
        }).start();
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
        client.sendPublicKey();
        client.listenForMessage();
        client.sendMessage();
    }
}

// 1. Open a socket for the client
// 2. Open an input stream and output stream to the socket.
// 3. Read from and write to the stream according to the server's protocol.
// 4. Close the streams.
// 5. Close the socket.

import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.util.Iterator;


/**
 * When a client connects the server spawns a thread to handle the client.
 * This way the server can handle multiple clients at the same time.
 */


public class ClientHandler implements Runnable {

    // Array list of clientHandler objects ran as threads from Server
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    // Socket for a connection, buffer reader and writer for receiving and sending data respectively.
    private Socket socket;
    private ObjectOutputStream objOut;
    private ObjectInputStream objInput;
    private String clientUsername;

    // Creating the client handler from the socket the server passes.
    public ClientHandler(Socket socket) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        try {  
            this.socket = socket;
            this.objOut = new ObjectOutputStream(socket.getOutputStream());
            this.objInput = new ObjectInputStream(socket.getInputStream());
            // When a client connects their username is sent.
            this.clientUsername = (String) objInput.readObject();
            // Add the new client handler to the array so they can receive messages from others.
            clientHandlers.add(this);
            broadcastMessage("SERVER: " + clientUsername + " has entered the chat!");
        } catch (IOException | ClassNotFoundException e) {
            closeEverything(socket, objInput, objOut);
        }
    }

    // Everything in this method is run on a separate thread. We want to listen for messages
    // on a separate thread because listening (bufferedReader.readLine()) is a blocking operation.
    // A blocking operation means the caller waits for the callee to finish its operation.
    @Override
    public void run() {
        // Continue to listen for messages while a connection with the client is still established.
        while (socket.isConnected()) {
            try {
                // Read what the client sent and then send it to every other client.
                Object in = objInput.readObject();
                handleMessage(in);
            } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | ClassNotFoundException e) {
                // Close everything gracefully.
                try {
                    closeEverything(socket, objInput, objOut);
                } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
                        | IllegalBlockSizeException | BadPaddingException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                break;
            }
        }
    }

    // Send a message through each client handler thread so that everyone gets the message.
    // Basically each client handler is a connection to a client. So for any message that
    // is received, loop through each connection and send it down it.
    public void broadcastMessage(String messageToSend) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    // We want to encrypt with private and publics here.
                    clientHandler.objOut.writeObject(messageToSend);
                    clientHandler.objOut.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, objInput, objOut);
            }
        }
    }

    public void sendMessage(PGPmessages message) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (clientHandler.clientUsername.equals(message.getReceipientUsername())) {
                    // We want to encrypt with private and publics here.
                    clientHandler.objOut.writeObject(message);
                    clientHandler.objOut.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, objInput, objOut);
            }
        }
    }


    //Take a message, and if it is a message to broadcast, broadcast, but if it is a key, store the key
    public void handleMessage(Object message) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
        //get message type
        switch (message.getClass().toString()){
            case "class sun.security.rsa.RSAPublicKeyImpl":
                System.out.println("Public key for user saved");
                PublicKey k = (PublicKey) message;
                ClientInfo  c = new ClientInfo(clientUsername,k);
                Server.clients.add(c);
                sendClientInfo();
                break;
            case "class java.lang.String":
                String msg = (String) message;
                String command = (String) msg.substring(0, 4);

                switch(command){
                    case "SEND":
                        broadcastMessage(clientUsername +": " +msg.substring(6));
                        break;
                    default:
                        break;
            }
                break;
            case "class PGPmessages":
                sendMessage((PGPmessages)message);
                break;
            case "class [B":
                System.out.println("Dcrypting bytes");
                byte [] msgBytes = (byte[]) message;
                break;
        }
        //get message command  
    }

    public synchronized void sendClientInfo()throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
        if (Server.clients.size() > 2){
        for (int i = 0;i < clientHandlers.size();i++) {
            try {
                    clientHandlers.get(i).objOut.writeObject(Server.clients);
                    clientHandlers.get(i).objOut.flush();
                } catch (IOException e) {
                    closeEverything(socket, objInput, objOut);
                }   
        }
    }
    }

    // If the client disconnects for any reason remove them from the list so a message isn't sent down a broken connection.
    public void removeClientHandler() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUsername + " has left the chat!");
    }

    // Helper method to close everything so you don't have to repeat yourself.
    public void closeEverything(Socket socket, ObjectInputStream in, ObjectOutputStream out) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        // The client disconnected or an error occurred so remove them from the list so no message is broadcasted.
        removeClientHandler();
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
}
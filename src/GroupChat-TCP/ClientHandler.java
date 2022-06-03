// 1. Open a socket for the client
// 2. Open an input stream and output stream to the socket.
// 3. Read from and write to the stream according to the server's protocol.
// 4. Close the streams.
// 5. Close the socket.

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.stream.Collectors;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * When a client connects the server spawns a thread to handle the client.
 * This way the server can handle multiple clients at the same time.
 */


public class ClientHandler implements Runnable {

    // Array list of clientHandler objects ran as threads from Server
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    public static ArrayList<PublicKey> publicKeys = new ArrayList<PublicKey>();

    // Socket for a connection, buffer reader and writer for receiving and sending data respectively.
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    // Creating the client handler from the socket the server passes.
    public ClientHandler(Socket socket) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        try {  
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(),StandardCharsets.UTF_8));
            this.bufferedWriter= new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),StandardCharsets.UTF_8));
            // When a client connects their username is sent.
            this.clientUsername = bufferedReader.readLine();
            // Add the new client handler to the array so they can receive messages from others.
            clientHandlers.add(this);
         //   broadcastMessage("SERVER: " + clientUsername + " has entered the chat!");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    // Everything in this method is run on a separate thread. We want to listen for messages
    // on a separate thread because listening (bufferedReader.readLine()) is a blocking operation.
    // A blocking operation means the caller waits for the callee to finish its operation.
    @Override
    public void run() {
        String messageFromClient;
        // Continue to listen for messages while a connection with the client is still established.
        while (socket.isConnected()) {
            try {
                // Read what the client sent and then send it to every other client.
                messageFromClient = bufferedReader.readLine();
                System.out.println("Message From client: " + messageFromClient);
                handleMessage(messageFromClient);
            } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
                // Close everything gracefully.
                try {
                    closeEverything(socket, bufferedReader, bufferedWriter);
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
                    clientHandler.bufferedWriter.write(asymmetricDecrypt(messageToSend.getBytes(),ClientHandler.publicKeys.get(0)));
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    //Take a message, and if it is a message to broadcast, broadcast, but if it is a key, store the key
    public void handleMessage(String message) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
        //get message command
        String command = message.substring(0, 4);

        switch(command){
            case "SEND":
                broadcastMessage(message);
                break;
            case "KEYU":
                String modulus = bufferedReader.readLine();
                String exponent = bufferedReader.readLine();
                String[] splitedModulus = modulus.split("\\s+");
                String[] splitedExponent = exponent.split("\\s+");
                RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(splitedModulus[2]), new BigInteger(splitedExponent[3]));
                KeyFactory factory = KeyFactory.getInstance("RSA");
                PublicKey pub = factory.generatePublic(spec);
                System.out.println(pub);
                ClientHandler.publicKeys.add(pub);
                break;
        }
    }

    // If the client disconnects for any reason remove them from the list so a message isn't sent down a broken connection.
    public void removeClientHandler() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUsername + " has left the chat!");
    }

    // Helper method to close everything so you don't have to repeat yourself.
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        // The client disconnected or an error occurred so remove them from the list so no message is broadcasted.
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String asymmetricDecrypt(byte[] text,Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
        //dencrypt
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key); //public key or private key
        byte[] encryptedText = cipher.doFinal(text);
        String result = new String(encryptedText);
        return result;
    }
}
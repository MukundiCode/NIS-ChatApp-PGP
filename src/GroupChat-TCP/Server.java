import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.*;
import java.util.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;


public class Server {

    private final ServerSocket serverSocket;
    public static HashMap<String, ClientInfo> hashTable = new HashMap<String, ClientInfo>();
    private PrivateKey privateKey;
    public static PublicKey publicKey;

    public Server (ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        RSA keyPair = new RSA();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
    }

    public void startServer() {
        try {
            // Listen for connections, from clients, on port 100.
            while (!serverSocket.isClosed()) {
                // Will be closed in the Client Handler.
                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected!");
                ClientHandler clientHandler = new ClientHandler(socket);
                Thread thread = new Thread(clientHandler);
                // The start method begins the execution of a thread of type ClientHandler which deals with BufferedReader and BufferedWeriter
                thread.start();
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Create the server socket and start the sever, accepting any requests from clients and parsing that into the clientHandler
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(100);
        Server server = new Server(serverSocket);
        server.startServer();
    }

}

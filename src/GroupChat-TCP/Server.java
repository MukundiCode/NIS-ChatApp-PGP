import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public class Server {

    private final ServerSocket serverSocket;
    private RSA keyPair;
    private PrivateKey CAPrivateKey;
    private PublicKey CAPublicKey;
    private X509Certificate CACertificate;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        keyPair = new RSA();
        CAPrivateKey = keyPair.getPrivate();
        CAPublicKey = keyPair.getPublic();
        CACertificate = Certificates.generateCACertificate(keyPair);
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

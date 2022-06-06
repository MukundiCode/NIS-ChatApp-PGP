import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.util.ArrayList;
import org.bouncycastle.operator.OperatorCreationException;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class Server {

    private final ServerSocket serverSocket;
    public static ArrayList<ClientInfo> clients = new ArrayList<ClientInfo>();
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    private static RSA keyPair;
    private static X509Certificate CACertificate;
    public static PublicKey CAPublicKey;

    public Server(ServerSocket serverSocket) throws CertificateException, OperatorCreationException, NoSuchAlgorithmException, KeyStoreException, IOException {
        this.serverSocket = serverSocket;
        keyPair = new RSA();
        CAPublicKey = keyPair.getPublic();
        // creates root certificate
        CACertificate = Certificates.generateCACertificate(keyPair);
        System.out.println("LOG: CA Root certificate generated");
        System.out.println();
    }

    public void startServer()throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
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
    public static void main(String[] args) throws IOException, CertificateException, OperatorCreationException, NoSuchAlgorithmException, KeyStoreException {
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        try {
            server.startServer();
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
                | BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}

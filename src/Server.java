import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Server implementation
 * 
 * This program is adapted from www.codejava.net
 */
public class Server {
    private DatagramSocket socket;
    private Random random;
    public static HashMap<String, ClientInfo> hashTable = new HashMap<String, ClientInfo>();
    public static HashMap<String, Boolean> reconnecting = new HashMap<String, Boolean>();
    public static boolean lossMode;
    public static boolean corruptMode;

    public Server(int port) throws SocketException {
        socket = new DatagramSocket(port);
        random = new Random();
    }

    
    /** 
     * Main method handles input, checks for errors
     * 
     * @param args port number
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Syntax: Server <port>");
            return;
        }

        lossMode = false;
        corruptMode = false;
        if (args.length > 1) {
            for (int j = 1; j < args.length; ++j) {
                if (args[j].compareTo("-loss") == 0) {
                    lossMode = true;
                    System.out.println("Loss mode active.");
                }
                if (args[j].compareTo("-corrupt") == 0) {
                    corruptMode = true;
                    System.out.println("Corruption mode active.");
                }
            }
        }

        int port = Integer.parseInt(args[0]);

        try {
            Server server = new Server(port);
            server.service();
        } catch (SocketException ex) {
            System.out.println("Socket error: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }

    
    /** 
     * Wait for clients to join
     * 
     * @throws IOException
     */
    private void service() throws IOException {
        while (true) {
            byte[] buf = new byte[512];
            // receive request
            DatagramPacket request = new DatagramPacket(buf, buf.length);
            socket.receive(request);
            System.out.println("Request received");
            String clientID = new String(request.getData(), 0, request.getLength());
            // Spawn thread

            InetAddress clientAddress = request.getAddress();
            int clientPort = request.getPort();

            int newPort = random.nextInt(14000) + 49500; 

            DatagramSocket newSocket = new DatagramSocket(newPort + 1);

            if (hashTable.containsKey(clientID)) {
                System.out.println("reconnecting: " + reconnecting.get(clientID));
                if (reconnecting.get(clientID)) {
                    System.out.println("Resending port to client");
                    int port = hashTable.get(clientID).getPort();

                    byte[] buffer = Integer.toString(port).getBytes();

                    DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);

                    if (!lossMode)
                        newSocket.send(response);
                    else {
                        int shouldSend = random.nextInt(2);
                        if (shouldSend != 0) {
                            newSocket.send(response);
                        } else {
                            System.out.println("Losing message...");
                        }
                    }

                } else {
                    System.out.println("Client ID not unique, sending cancellation to client.");
                    newPort = 0;
                    byte[] buffer = Integer.toString(newPort).getBytes();

                    DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
                    socket.send(response);
                }

            } else {

                hashTable.put(clientID, new ClientInfo(newPort, clientAddress)); //add public key here
                reconnecting.put(clientID, true);

                System.out.println("Added to hashTable, now the size is: " + hashTable.size());

                byte[] buffer = Integer.toString(newPort).getBytes();

                DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);

                if (!lossMode)
                    newSocket.send(response);
                else {
                    int shouldSend = random.nextInt(2);
                    System.out.println(shouldSend);
                    if (shouldSend != 0) {
                        newSocket.send(response);
                    } else {
                        System.out.println("Message lost");
                    }
                }

                System.out.println("Port allocated to client " + clientID + "(" + clientAddress.getHostName() + ", "
                        + clientAddress.getHostAddress() + "). Port number is " + newPort);
                new ServerThread(newSocket, clientID).start();

            }
        }
    }
}
import java.io.*;
import java.net.*;

/**
 * Client spawns this thread to able to continually receive messages and display on GUI
 * Even when you are simulataneously sending messages.
 */
public class ClientListenerThread extends Thread {
    int port;
    String responseID;
    boolean listen;
    String clientID;
    int lastport;
    DatagramSocket socket;

    /**
     * constructor
     * 
     * @param port  
     * @param clientID  
     */
    public ClientListenerThread(int port, String clientID) {
        this.port = port;
        listen = true;
        this.clientID = clientID;
    }

    /**
     * Stops the thread
     */
    public void stopListen() {
        listen = false;
    }

    /**
     * Receives messages and sends appropriate confirmations to the sender to ensure correctness
     */
    public void run() {
        try {
            socket = new DatagramSocket(port);
            byte[] buffer = new byte[512];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            socket.setSoTimeout(5000);
            while (listen) {
                try {
                    socket.receive(response);
                    int srcPort = response.getPort();
                    InetAddress srcIP = response.getAddress();
                    String received = new String(response.getData(), 0, response.getLength());
                    long checksum = Long.parseLong(received.substring(0, received.indexOf("^")));
                    String handle = received.substring(received.indexOf("^") + 1, received.lastIndexOf("^"));
                    String clientSender = received.substring(received.lastIndexOf("^") + 1, received.indexOf("%"));
                    received = received.substring(received.indexOf("%") + 1, received.length());
                    if (checksum != (Client.getCheckSum(received))) {
                    } else {
                        if (clientSender.compareTo(clientID) == 0) {
                        } else {
                            if (lastport == srcPort) {
                                sendConfirm(srcPort, srcIP);
                            } else {
                                if (handle.compareTo("error") == 0) {
                                    Client.newMessage("Client" + clientSender
                                            + " went offline before they received the message: " + received);
                                    sendConfirm(srcPort, srcIP);
                                    lastport = srcPort;
                                } else {
                                    System.out.println("New message from Client " + clientSender + ": " + received);
                                    Client.newMessage("New message from Client " + clientSender + ": " + received);
                                    lastport = srcPort;
                                    sendConfirm(srcPort, srcIP);
                                }
                            }
                        }
                    }
                } catch (SocketTimeoutException e1) {

                } catch (Exception e) {
                    System.out.println("Incorrect format, message ignored.");
                }
            }
            socket.close();
            System.out.println("Closing Listener");
        } catch (

        IOException e) {
            System.out.println("Oh no!");
        }
    }

    /**
     * Acknowledgement
     * 
     * @param port Source port
     * @param ip   Source ip
     */
    public void sendConfirm(int port, InetAddress ip) {
        try {
            byte[] confirm = ("Confirm_" + port).getBytes();
            DatagramPacket confirmMsg = new DatagramPacket(confirm, confirm.length, ip, port);
            socket.send(confirmMsg);
        } catch (IOException e) {
            System.exit(0);
        }
    }
}

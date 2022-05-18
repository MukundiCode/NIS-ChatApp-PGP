import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.zip.CRC32;

/**
 * This program is adapted from www.codejava.net
 * 
 * Client class handles creation and setup of client, sending of messages to server
 */
public class Client {

    static GUI gui;
    static InetAddress address;
    static int port;
    static int newPort;
    static DatagramSocket socket;
    static DatagramPacket response;
    static byte[] buffer;
    static int checkDigit;
    static ClientListenerThread listener;
    static String thisID;
    static boolean corruptMode;
    public static boolean lossMode;

    
    /** 
     * Main method does error checking and input
     * 
     * @param args Takes in hostname, port and client ID
     * 
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Syntax: Client <hostname> <port> <id>");
            return;
        }
        lossMode = false;
        corruptMode = false;
        if (args.length > 3) {
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

        String hostname = args[0];
        port = Integer.parseInt(args[1]);
        String id = args[2];

        if (id.indexOf("^") > -1 | id.indexOf("%") > -1) {
            System.out.println("Client ID can't contain '%' or '^'. Try again.");
            System.exit(1);
        }

        // Fire up GUI
        gui = new GUI(id, hostname, port);

        requestJoin(hostname, port, id);
    }

    
    /** 
     * Ask server to set up client
     * 
     * @param hostname e.g. localhost
     * @param port port number
     * @param id client ID
     */
    public static void requestJoin(String hostname, int port, String id) {
        thisID = id;
        int attempts = 0;
        while (true) {
            try {
                socket = new DatagramSocket();
                attempts++;
                socket.setSoTimeout(200);
                checkDigit = 0;
                if (attempts > 10) {
                    System.out.println("10 failed request attempts. Shutting Down.");
                    socket.close();
                    System.exit(1);
                }

                // Send request to server for port for ClientListenerThread
                address = InetAddress.getByName(hostname);

                DatagramPacket request = new DatagramPacket(id.getBytes(), id.length(), address, port);

                if (!lossMode)
                    socket.send(request);
                else {
                    Random random = new Random();
                    int shouldSend = random.nextInt(2);
                    if (shouldSend != 0) {
                        socket.send(request);
                    } else {
                        System.out.println("Losing message...");
                    }
                }
                System.out.println("Request sent");

                buffer = new byte[512];
                response = new DatagramPacket(buffer, buffer.length);
                socket.receive(response);

                String portNum = new String(buffer, 0, response.getLength());
                newPort = Integer.parseInt(portNum);
                if (newPort != 0) {
                    listener = new ClientListenerThread(newPort, thisID);
                    listener.start();
                    DatagramPacket message;
                    byte[] buf = ("0").getBytes();
                    message = new DatagramPacket(buf, buf.length, address, newPort + 1);
                    socket.send(message);
                    break;
                } else {
                    System.out.println("Please try again with a unique ID.");
                    System.exit(1);
                }
            } catch (SocketTimeoutException ex) {
                System.out.println("Socket timed out. Resending request.");
            } catch (IOException ex) {
                System.out.println("Client error: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    /**
     * newMessage() is called from the ClientListenerThread and updates the GUI
     * accordingly.
     *
     */
    public static void newMessage(String text) {

        if (gui.incomingMessages.getText().trim().length() == 0) {
            gui.incomingMessages.setText(text);
        } else {
            gui.incomingMessages.append("\n" + text);
        }

    }

    /**
     * sendMessage() is invoked from the GUI class and sends the relevant message to
     * the given ClientID. copy/pasted from Tims working code for the terminal -
     * Thanks Tim :)
     */

    public static void sendMessage(String otherClientID, String msgString) {

        try {
            System.out.println("Sending Message to ServerThread");
            socket = new DatagramSocket();
            byte[] buf;
            DatagramPacket message;
            if (otherClientID.compareTo("quit") == 0) {
                buf = ("quit").getBytes();
                message = new DatagramPacket(buf, buf.length, address, newPort + 1);
                confirmEnd(message, 0);
            } else {
                long checkSum = getCheckSum(msgString);
                System.out.println("This is the checksum: " + checkSum);
                String header = otherClientID + "^" + checkDigit + "^" + checkSum + "^" + thisID;
                System.out.println("This is the header" + header);
                buf = (header + "%" + msgString).getBytes();
                message = new DatagramPacket(buf, buf.length, address, newPort + 1);
                confirm(message, 0);
            }

        } catch (IOException ex) {
            System.out.println("Client error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    
    /** 
     * Checksum method
     * 
     * @param message string data of message
     * @return long 
     */
    static long getCheckSum(String message) {
        byte[] msg = message.getBytes();
        CRC32 check = new CRC32();
        check.update(msg, 0, msg.length);
        return check.getValue();
    }

    
    /**
     * Alternates check digit
     * @param i
     * @return int
     */
    static int changeCheck(int i) {
        if (i == 0) {
            return 1;
        } else {
            return 0;
        }
    }

    
    /** 
     * Does all the necessary checks for a message, such as loss and error prevention
     * 
     * @param message datagram message
     * @param count Avids infinite loops
     */
    static void confirm(DatagramPacket message, int count) {
        try {
            if (count > 10) {           //after 10 retransmission attempts, assume failure of server
                System.out.println("Too many confirmation timeouts. Server failure. Shutting down.");
                listener.stopListen();
                socket.close();
                System.exit(1);
            }
            if (corruptMode) {                      //special functionality for corrupt mode
                Random random = new Random();
                int shouldSend = random.nextInt(2);
                if (shouldSend == 0) {
                    String data = new String(message.getData(), 0, message.getLength());
                    int first = data.indexOf("^");
                    data = data.substring(0, data.indexOf("^", first + 1)) + "^corrupt"
                            + data.substring(data.lastIndexOf("^"));
                    byte[] corruptMsg = data.getBytes();
                    DatagramPacket corruptmsg = new DatagramPacket(corruptMsg, corruptMsg.length, message.getAddress(),
                            message.getPort());
                    socket.send(corruptmsg);
                    System.out.println("Corrupting message...");
                } else {
                    socket.send(message);
                }
            } else {
                socket.send(message);
            }

            buffer = new byte[512];
            response = new DatagramPacket(buffer, buffer.length);
            socket.setSoTimeout(200);
            try {
                socket.receive(response);
                String received = new String(response.getData(), 0, response.getLength());
                System.out.println(received);
                System.out.println();
                if (received.compareTo("Confirm_" + checkDigit) == 0) {
                    System.out.println("Successful Message Send.");
                    checkDigit = changeCheck(checkDigit);
                } else if (received.compareTo("InvalidID")==0){
                    gui.incomingMessages.append(" -- ERROR: INVALID CLIENTID --");
                    gui.clientID_Input.setText("Please enter valid client ID");
                }
                else {
                   count++;
                   confirm(message,count);
                }

            } catch (SocketTimeoutException e) {        //recursively calls if timedout
                System.out.println("Did not get a response, sending message again.");
                count++;
                confirm(message, count);
            }
        } catch (IOException ex) {
            System.out.println("Client error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    
    /** 
     * Does the checks when quitting the program
     * 
     * @param message
     * @param count Makes sure we don't check forever
     */
    static void confirmEnd(DatagramPacket message, int count) {
        try {
            if (count > 10) {
                System.out.println("Too many confirmation timeouts. Assume server closed. Shutting down.");
                listener.stopListen();
                socket.close();
                System.exit(1);
            }
            socket.send(message);

            buffer = new byte[512];
            response = new DatagramPacket(buffer, buffer.length);
            socket.setSoTimeout(200);
            try {
                socket.receive(response);
                String received = new String(response.getData(), 0, response.getLength());
                System.out.println(received);
                System.out.println();
                if (received.compareTo("Confirm_Quit") == 0) {
                    System.out.println("Successful Quit Send.");
                }
                socket.close();
                listener.stopListen();

            } catch (SocketTimeoutException e) {
                System.out.println("Did not get a response, sending message again.");
                count++;
                confirmEnd(message, count);
            }
        } catch (IOException ex) {
            System.out.println("Client error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}
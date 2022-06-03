import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.CRC32;

/**
 * One thread for each client, runs an infinite loop to keep listening to the client
 */
public class ServerThread extends Thread {

    protected DatagramSocket socket = null;
    protected String clientID;

    public ServerThread(DatagramSocket newSocket, String clientID) throws IOException {
        this("ServerThread", newSocket, clientID);

    }

    public ServerThread(String name, DatagramSocket newSocket, String clientID) throws IOException {
        super(name);
        socket = newSocket;
        this.clientID = clientID;
    }

    /**
     * Listens for client's messages, as well as does extensive error checking to see if the message is correct
     */
    public void run() {
        int check = 0; // int that holds checkdigit for message
        InetAddress address;
        int port;
        while (true) {
            try {
                byte[] buf = new byte[512];

                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                Server.reconnecting.replace(clientID, false);
                if (packet.getData().equals("0".getBytes())) {
                    continue;
                }

                address = packet.getAddress();
                port = packet.getPort();

                String received = new String(packet.getData(), 0, packet.getLength());
                int hInt = received.indexOf("%");
                if (hInt == -1) { 
                    if (received.compareTo("quit") == 0) {
                        break;
                    }
                }
                String header = received.substring(0, hInt);
                String message = received.substring(hInt + 1);
                int first = header.indexOf("^");
                int middle = header.indexOf("^", first + 1);
                int last = header.lastIndexOf("^");
                int clientCheckDig = Integer.parseInt(header.substring(first + 1, middle));
                long clientCheckSum = Long.parseLong(header.substring(middle + 1, last));
                String clientsID = header.substring(0, first);

                // Check if clientsID is correct
                boolean found = true;
                if (clientsID.compareTo("broadcast") != 0) {
                    found = Server.hashTable.containsKey(clientsID);
                }

                long checkSum = getCheckSum(message);

                if (!found) {
                    buf = ("InvalidID").getBytes();
                } else if ((clientCheckDig != check) || (checkSum != clientCheckSum)) { // checks to see if have already
                                                                                        // processed this message or if
                                                                                        // it got corrupted
                    buf = ("Confirm_" + clientCheckDig).getBytes();
                } else if (clientsID.compareTo("broadcast") == 0) {
                    buf = ("Confirm_" + check).getBytes();
                    for (String ID : Server.hashTable.keySet()) {
                        new ServerSenderThread(ID + received.substring(first)).start();
                    }
                    check = changeCheck(check);
                } else {
                    // Get confirmation message
                    buf = ("Confirm_" +check).getBytes();
                    // Processes message
                    System.out.println("Spawning sender thread");
                    new ServerSenderThread(received).start();

                    check = changeCheck(check);
                }

                if (Server.corruptMode) {
                    Random random = new Random();
                    int shouldSend = random.nextInt(2);
                    if (shouldSend == 0) {
                        System.out.println("Corrupting message...");
                        byte[] corrupt = new byte[10];
                        random.nextBytes(corrupt);
                        buf[5] = corrupt[5];
                    }
                }

                System.out.println(new String(buf, 0, buf.length));
                // Sends confirmation to message's IP and Port
                packet = new DatagramPacket(buf, buf.length, address, port);
              
                if (!Server.lossMode)
                    socket.send(packet);
                else {
                    Random random = new Random();
                    int shouldSend = random.nextInt(2);
                    if (shouldSend != 0) {
                        socket.send(packet);
                    } else {
                        System.out.println("Losing message...");
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("ServerThread - Incorrect format, message ignored.");
            }
        }
        try {
            while (true) {                                      //sends confirmation of exit
                byte[] bufEnd = "Confirm_Quit".getBytes();
                DatagramPacket packet = new DatagramPacket(bufEnd, bufEnd.length, address, port);
                socket.send(packet);

                socket.setSoTimeout(500);

                byte[] buf = new byte[512];
                DatagramPacket anything = new DatagramPacket(buf, buf.length);
                socket.receive(anything);

            }
        } catch (SocketTimeoutException e) {
            System.out.println("Server Thread shutting down.");
            Server.hashTable.remove(clientID);
        } catch (IOException e1) {
            System.out.println("IO ERROR");
        }
        socket.close();
    }

    /**
     * Takes a message and finds its checksum
     * 
     * @param message   string data of message
     * @return long
     */
    static long getCheckSum(String message) {
        byte[] msg = message.getBytes();
        CRC32 check = new CRC32();
        check.update(msg, 0, msg.length);
        return check.getValue();
    }

    /**
     * Alternates check digit between 1 and 0
     * @param i check digit
     * @return int
     */
    static int changeCheck(int i) {
        if (i == 0) {
            return 1;
        } else {
            return 0;
        }
    }
}
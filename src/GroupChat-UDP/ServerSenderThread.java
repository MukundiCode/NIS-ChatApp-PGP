import java.io.*;
import java.net.*;

/**
 * Spawned to send message to destination client
 */
public class ServerSenderThread extends Thread {
    String message;
    DatagramSocket socket;

    public ServerSenderThread(String message) throws IOException {
        this.message = message;

    }

    /**
     * String manipulation to send the message to the destination client
     */
    public void run() {
        try {
            System.out.println("Started");
            byte[] buf = new byte[512];
            int i = message.indexOf('%');
            if (i >= 0) {
                String otherClient = message.substring(0, message.indexOf('^'));
                System.out.println("Other client is: " + otherClient);
                int last = message.substring(0, i).lastIndexOf("^");
                String thisClient = message.substring(last + 1, i);
                String msgData = message.substring(i + 1, message.length());
                long csSend = ServerThread.getCheckSum(msgData);
                message = csSend + "^message^" + thisClient + "%" + msgData;
                System.out.println("Message is: " + message);
                buf = message.getBytes();

                for (String name : Server.hashTable.keySet()) {
                    String key = name.toString();
                    String value = Server.hashTable.get(name).toString();
                    System.out.println(key + " " + value);
                }

                ClientInfo c = Server.hashTable.get(otherClient);
                System.out.println(c);
                // send the response to the client at "address" and "port"
                InetAddress address = c.getAddress();
                int port = c.getPort();

                socket = new DatagramSocket();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);

                confirmMessage(packet, otherClient, thisClient, csSend, msgData);
                socket.close();
                System.out.println("Sent to port " + port);
            } else {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets confirmation that message is received by destination client
     * 
     * @param message   message to send
     * @param otherID   Client ID of destination client
     * @param sourceID  Client ID of source client
     * @param cs        checksum value
     * @param msg       string data of the datagram packet
     */
    public void confirmMessage(DatagramPacket message, String otherID, String sourceID, long cs, String msg) {
        try {
            boolean msgConfirm = false;
            int count = 0;
            while (!msgConfirm) {
                socket.send(message);
                byte[] buffer = new byte[512];
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                socket.setSoTimeout(150);
                try {
                    socket.receive(response);
                    String received = new String(response.getData(), 0, response.getLength());
                    if (received.compareTo("Confirm_" + socket.getLocalPort()) == 0) {
                        msgConfirm = true;
                        System.out.println("Successful Message Send to Destination.");
                    }

                } catch (SocketTimeoutException e) {
                    System.out.println("Did not get a response from destination client, sending message again.");
                    count++;
                }
                if (count > 10) {
                    System.out.println("Destination client did not receive message. Sending back to source.");
                    ClientInfo source = Server.hashTable.get(sourceID);
                    InetAddress sourceIP = source.getAddress();
                    int sourcePort = source.getPort();
                    String data = +cs + "^error^" + otherID + "%" + msg;
                    byte[] buf = data.getBytes();
                    DatagramPacket error = new DatagramPacket(buf, buf.length, sourceIP, sourcePort);
                    confirmError(error);
                    msgConfirm = true;

                }
            }
        } catch (IOException e1) {
            System.out.print("IO Error. Exiting.");
            System.exit(0);
        }
    }

    /**
     * Sends message back to source client, saying the destination client was not able to receive the message
     * 
     * @param message
     */
    public void confirmError(DatagramPacket message) {
        try {
            boolean msgConfirm = false;
            int count = 0;
            while (!msgConfirm) {
                socket.send(message);
                byte[] buffer = new byte[512];
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                socket.setSoTimeout(150);
                try {
                    socket.receive(response);
                    String received = new String(response.getData(), 0, response.getLength());
                    System.out.println(received);
                    if (received.compareTo("Confirm_" + socket.getLocalPort()) == 0) {
                        msgConfirm = true;
                        System.out.println("Successful Error Send.");
                    }

                } catch (SocketTimeoutException e) {
                    System.out.println("Did not get a response from source client, sending message again.");
                    count++;
                }
                if (count > 10) {
                    System.out.println("Source did not receive error. Forgetting message.");
                    msgConfirm = true;
                }
            }
        } catch (IOException e1) {
            System.out.print("IO Error. Exiting.");
            System.exit(0);
        }
    }
}

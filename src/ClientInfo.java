import java.net.InetAddress;

/**
 * Wrapper class for some info
 */
public class ClientInfo {
    private int port;
    private InetAddress address;

    public ClientInfo(int port, InetAddress address) {
        this.port = port;
        this.address = address;
    }

    /**
     * gets port number
     * 
     * @return int
     */
    public int getPort() {
        return port;
    }

    /**
     * gets internet address
     * 
     * @return InetAddress
     */
    public InetAddress getAddress() {
        return address;
    }
}

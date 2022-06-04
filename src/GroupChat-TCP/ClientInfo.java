import java.net.InetAddress;
import java.security.PublicKey;

/**
 * Wrapper class for some specific client info
 */
public class ClientInfo {
    private int port;
    private InetAddress address;
    private byte[] publicKey;
    private String username;

    public ClientInfo(String user, byte[] KU) {
        this.username = user;
        this.publicKey = KU;
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

    /**
     * gets public key
     * 
     * @return publicKey
     */
    public byte[] getPublicKey() {
        return publicKey;
    }

    /**
     * gets user name
     * 
     * @return username
     */
    public byte[] getUsername() {
        return username;
    }
}

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hashing {

    public static String computeHash(String message) throws NoSuchAlgorithmException, UnsupportedEncodingException{
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(message.getBytes("UTF-8"));
        //sign the hash with addSignature from Encryption.java this compute hash should receive a key as a argument
        String output = hexStringConverter(encodedhash);
        return output;
    }

    public static String hexStringConverter(byte[] digestHash){
        StringBuilder hexString = new StringBuilder(2 * digestHash.length);
        for (int i = 0; i < digestHash.length; i++) {
            String hex = Integer.toHexString(0xff & digestHash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        System.out.println(computeHash("Hello how are you"));
    }
}

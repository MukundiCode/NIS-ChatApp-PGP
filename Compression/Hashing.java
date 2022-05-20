import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public class Hashing{

    public static String computeHash(String message) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException, SignatureException{
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(message.getBytes("UTF-8"));
        
        //sign the hash with addSignature from Encryption.java this compute hash should receive a key as a argument
        //private static byte[] addSignature(byte [] text, Key key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException{
        //had to change the visibility of add signature to public
        /*
        The method header would change to String computeHash(String message,Key signKey) line 19 would be the code used to sign the hash
        byte [] signedHash = Encryption.addSignature(encodedhash, signKey);
        */
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

    public static boolean verifyHash(String receivedHash, String receivedMessage) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException, SignatureException{
        String computedHash = Hashing.computeHash(receivedMessage);
        if(receivedHash.equals(computedHash)){
            return true;
        }else{
            return false;
        }
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException, SignatureException {
        System.out.println(computeHash("Hello how are you"));
    }
}

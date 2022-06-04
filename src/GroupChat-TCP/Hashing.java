import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

/**
 * Hashing class currently using sha-256
 * https://www.baeldung.com/sha-256-hashing-java - Aided in the hex represenation of the SHA-256 byte array
 */
public class Hashing{

    /**
     * Compute hash uses java message digest need to add the signing of the hash component- will do when we integrate everything
     * @param message client message used for computing hash
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public static byte[] computeHash(String message) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException, SignatureException{
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(message.getBytes("UTF-8"));
        
        //sign the hash with addSignature from Encryption.java this compute hash should receive a key as a argument
        //private static byte[] addSignature(byte [] text, Key key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException{
        //had to change the visibility of add signature to public
        /*
        The method header would change to String computeHash(String message,Key signKey) line 19 would be the code used to sign the hash
        byte [] signedHash = Encryption.addSignature(encodedhash, signKey);
        */
        return encodedhash;
    }

    /**
     * Generate Hex representation from the bytecode array hash
     * @param digestHash jaav message digest bytecode hash
     * @return
     */
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

    /**
     * Method used to compare received hash with the computed hash
     * True - hashes match
     * False - hashes do not match
     * @param receivedHash hash contained in the message
     * @param receivedMessage message received hash will be computed using it as input
     * @return bool true or false
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws SignatureException
     */
    public static boolean verifyHash(String receivedHash, String receivedMessage) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException, SignatureException{
        // String computedHash = Hashing.computeHash(receivedMessage);
        // if(receivedHash.equals(computedHash)){
        //     return true;
        // }else{
        //     return false;
        // }
        return false;
    }
}

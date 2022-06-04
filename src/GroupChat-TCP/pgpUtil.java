import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;

public class pgpUtil {
    

    public static void sendMessage(String plainText, Key sendPrivateKey, Key shortLivedKey) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException, SignatureException{
        //step 1 hash of message
        byte [] messageHash = Hashing.computeHash(plainText);
    
        //step 2 sign the hash
        byte [] signedMessageHash = 

        System.out.println(messageHash);





    }

    public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException, SignatureException {
        byte [] messageHash = Hashing.computeHash("hello guys");
    
        //step 2 sign the hash
        System.out.println(messageHash);
        

    }
}

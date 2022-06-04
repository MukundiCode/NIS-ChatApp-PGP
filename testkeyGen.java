import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class testkeyGen {
    
    public static void main(String[] args) throws NoSuchAlgorithmException {
        // create new key
        SecretKey secretKey = KeyGenerator.getInstance("AES").generateKey();
        // get base64 encoded version of the key
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
    

        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        // rebuild key using SecretKeySpec
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES"); 

        System.out.println(secretKey.toString());
        System.out.println(originalKey.toString());
    }
    
}

        // KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        // keyGen.init(256); // for example
        // SecretKey secretKey = keyGen.generateKey();
        // //System.out.println(secretKey.toString());
        // byte [] sharedKeyArr = secretKey.getEncoded();
        // System.out.println(sharedKeyArr.length);
        // String sharedKeyStringRep = new String(sharedKeyArr);
        // System.out.println(sharedKeyStringRep);
        // byte [] tranformedArr = sharedKeyStringRep.getBytes();
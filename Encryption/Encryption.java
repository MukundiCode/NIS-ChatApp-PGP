import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;

public class Encryption {

    /*Method takes in a secret key and a cipher message, returns encrypted */
    private static String symmetricEncrypt(Key key, String text, IvParameterSpec ivSpec) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{

        //get IV
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        //encryption
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encryptedOutput = cipher.doFinal(text.getBytes());
        String result = new String(encryptedOutput);
        return result;
        
    }


    /*Method takes in a secret key and a cipher message, returns decrypted */
    private static String symmetricDecrypt(Key key, String text,IvParameterSpec ivSpec) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{

        //get IV
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        //Decrypt
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        byte[] encryptedOutput = cipher.update(text.getBytes(),0,text.length()); //seems like it moves with block size of 16, so leaves some text
       //byte[] encryptedOutput = cipher.doFinal(text.getBytes());
        String result = new String(encryptedOutput);
        return result;
        
    }

    public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        // specify we want a key length of 192 bits, allowed for AES
        generator.init(128);
        Key key = generator.generateKey();
        byte[] random = new byte[16];
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.nextBytes(random);
        IvParameterSpec ivSpec = new IvParameterSpec(random);
        String c = symmetricEncrypt(key, "My name is Mukundi and i love swimming",ivSpec);
        System.out.println(c);
        System.out.println(symmetricDecrypt(key, c,ivSpec));
    }


}
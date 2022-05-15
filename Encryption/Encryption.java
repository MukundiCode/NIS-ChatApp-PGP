import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;

public class Encryption {

    /*Method takes in a secret key and a cipher message, returns encrypted */
    private static byte[] symmetricEncrypt(Key key, String text, IvParameterSpec ivSpec) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{

        //get IV
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        //encryption
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encryptedOutput = cipher.doFinal(text.getBytes());
        return encryptedOutput;
        
    }


    /*Method takes in a secret key and a cipher message, returns decrypted */
    private static String symmetricDecrypt(Key key, byte[] text,IvParameterSpec ivSpec) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
        //get IV
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        //Decrypt
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        byte[] encryptedOutput = cipher.doFinal(text);
        String result = new String(encryptedOutput);
        return result;
        
    }

    private static byte[] asymmetricEncrypt(String text,Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
        byte[] textByte = text.getBytes();
        //encrypt
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key); //private key
        byte[] encryptedText = cipher.doFinal(textByte);
        return encryptedText;
    }

    private static String asymmetricDecrypt(byte[] text,Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
        //dencrypt
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, key); //public key
        byte[] encryptedText = cipher.doFinal(text);
        String result = new String(encryptedText);
        return result;
    }

    public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
        KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA");
        kpGen.initialize(1024);
        KeyPair keyPair = kpGen.generateKeyPair();

        byte[] textCipher = asymmetricEncrypt("My name is Mukundi chitamba",keyPair.getPrivate());
        System.out.println(asymmetricDecrypt(textCipher, keyPair.getPublic()));
    }


}
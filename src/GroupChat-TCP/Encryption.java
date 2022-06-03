import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;

public class Encryption {

    /*Method takes in a secret key, a string to encrypt, and an ivSpec*/
    private static byte[] symmetricEncrypt(Key key, String text, IvParameterSpec ivSpec) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{

        //get IV
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        //encryption
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encryptedOutput = cipher.doFinal(text.getBytes());
        return encryptedOutput;
    }


    /*Method takes in a secret key, a byte array to decrypt, and an ivSpec */
    private static String symmetricDecrypt(Key key, byte[] text,IvParameterSpec ivSpec) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
        //get IV
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        //Decrypt
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        byte[] encryptedOutput = cipher.doFinal(text);
        String result = new String(encryptedOutput);
        return result;
        
    }

    /*Method takes in a string to encrypt and a private key */
    private static byte[] asymmetricEncrypt(String text,Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
        byte[] textByte = text.getBytes();
        //encrypt
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key); //private key or public key
        byte[] encryptedText = cipher.doFinal(textByte);
        return encryptedText;
    }

    /*Method takes in a byte array to decrypt and a public key */
    private static String asymmetricDecrypt(byte[] text,Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
        //dencrypt
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, key); //public key or private key
        byte[] encryptedText = cipher.doFinal(text);
        String result = new String(encryptedText);
        return result;
    }

    //Takes in a byte array of the message, and the private key to sign with 
    protected static byte[] addSignature(byte [] text, Key key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException{
        Signature sign = Signature.getInstance("SHA256withDSA");
        sign.initSign((PrivateKey)key);
        sign.update(text);
        byte[] signature = sign.sign();
        return signature;
    }

    private static boolean verifySignature(byte [] text, KeyPair keyPair) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException{
        Signature sign = Signature.getInstance("SHA256withDSA");
        sign.initSign((PrivateKey)keyPair.getPrivate());
        sign.update(text);
        byte[] signature = sign.sign();

        sign.initVerify(keyPair.getPublic());
        sign.update(text);  
        boolean bool = sign.verify(signature);
        return bool;
    }


    public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, SignatureException{
        KeyPairGenerator kpGen = KeyPairGenerator.getInstance("DSA");
        kpGen.initialize(2048);
        KeyPair keyPair = kpGen.generateKeyPair();
        

        byte[] textCipher = addSignature("My name is Mukundi chitamba".getBytes(),keyPair.getPrivate());
        System.out.println(verifySignature(textCipher, keyPair));
    }


}
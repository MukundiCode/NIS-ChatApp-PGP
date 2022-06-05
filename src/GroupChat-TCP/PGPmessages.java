import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class PGPmessages {
    private byte[] messageComponent;//message++hash attribute of the pgp message object
    private byte[] keyComponent;//session key attribute of the pgp message object

    /**
     * The constructor used to generate a new pgp message object
     * @param messageComponent plaintext componnent
     * @param keyComponent session key component
     */
    public PGPmessages(byte[] messageComponent, byte [] keyComponent){
        this.messageComponent = messageComponent;
        this.keyComponent = keyComponent;
    }

    public static PGPmessages sendMessage(String plainMessage, PrivateKey senderPrivate, PublicKey recipientPublicKey) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, SignatureException, IOException, InvalidAlgorithmParameterException {
        byte[] messageData = plainMessage.getBytes("UTF-8");//getting the bytes of the plainMessage
        byte[] shaSignature = Encryption.signedData(messageData, senderPrivate);//generating the SHA256withRSA signed hash
        //writing the message and signed hash byte arrays to a shared byte array
        byte[] messageConcat = new byte[messageData.length+shaSignature.length];//creating new byte array of the combined lengths of the previous arrays
        System.arraycopy(messageData, 0, messageConcat, 0, messageData.length);
        System.arraycopy(shaSignature, 0, messageConcat, messageData.length, shaSignature.length);
        byte [] compressedData = Compression.compress(messageConcat);//calling compression methods need to add methods to check for compression
        SecretKey secretKey = Encryption.secretKeyGeneration();//generating the secret shortlived key //MOVED
        //LINES 49-51 can be taken out once jono is done logging
        // System.out.println("ORIGINAL SECRET KEY");
        // System.out.println(Arrays.toString(secretKey.getEncoded()));
        //initialization vector generation to use for the symmetric cipher
		byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        byte [] encryptedMessage = Encryption.symmetricEncrypt(secretKey, compressedData, ivParameterSpec);//object attribute 1
        //lines 56-59 can be removed when logging is done
        // System.out.println("ENCRYPTED MESSAGE CONTENT");
        // System.out.println(Arrays.toString(encryptedMessage));
        // System.out.println(new String(encryptedMessage));
        byte [] encryptedSessionKey = Encryption.encryptSessionKey(recipientPublicKey, secretKey); //encrypting the session key //MOVE
        PGPmessages pgpTransmission = new PGPmessages(encryptedMessage, encryptedSessionKey);//calling the constructor to generate the transmission
        //lines 62-64 can be removed
        //System.out.println(Arrays.toString(pgpTransmission.messageComponent));
        //System.out.println(Arrays.toString(pgpTransmission.keyComponent));
        return pgpTransmission;
    }

    public static String receiveMessage(PGPmessages receivedMessage, PrivateKey recipientPrivateKey, PublicKey senderPublicKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException, SignatureException {
        //seperating the pgp transmission object into the message and key components
        byte[] encryptedmessageComponent = receivedMessage.messageComponent;
        byte[] keyComponent = receivedMessage.keyComponent;
        byte[] decryptedSessionKeyByte = Encryption.asymmetricDecrypt(keyComponent, recipientPrivateKey);//decrypting the session key using asymmetric decryption with the recipients private key as algorithm input
        SecretKey secretKeyRestructured = new SecretKeySpec(decryptedSessionKeyByte, 0, decryptedSessionKeyByte.length, "AES");//reconstruvted session key
        //Lines 74-76 can be removed after logging
        // System.out.println("RECONSTRUCTED SECRET KEY");
        // System.out.println(Arrays.toString(secretKeyRestructured.getEncoded()));
        byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);//iv for aes algorithm
        byte[] decryptedCompressedMessageComponent = Encryption.symmetricDecrypt(secretKeyRestructured, encryptedmessageComponent, ivParameterSpec);//decrypted compressed mesaage bytes
        byte[] decompressedMessageComponent = Compression.decompress(decryptedCompressedMessageComponent);//decompressed message bytes
        //lines 81-83 remove after logging
        // System.out.println("RECEIVED SIDE BYTE ARRAY");
        // System.out.println(Arrays.toString(decompressedMessageComponent));
        //calculation to split up the shared byte array into the message and signed hash components
        int byteArrayLength = decompressedMessageComponent.length;
        int signatureLength = 128;
        int messageEndIndex = byteArrayLength-signatureLength;
        // System.out.println(byteArrayLength);
        byte[] messageByteArray = Arrays.copyOfRange(decompressedMessageComponent, 0, messageEndIndex);
        byte[] signedHash = Arrays.copyOfRange(decompressedMessageComponent, messageEndIndex, byteArrayLength);
        //Can remove 91-94
        // String messageString = new String(messageByteArray);
        // System.out.println("The message part is HERE");
        // System.out.println(messageString);
        //Verification of the signatures
        Signature authCheck = Signature.getInstance("SHA256withRSA");
        authCheck.initVerify(senderPublicKey);
        authCheck.update(messageByteArray);
        //returns message if the signatures match
        if(authCheck.verify(signedHash)){
            return new String(messageByteArray);
            // System.out.println(new String(messageByteArray));
        }else{
            return "Invalid data was received";
        }
    }
        /*
         * MY LOGIC FOR SPLITTING SHARED BYTE ARRAY
         * Calculation that happens here is:
         * We calculate the total length of our byte array let that be T
         * We know the signature length is 512 let that be S
         * Our message component let that be M
         * M+S=T
         * T-S= M let this result be seperator index
         * MessageByteArray = concatArr.copy(0, seperator index)
         * SignatureByteArray = concatArr.copy(seperatorindex, lengthof byte array)
         * will return an array of the two seperate byte array
         */
    
     /**
      * This is a test method that can be removed later. I generated keys using Jonos RSA class
      * @throws NoSuchAlgorithmException
      * @throws InvalidKeyException
      * @throws NoSuchPaddingException
      * @throws IllegalBlockSizeException
      * @throws BadPaddingException
      * @throws SignatureException
      * @throws InvalidAlgorithmParameterException
      * @throws IOException
      */
     public static void test() throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, SignatureException, InvalidAlgorithmParameterException, IOException{
        RSA senderKeys = new RSA();
        PublicKey senderPublicKey = senderKeys.getPublic();
        PrivateKey senderPrivateKey = senderKeys.getPrivate();
        RSA recipientKeys = new RSA();
        PublicKey recipientPublicKey = recipientKeys.getPublic();
        PrivateKey recipientPrivateKey = recipientKeys.getPrivate();
        String plainMessage= "Hello nis assignment pgp component";
        PGPmessages sentMessage =sendMessage(plainMessage, senderPrivateKey, recipientPublicKey);
        String result = receiveMessage(sentMessage, recipientPrivateKey, senderPublicKey);
        System.out.println(result);
    }

    /**
     * main is not needed in this class
     * @param args
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws SignatureException
     * @throws InvalidAlgorithmParameterException
     * @throws IOException
     */
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, SignatureException, InvalidAlgorithmParameterException, IOException {
        test();
    } 
}
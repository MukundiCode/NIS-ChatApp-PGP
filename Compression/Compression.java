import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.zip.*;

/**
 * The Compression class is a utility class with methods used for the compression and decompression of the Hash and Message components.
 * The class also has methods to split the hash and message components from the transmitted message.
 */
public class Compression {
    /**
     * Appends the hash to the Message being transmitted. H+M will be the string that is compressed. Waiting to sign digital hash
     * @param message Original client message that is being sent
     * @return appended digitally signed hash to the client message
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws SignatureException
     */
    public static String messageConstructor(String message) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException, SignatureException{
        String hash = Hashing.computeHash(message);
        String constructedString = hash+"$#$"+message;;
        return constructedString;
    }

    
    /**
     * Compression method makes use of the deflator function
     * @param message Hash+Message to be compressed
     * @return Compressed Message
     * @throws IOException
     */
    public static byte[] compress(String message) throws IOException{
       try {
        byte [] data = message.getBytes();
        System.out.println("Original message byte length: "+data.length);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DeflaterOutputStream compresser = new DeflaterOutputStream(output);
        compresser.write(data);
        compresser.finish();
        byte [] outputArr = output.toByteArray();
        return outputArr;   
       } 
       catch (Exception e) {
           System.out.println(e);
       }
    return null;
    }

   
    /**
     * Decompress the compressed message. Has to be lossless
     * @param compressedByteArr byte array of compressed message
     * @return decompressed byte array
     * @throws IOException
     */
    public static byte[] decompress(byte[] compressedByteArr) throws IOException{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InflaterOutputStream inflater = new InflaterOutputStream(out);
        inflater.write(compressedByteArr);
        inflater.finish();
        return out.toByteArray();
    }

    /**
     * The method retrieves the hash and Message component. $#$ was used to split up the hash and message component as SHA-512 does not use those characters
     * @param appendedProtocolString H+M with $#$ split
     * @return Array with hash in position 0 and message in position 1
     */
    public static String [] messageSplit(String appendedProtocolString){
        String[] components = new String[2];
        int i = appendedProtocolString.indexOf("$#$");
        components[0] = appendedProtocolString.substring(0, i);
        components[1] = appendedProtocolString.substring(i+3, appendedProtocolString.length());
        return components;
    }



    /**
     * Demo of the methods not needed in final handin
     * @param args
     * @throws IOException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws SignatureException
     */
    public static void main(String[] args) throws IOException, InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        String message = messageConstructor("hello my nis group assignemnet is about a pgp crtptosystem i am trying to do compression with java inflate & deflate");
        byte [] compressed = compress(message);
        System.out.println("Compressed message byte length: "+compressed.length);
        byte [] decompressed = decompress(compressed);
        System.out.println(Arrays.equals(message.getBytes(), decompressed));
        String[] components = messageSplit(new String(decompressed));
        System.out.println("The hash component in hex representation: "+ components[0]);
        System.out.println("Decompressed message component: "+ components[1]);
        if (Hashing.verifyHash(components[0], components[1])){
            System.out.println("Computed hash matches received hash");
        }
        else{
            System.out.println("Computed hash does not match received hash");
        }
    }
}
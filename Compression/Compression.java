import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.zip.*;

public class Compression {
    public static String messageConstructor(String message) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException, SignatureException{
        String hash = Hashing.computeHash(message);
        String constructedString = hash+"$#$"+message;;
        return constructedString;
    }

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

    public static byte[] decompress(byte[] compressedByteArr) throws IOException{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InflaterOutputStream inflater = new InflaterOutputStream(out);
        inflater.write(compressedByteArr);
        inflater.finish();
        return out.toByteArray();
    }

    public static String [] messageSplit(String appendedProtocolString){
        String[] components = new String[2];
        int i = appendedProtocolString.indexOf("$#$");
        components[0] = appendedProtocolString.substring(0, i);
        components[1] = appendedProtocolString.substring(i+3, appendedProtocolString.length());
        return components;
    }



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
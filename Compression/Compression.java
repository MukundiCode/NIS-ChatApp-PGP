import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.*;

public class Compression {
    //TO:DO stringbuilder function for digital signed hash and generating hashes then incoporating into compression

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

    public static void main(String[] args) throws IOException {
        String message = "hello my nis group assignemnet is about a pgp crtptosystem i am trying to do compression with java inflate & deflate";
        byte [] compressed = compress(message);
        System.out.println("Compressed message byte length: "+compressed.length);
        byte [] decompressed = decompress(compressed);
        System.out.println(Arrays.equals(message.getBytes(), decompressed));
    }
    
}

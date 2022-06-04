import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.security.*;
import java.math.BigInteger;
import java.security.cert.*;
import java.util.*;
import java.io.*;

// classes using for certificates X500Name, X509CertificateHolder, JcaX509CertificateHolder, X509v3CertificateBuilder, SubjectPublicKeyInfo, JcaContentSignerBuilder
public class Certificates {
    
    private static PrivateKey CAPrivateKey;
    private static PublicKey CAPublicKey;
    public static KeyStore keyStore;

    // generates CA certificate
    public static X509Certificate generateCACertificate(RSA keyPair) throws CertificateException, OperatorCreationException, NoSuchAlgorithmException, KeyStoreException, IOException {
        CAPrivateKey = keyPair.getPrivate();
        CAPublicKey = keyPair.getPublic();
        Calendar certExpireDate = Calendar.getInstance();
        certExpireDate.add(Calendar.YEAR, 1);

        X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(
                new X500Name("CN=CA Root Certificate"),
                BigInteger.ONE,
                new Date(),
                certExpireDate.getTime(),
                new X500Name("CN=CA Root Certificate"),
                SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded())
        );

        X509CertificateHolder CAcertHolder = certificateBuilder.build(new JcaContentSignerBuilder("SHA1withRSA").build(keyPair.getPrivate()));
        X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(CAcertHolder);
        createKeyStore(certificate);

        return certificate;
    }

    // generate client certificate & sign it with CA private key
    public static X509Certificate generateClientCertificate(PublicKey clientKey, String clientName) throws CertificateException, OperatorCreationException, KeyStoreException, NoSuchAlgorithmException, FileNotFoundException, IOException {

        Calendar certExpireDate = Calendar.getInstance();
        certExpireDate.add(Calendar.YEAR, 1);

        X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(
                new X500Name("CN="+clientName),
                BigInteger.ONE,
                new Date(),
                certExpireDate.getTime(),
                new X500Name("CN="+clientName),
                SubjectPublicKeyInfo.getInstance(clientKey.getEncoded())
        );

        X509CertificateHolder certHolder = certificateBuilder.build(new JcaContentSignerBuilder("SHA1withRSA").build(CAPrivateKey));
        X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(certHolder);
        storeCertificate(certificate, clientName);

        return certificate;
    }

    public static byte[] convertCertToByte(X509Certificate certificate) throws CertificateEncodingException {
        return certificate.getEncoded();

    }

    public static X509Certificate convertByteToCert(byte[] byteCertArray) throws CertificateException {
        InputStream stream = new ByteArrayInputStream(byteCertArray);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        
        return (X509Certificate) cf.generateCertificate(stream);
    }

    // validates client certificate with CA to determine if public key belongs to client
    public static boolean validateCertificate(X509Certificate certificate, PublicKey CAPublicKey){
        
        try {
            certificate.verify(CAPublicKey);
            return true;
        } 
        catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException e) {
            System.out.println("certificate rejected");
        } catch (SignatureException ex) {
            System.out.println("certificate rejected");
        }
        return false;
        
    }

    private static void createKeyStore(X509Certificate certificate) throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException {
        File file = new File("KeyStore.pkcs12");
        keyStore.load(null, null);

        keyStore.setCertificateEntry("caRootCertificate", certificate);
        keyStore.store(new FileOutputStream(file), "123".toCharArray());
    }


    private static void storeCertificate(X509Certificate certificate, String clientName) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException {
        keyStore.setCertificateEntry(clientName, certificate);
        keyStore.store(new FileOutputStream("KeyStore.pkcs12"), "123".toCharArray());
    }

    public static PublicKey getPublicKeyFromKeyStore(String clientName) throws KeyStoreException{
        return keyStore.getCertificate(clientName).getPublicKey();
    }

    public static void main(String[] args) {
        System.out.println("compiles");
    }

}

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

public class Certificates {
    
    private static PrivateKey CAPrivateKey;
    private static PublicKey CAPublicKey;

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
        //createKeyStore(certificate);

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
        //storeCertificate(certificate, clientName);

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
    public static boolean validateCertificate(X509Certificate certificate){
        
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

    public static PublicKey getPublicKeyFromCertificate(X509Certificate clientCert) {
        return clientCert.getPublicKey();
    }


    /*
     * to compile Certifcates class use command: javac -cp .:1.jar:2.jar:3.jar:4.jar:5.jar:6.jar Certificates.java
     * to run Certificates class use command: java -cp .:1.jar:2.jar:3.jar:4.jar:5.jar:6.jar Certificates
     */
    public static void main(String[] args) throws CertificateException, OperatorCreationException, NoSuchAlgorithmException, KeyStoreException, IOException {
        // create and generate Server/CA key pair and root certificate
        RSA keyPair = new RSA();
        X509Certificate caCert = generateCACertificate(keyPair);
        System.out.println("CA certificate: "+caCert.toString());
        System.out.println("CA Public Key: "+caCert.getPublicKey());
        // create and client1 key pair and signed certificate
        RSA keyPairClient1 = new RSA();
        X509Certificate c1Cert = generateClientCertificate(keyPairClient1.getPublic(), "client1");
        System.out.println("Client 1 certificate: "+c1Cert.toString());
        // create and client2 key pair and signed certificate
        RSA keyPairClient2 = new RSA();
        X509Certificate c2Cert = generateClientCertificate(keyPairClient2.getPublic(), "client2");
        System.out.println("Client 2 certificate: "+c2Cert.toString());
        // validate each client certificate with CA Public Key
        boolean client1Validate = validateCertificate(c1Cert);
        System.out.println("Client 1 certificate validate: "+client1Validate);
        boolean client2Validate = validateCertificate(c2Cert);
        System.out.println("Client 2 certificate validate: "+client2Validate);
    }

}

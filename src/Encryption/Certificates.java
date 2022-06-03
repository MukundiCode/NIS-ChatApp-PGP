import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public class Certificates {
    
    private PrivateKey CAPrivateKey;
    private PublicKey CAPublicKey;

    public X509Certificate generateCACertificate(KeyPair keys) throws CertificateException {
        CAPrivateKey = keys.getPrivate();
        Calendar certExpireDate = Calendar.getInstance();
        certExpireDate.add(Calendar.YEAR, 1);

        X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(
                new X500Name("CN=CA Root Certificate"),
                BigInteger.ONE,
                new Date(),
                certExpireDate.getTime(),
                new X500Name("CN=CA Root Certificate"),
                SubjectPublicKeyInfo.getInstance(keys.getPublic().getEncoded())
        );

        X509CertificateHolder CAcertHolder = certificateBuilder.build(new JcaContentSignerBuilder("SHA1withRSA").build(keys.getPrivate()));
        X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(CAcertHolder);

        return certificate;
    }

    public X509Certificate generateClientCertificate(PublicKey clientKey, String clientName) throws CertificateException {

        Calendar certExpireDate = Calendar.getInstance();
        certExpireDate.add(Calendar.YEAR, 1);

        X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(
                new X500Name("CN="+clientName),
                BigInteger.ONE,
                new Date(),
                certExpireDate.getTime(),
                new X500Name("CN="+clientName),
                SubjectPublicKeyInfo.getInstance(keys.getPublic().getEncoded())
        );

        X509CertificateHolder certHolder = certificateBuilder.build(new JcaContentSignerBuilder("SHA1withRSA").build(CAPrivateKey));
        X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(certHolder);

        return certificate;
    }

    public byte[] convertCertToByte(X509Certificate certificate) {
        return certificate.getEncoded();

    }

    public X509Certificate convertByteToCert(bytes[] byteCertArray) {
        InputStream stream = new ByteArrayInputStream(byteCertArray);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        
        return (X509Certificate) cf.generateCertificate(stream);
    }

    public boolean validateCertificate(X509Certificate certificate, PublicKey CAPublicKey){
        
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

    public PublicKey getClientPublicKey(X509Certificate certificate) {
        return certificate.getPublicKey();
    }
}

import java.security.*;
import java.security.spec.X509EncodedKeySpec;

public class PKI {
    static String[] pk = {
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhpIBAofbjpEHwjcnbQOd7Hh0xbMmxsJcBMLqHJrRT7z4JGjEcqSKUZUWp8+z+g9kh1ck2Tm1SOXa5JsKGTcLvMpsfwbwCS9zYqr0xm21pvm8p+0ccZrqAc7oWPQk8eVomC2yCGEms2B1IDe5FN7rt0s6sJURweaxTl6rh6sSkJ4+Tg0mbmlbsrDjjWY/WxMhfWD1yI7A4vLru7R3QXdP3eLOGkkufngPv5/2fl5GO+LBTLxDzLI3hcoNa6zkasJU14lj74SVgGLCzvpj4Hsq58iSzUndakuBbPnUJj/THHkIImOubXdwLM7v71GBGlpzGcELgyWt/rBMEI6o2koJ9wIDAQAB",
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgs/hVjU4f3AxtNtYXsr7pnT9J1fVsL8AcIyx/TOoaneW9/u4dnSWUBIPZtdJ/TKu3t9NUopmCCGA4Z7KwWliACaGZMNS3Kmi87ZNXNsc5aaHMh+C6VfVf6Gj4KXMLC7co1IBZ6Wcx89VEgCYGKxy5I8XQdHH3TRpCWNfZ1LDq0QPKPC2RC/GArFeK9XNYtu/gMKiKDKHZgvHJmkzbaWhSXvg/VWTQ6IejUWs+MqPdivX9j4h/+mcywIHggJF1Sb32jOI7Y6bMgreke8eszYGQs1bPfM7dIgfFrZzWgltMSc3JDFdHwvKjBB5IuCo8JIXBbhI4IpEJvaoepKFMXg12wIDAQAB"
        };
    static String[] sk = {
        "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"
        };
    static KeyPair[] kp;
    
    public static PrivateKey getAkt(int tokenVersion) {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            byte[] akBytes = Base64.decode(sk[tokenVersion]);
            X509EncodedKeySpec x = new X509EncodedKeySpec(akBytes);
            PrivateKey sk = kf.generatePrivate(x);
            return sk;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static PublicKey getVkt(int tokenVersion) {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            byte[] akBytes = Base64.decode(pk[tokenVersion]);
            X509EncodedKeySpec x = new X509EncodedKeySpec(akBytes);
            PublicKey pk = kf.generatePublic(x);
            return pk;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static KeyPair genKeyPair() {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(2048);
            return keyPairGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}

import javax.crypto.KeyAgreement;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;

public class Token {
    public String name;    // 8 bits
    private String pinH;
    private String pt;
    private int n = 8;
    private int m = 3;
    
    public Token(String name) {
        this.name = name;
    }

    boolean setup() {
        System.out.println("\nSetup start!");
        try {
            KeyPair keyPairA = generateKeyPair();
            KeyAgreement kaA = KeyAgreement.getInstance("ECDH");
            kaA.init(keyPairA.getPrivate());
            PublicKey publicKeyA = keyPairA.getPublic();
            String pbkAstr = Base64.encodeBytes(publicKeyA.getEncoded());

            // 接收client的公钥、pin、协同密钥
            // 验证ECDH，若验证通过则返回 (K + client生成的pin)，否则返回null
            String rst = Client.requestSetup(pbkAstr);
            String KPin = verify(kaA, rst, 6);
            String pin = KPin.substring(44);

            if (pin != null) {
                System.out.println("Token verified ECDH in setup.");
                storePin(pin);
                return true;
            } else {
                System.out.println("Risk of tampering, terminated.");
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    boolean bind() {
        System.out.println("\nBind start!");
        try {
            KeyPair keyPairA = generateKeyPair();
            KeyAgreement kaA = KeyAgreement.getInstance("ECDH");
            kaA.init(keyPairA.getPrivate());
            PublicKey publicKeyA = keyPairA.getPublic();
            String pbkAstr = Base64.encodeBytes(publicKeyA.getEncoded());

            // 接收client的公钥、pinH、协同密钥
            // 验证ECDH，若验证通过则返回(K + client计算的pinH)，否则返回null
            String rst = Client.requestBind(pbkAstr);
            String KPinH = verify(kaA, rst, 6);
            String pinH = KPinH.substring(44);

            if (pinH != null) {
                System.out.println("Token verified ECDH in bind.");
                if (pinH.equals(this.pinH)) {
                    System.out.println("Token bound.");

                    // 把绑定信息返回给client
                    String pt = generatePt();    // 128 bits
                    String kABstr = KPinH.substring(0, 44);
                    String decrypted = AESUtil.encrypt(pt + kABstr);    // 236 bits
                    String bindInfo = decrypted + name;
                    Client.verifyBind(bindInfo);

                    return true;
                } else {
                    System.out.println("Wrong PIN!");
                    System.out.println("your pinH: " + pinH + "\nthis.pinH: " + this.pinH);
                    return false;
                }
            } else {
                System.out.println("Risk of tampering, terminated.");
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    boolean validate(String message) {
        String t = message.substring(0, 128);    // len取决于HMAC
        String M = message.substring(128);
        String tCal = generateT(M, pt);

        if (t.equals(tCal)) {
            return true;
        }else {
            return false;
        }
    }

    private String verify(KeyAgreement kaA, String rst, int len) {
        try {
            String pkBstr = rst.substring(0, 124);
            String encrypted = rst.substring(124);
            String decrypted = AESUtil.decrypt(encrypted);
            String pin = decrypted.substring(0, len);    // pin or pinH
            String kBAstr = decrypted.substring(len);

            byte[] pkBbyte = Base64.decode(pkBstr);
            KeyFactory kf = KeyFactory.getInstance("EC");
            X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(pkBbyte);
            PublicKey pkB = kf.generatePublic(pkSpec);

            kaA.doPhase(pkB, true);
            byte[] kAB = kaA.generateSecret();
            String kABstr = Base64.encodeBytes(kAB);    // 44 bits

            String K = generateK(kABstr);

            if (kABstr.equals(kBAstr)) {
                return K + pin;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String generateT(String M, String pt) {
        String t = pt + M;
        return t;
    }

    private String generatePt() {
        // 还没有去重操作（）
        final String chars = "01";

        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 128; i++) {
            int randomIndex = random.nextInt(chars.length());
            sb.append(chars.charAt(randomIndex));
        }

        String pt = sb.toString();
        this.pt = pt;
        return pt;
    }

    private String generateK(String kABstr) {
        String K = kABstr;
        return K;
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
            keyGen.initialize(ecSpec);

            KeyPair keyPairA = keyGen.generateKeyPair();
            return keyPairA;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean storePin(String pin) {
        // 根据pin生成pinH
        String pinH = pin;


        this.pinH = pinH;
        System.out.println("Token finished setup.");
        System.out.println("PIN: " + pin);
        return true;
    }
    
    private String rResponse(String ids, String uid, String hr){
        // if (not setup) {setup}
        // if (not bound) {bound}
        
        String[] kg = keyGen();
        String pk = kg[0];
        String sk = kg[1];
        int n = 0;
        String cid = Utils.getRandom01(128);
        StringBuilder ad = new StringBuilder();
        ad.append(Utils.SHA256(ids));
        ad.append(n);
        ad.append(cid);
        ad.append(pk);
        // sign
        // store the information into database
        return /*here should not be ad*/ ad.toString();
    }
    
    private static String[] keyGen(){
        String[] ans = new String[2];
        //ans[0] = pk;
        //ans[1] = sk;
        return ans;
    }
}

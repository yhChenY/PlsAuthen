import javax.crypto.KeyAgreement;
import java.io.IOException;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;

public class Token {
    public String name;    // 8 bits
    private String pinH;
    private String pt;
    private int n = 8;
    private int m = 3;
    public int tokenVersion;

    public Token(String name, int version) {
        this.name = name;
        tokenVersion = version;
    }

    boolean setup() {
        System.out.println("\nSetup start!");
        try {
            KeyPair keyPairA = generateKeyPair();
            KeyAgreement kaA = KeyAgreement.getInstance("ECDH");
            kaA.init(keyPairA.getPrivate());
            PublicKey publicKeyA = keyPairA.getPublic();
            String pkAstr = Base64.encodeBytes(publicKeyA.getEncoded());
            System.out.println("pkAstr: " + pkAstr);

            // 接收client的公钥、pin、协同密钥
            // 验证ECDH，若验证通过则返回 (K + client生成的pin)，否则返回null
            String rst = Client.requestSetup(pkAstr);
            String KPin = verify(kaA, rst, 6);

            if (KPin != null) {
                String pin = KPin.substring(44);
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

            if (KPinH != null) {
                String pinH = KPinH.substring(44);
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
        } else {
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
            System.out.println("kBAstr: " + kBAstr);

            byte[] pkBbyte = Base64.decode(pkBstr);
            KeyFactory kf = KeyFactory.getInstance("EC");
            X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(pkBbyte);
            PublicKey pkB = kf.generatePublic(pkSpec);

            kaA.doPhase(pkB, true);
            byte[] kAB = kaA.generateSecret();
            String kABstr = Base64.encodeBytes(kAB);    // 44 bits
            System.out.println("kABstr: " + kABstr);

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

    public Object[] rResponse(String ids, String uid, String hr) {
        // if (not setup) {setup}
        // if (not bound) {bound}
        String[] kg = keyGen();
        String pk = kg[0];
        String sk = kg[1];
        int n = 0;
        String cid = Utils.getRandom01(128);
        StringBuilder adBuilder = new StringBuilder();
        adBuilder.append(Utils.SHA256(ids));
        adBuilder.append(Utils.intToBinStr(n));
        adBuilder.append(cid);
        adBuilder.append(pk);
        String ad = adBuilder.toString();
        // ad is always 64 + 32 + 128 + 392 = 616 length
        String signData = ad + hr;
        // sign
        byte[] sig = null;
//        try{
//            sig = Utils.sign(Base64.decode(signData),PKI.getAkt(tokenVersion));
//        }catch (IOException e){
//            e.printStackTrace();
//        }
        // store the information into database
        DatabaseOp db = new DatabaseOp();
        db.getConnection();
        db.insertTokenCredential(name, ids, cid, n, sk, pk);
        db.closeConnection();
        Object[] ans = new Object[2];
        ans[0] = ad;
        ans[1] = sig;
        return ans;
    }

    public String aResponse(String ids, String hr) {
        // select from database according to ids
        DatabaseOp db = new DatabaseOp();
        db.getConnection();
        String cid = db.selectCid(name, ids);
        int n = db.selectN(name, ids);
        n++;
        //ad
        StringBuilder ad = new StringBuilder();
        ad.append(Utils.SHA256(ids));
        ad.append(Utils.intToBinStr(n));
        ad.append(cid);
        //sign

        // update credential info
        db.updateN_token(name, ids, n);
        db.closeConnection();
        //send
        return ad.toString();
    }

    public static String[] keyGen() {
        String[] ans = new String[2];
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
//            RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(2048,RSAKeyGenParameterSpec.F4);
            keyPairGen.initialize(2048);
            KeyPair keyPair = keyPairGen.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();
            ans[0] = Base64.encodeBytes(publicKey.getEncoded());
            ans[1] = Base64.encodeBytes(privateKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return ans;
    }
}

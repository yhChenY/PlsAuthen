import javax.crypto.*;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;

public class Test {
    public static void main(String[] args) {
        boolean setup = tokenSetup();

    }

    static String getPwd(String pin) {
        DatabaseOp dbOp = new DatabaseOp();
        dbOp.getConnection();

        String sql = "select pwd from test where pin = '" + pin + "'";
        String pwd = dbOp.select(sql);

        dbOp.closeConnection();
        return pwd;
    }

    static void storeTest(byte[] pin, byte[] pwd) {
        DatabaseOp dbOp = new DatabaseOp();
        dbOp.getConnection();

        String pinStr = numBytesToString(pin);
        String pwdStr = numBytesToString(pwd);
        String statement = "insert into test (pin, pwd)";
        String[] para = {pinStr, pwdStr};
        dbOp.insert(statement, para);

        dbOp.closeConnection();
    }

    static boolean tokenSetup() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
            keyGen.initialize(ecSpec);

            // A生成自己的私密信息，把公钥发给B
            KeyPair keyPairA = keyGen.generateKeyPair();
            KeyAgreement kaA = KeyAgreement.getInstance("ECDH");
            kaA.init(keyPairA.getPrivate());
            PublicKey publicKeyA = keyPairA.getPublic();
            String pbkAStr = Base64.encodeBytes(publicKeyA.getEncoded());

            // B生成自己的私密信息，返回公钥、用CBC算法加密过的userPIN和对称密钥
            String rst = clientSetup(pbkAStr);
            String pkBStr = rst.substring(0, 124);
            String encrypted = rst.substring(124);

            // A收到B发送过来的公开信息和对称密钥，解密
            byte[] pkBByte = Base64.decode(pkBStr);
            KeyFactory kf = KeyFactory.getInstance("EC");
            X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(pkBByte);
            PublicKey pkB = kf.generatePublic(pkSpec);

            String decrypted = AESUtil.decrypt(encrypted);
            String pin = decrypted.substring(0, 6);
            String kBAstr = decrypted.substring(6);

            // A将收到的对称密钥和自己计算出的对称密钥进行比较
            kaA.doPhase(pkB, true);
            byte[] kAB = kaA.generateSecret();
            String kABstr = Base64.encodeBytes(kAB);

            if (kABstr.equals(kBAstr)) {
                System.out.println("verified");
                System.out.println("pin: " + pin);
                System.out.println("DH: " + kABstr);
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    static String clientSetup(String pkAStr) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
            keyGen.initialize(ecSpec);

            // B生成自己的私密信息
            KeyPair keyPairB = keyGen.generateKeyPair();
            KeyAgreement kaB = KeyAgreement.getInstance("ECDH");
            kaB.init(keyPairB.getPrivate());
            PublicKey publicKeyB = keyPairB.getPublic();
            String pkBStr = Base64.encodeBytes(publicKeyB.getEncoded());    // 124 bits

            // B把A发送过来的公用信息转换成公钥
            byte[] pkAbyte = Base64.decode(pkAStr);
            KeyFactory kf = KeyFactory.getInstance("EC");
            X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(pkAbyte);
            PublicKey pkA = kf.generatePublic(pkSpec);

            // B计算出对称密钥
            kaB.doPhase(pkA, true);
            byte[] kBA = kaB.generateSecret();
            String kBAstr = Base64.encodeBytes(kBA);

            // 获取userPIN
            String pin = generatePin(6);

            String src = pin + kBAstr;
            String encrypted = AESUtil.encrypt(src);
            String rst = pkBStr + encrypted;

//            System.out.printf("pkB: %s\npin: %s\nkBA: %s\nencrypted: %s\n", pkBStr, pin, kBAstr, encrypted);
            return rst;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String generatePin(int len) {
        // ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789
        final String chars = "0123456789";

        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < len; i++) {
            int randomIndex = random.nextInt(chars.length());
            sb.append(chars.charAt(randomIndex));
        }
        return sb.toString();
    }

    static String numBytesToString(byte[] bytes) {
        String string = "";
        for (int i = 0; i < bytes.length; i++) {
            string += Integer.valueOf(bytes[i]).toString();
        }
        return string;
    }

}

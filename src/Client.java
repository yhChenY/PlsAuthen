import javax.crypto.KeyAgreement;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;

/**
 * CTAP中Token和Client都没写哈希H()（即K目前等于协同密钥），而且Client可能还没写generateK方法
 * verifyBind没有验证椭圆曲线加密（即K）
 * Validate过程没写内容（即generateT方法）
 */

public class Client {
    int clientId;

    static String requestSetup(String pkAStr) {
        try {
            // 获取pin
            String pin = generatePin(6);
            // 返回公钥、加密（pin、对称密钥）
            return generateRst(pin, pkAStr);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static String requestBind(String pkAStr) {
        // 获取用户输入的pin
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please enter your PIN: ");
        String pin = scanner.nextLine();
        scanner.close();
        // 根据pin生成pinH
        String pinH = generatePinH(pin);

        // 返回公钥、加密（pinH、对称密钥）
        String rst = generateRst(pinH, pkAStr);
        System.out.println("client return: " + rst);
        return rst;
    }

    static void verifyBind(String bindInfo) {
        try {
            String encryped = bindInfo.substring(0, 236);
            String token = bindInfo.substring(236);
            String decrypted = AESUtil.decrypt(encryped);
            String pt = decrypted.substring(0, 128);
            String kABstr = decrypted.substring(128);

            // 还没有验证椭圆曲线加密


//            System.out.println("token: " + token);
//            System.out.println("pt: " + pt);
            storeNamePt(token, pt);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void authorize(String M, Token token) {
        System.out.println("\nAuthorize start!");
        String name = token.name;
        System.out.println("Search pt of token: " + token);
        String pt = getPt(name);
        if (pt.length() == 0) {
            System.out.println("Not trusted client!");
        } else {
            System.out.println("client found pt: " + pt);
            String t = generateT(M, pt);
            boolean b = token.validate(t + M);

            if (b) {
                System.out.println("Allowed!");
            } else {
                System.out.println("Rejected!");
            }
        }
    }

    private static String generateT(String M, String pt) {
        String t = pt + M;
        return t;
    }

    private static String generatePinH(String pin) {
        String pinH = pin;
        return pinH;
    }

    private static String generateRst(String pin, String pkAStr) {
        String key = generateKey(pkAStr);
        String pkBstr = key.substring(0, 124);
//        System.out.println("pkBstr: " + pkBstr);
        String kBAstr = key.substring(124);
//        System.out.println("kBAstr: " + kBAstr);

        String src = pin + kBAstr;
        String encrypted = AESUtil.encrypt(src);
//        System.out.println("encrypted: " + encrypted);
        String rst = pkBstr + encrypted;
        return rst;
    }

    private static String generateKey(String pkAStr) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
            keyGen.initialize(ecSpec);

            KeyPair keyPairB = keyGen.generateKeyPair();
            KeyAgreement kaB = KeyAgreement.getInstance("ECDH");
            kaB.init(keyPairB.getPrivate());

            // 公钥
            PublicKey publicKeyB = keyPairB.getPublic();
            String pkBstr = Base64.encodeBytes(publicKeyB.getEncoded());    // 124 bits

            // 对称密钥
            byte[] pkAbyte = Base64.decode(pkAStr);
            KeyFactory kf = KeyFactory.getInstance("EC");
            X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(pkAbyte);
            PublicKey pkA = kf.generatePublic(pkSpec);

            kaB.doPhase(pkA, true);
            byte[] kBA = kaB.generateSecret();
            String kBAstr = Base64.encodeBytes(kBA);

            return pkBstr + kBAstr;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String generatePin(int len) {
        // ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789
        final String chars = "0123456789";

        SecureRandom random = new SecureRandom();
        StringBuilder sb;
        String pin;

        do {
            sb = new StringBuilder();
            for (int i = 0; i < len; i++) {
                int randomIndex = random.nextInt(chars.length());
                sb.append(chars.charAt(randomIndex));
            }
            pin = sb.toString();
        } while (isPinDuplicated(pin));

        storePin(pin);

        return pin;
    }

    private static boolean isPinDuplicated(String pin) {
        DatabaseOp dbOp = new DatabaseOp();
        dbOp.getConnection();

        String sql = "select count(*) from pin where pin = '" + pin + "'";
        int cnt = Integer.parseInt(dbOp.select(sql, "count"));

        dbOp.closeConnection();

        if (cnt == 0) {
            System.out.println("New pin is not duplicated, nice");
            return false;
        } else {
            System.out.println("New pin duplicated, will auto re-generate");
            return true;
        }
    }

    private static void storePin(String pin) {
        DatabaseOp dbOp = new DatabaseOp();
        dbOp.getConnection();

        String statement = "insert into pin (pin)";
        String[] para = {pin};
        dbOp.insert(statement, para, 1);

        dbOp.closeConnection();

        System.out.println("Client bound.");
    }

    private static void storeNamePt(String token, String pt) {
        DatabaseOp dbOp = new DatabaseOp();
        dbOp.getConnection();

        String statement = "insert into client_lib (token, pt)";
        String[] para = {token, pt};
        dbOp.insert(statement, para, 2);

        dbOp.closeConnection();

        System.out.println("Client bound.");
    }

    private static String getPt(String token) {
        DatabaseOp dbOp = new DatabaseOp();
        dbOp.getConnection();

        String sql = "select pt from client_lib where token = '" + token + "'";
        String pt = dbOp.select(sql, "pt");

        dbOp.closeConnection();
        return pt;
    }

    public void register(Token token, String intendedIds, Server server) {
        String ch = server.rChallenge();
        String uid = ch.substring(0, 512);
        String r = ch.substring(512, 512 + 128);
        String ids = ch.substring(640);
        if (!ids.equals(intendedIds)) {
            System.out.println("Current server is not matched with intended server, registration stopped");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(uid);
            sb.append(Utils.SHA256(r));
            sb.append(ids);
            //??
            Object[] res = token.rResponse(ids, uid, Utils.SHA256(r));
            // store info
            boolean ans = server.rCheck(res);
            if (ans) {
                System.out.println("Register successfully");
            } else {
                System.out.println("Register failed");
            }
        }
    }

    public void login(Token token, String intendedIds, Server server) {
        String ch = server.aChallenge();
        String r = ch.substring(0, 128);
        String ids = ch.substring(128);
        if (!ids.equals(intendedIds)) {
            System.out.println("Current server is not matched with intended server, authentication stopped");
        } else {
            StringBuilder sb = new StringBuilder();
            String hr = Utils.SHA256(r);
            sb.append(hr);
            sb.append(ids);

            String res = token.aResponse(ids, hr);
            String ans = server.aCheck(res);
            if (ans.equals("FAILED")) {
                System.out.println("Log-in Failed");
            } else {
                System.out.println("NOW Log-in: " + ans);
            }
        }
    }
}

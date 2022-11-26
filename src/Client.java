import javax.crypto.KeyAgreement;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;


public class Client {
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
        return generateRst(pinH, pkAStr);
    }

    static void verifyBind(String bindInfo) {
        try {
            String encryped = bindInfo.substring(0, 236);
            String token = bindInfo.substring(236);
            String decrypted = AESUtil.decrypt(encryped);
            String pt = decrypted.substring(0, 128);
            String kABstr = decrypted.substring(128);

            // 还没有验证K


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
        String pt = getPt(name);
        String t = generateT(M, pt);
        boolean b = token.validate(t + M);

        if (b) {
            System.out.println("Allowed!");
        } else {
            System.out.println("Rejected!");
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
        String kBAstr = key.substring(124);

        String src = pin + kBAstr;
        String encrypted = AESUtil.encrypt(src);
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
        // 还没有去重操作（）
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

    private static void storeNamePt(String token, String pt) {
        DatabaseOp dbOp = new DatabaseOp();
        dbOp.getConnection();

        String statement = "insert into client_lib (token, pt)";
        String[] para = {token, pt};
        dbOp.insert(statement, para);

        dbOp.closeConnection();

        System.out.println("Client bound.");
    }

    private static String getPt(String token) {
        DatabaseOp dbOp = new DatabaseOp();
        dbOp.getConnection();

        String sql = "select pt from client_lib where token = '" + token + "'";
        String pt = dbOp.select(sql);

        dbOp.closeConnection();
        return pt;
    }
    
    public void register(Token token, String intendedIds,Server server){
        String ch = server.rChallenge();
        String uid = ch.substring(0,512);
        String r = ch.substring(512,512+128);
        String ids = ch.substring(640);
        if(!ids.equals(intendedIds)){
            System.out.println("Current server is not matched with intended server, registration stopped");
        }else {
            StringBuilder sb = new StringBuilder();
            sb.append(uid);
            sb.append(Utils.SHA256(r));
            sb.append(ids);
            //??
            String res = token.rResponse(ids,uid,Utils.SHA256(r));
            // store info
            boolean ans = server.rCheck(res);
            if(ans){
                System.out.println("Register successfully");
            }else{
                System.out.println("Register failed");
            }
        }
    }
    
    
    
}

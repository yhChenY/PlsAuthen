import java.io.IOException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;

public class CTAP2Main {
    public static void main(String[] args) {
        Token token = new Token("000005",0);
//        token.setup();
//        token.bind();
//        Client.authorize("", token);
        Client client = new Client();
        Server server = new Server();
        server.ids = "test.com";
        client.clientId = 1;
        client.register(token,"test.com",server);
        client.login(token,"test.com",server);
//        String pk;
//        String sk;
//        try{
//            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
//            RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(2048,RSAKeyGenParameterSpec.F4);
//            keyPairGen.initialize(spec);
//            KeyPair keyPair = keyPairGen.generateKeyPair();
//            PublicKey publicKey = keyPair.getPublic();
//            PrivateKey privateKey = keyPair.getPrivate();
//            pk = Base64.encodeBytes(publicKey.getEncoded());
//            sk = Base64.encodeBytes(privateKey.getEncoded());
//            System.out.println(pk);
//            System.out.println(pk.length());
//            System.out.println(sk);
//            System.out.println(sk.length());
//        }catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e){
//            e.printStackTrace();
//        }

    }
}

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Utils {
    public static String getRandom01(int length){
        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        for(int i = 0;i<length;i++){
            sb.append(r.nextInt(2));
        }
        return sb.toString();
    }
    
    public String SHA256(String s){
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        byte[] ans = null;
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(bytes);
            ans = md.digest();
        } catch (NoSuchAlgorithmException exception) {
            exception.printStackTrace();
        }
        return byteToHex(ans);
    }
    
    private static String byteToHex(byte[] a){
        StringBuilder sb = new StringBuilder();
        for (byte b : a) {
            String tmp = Integer.toHexString(b & 0xff);
            // & 0xff becauese (int)byte would do sign-extension
            if (tmp.length() == 1) {
                sb.append("0");
                // 1 byte = 8 bit , so tmp shall be of length 2, if it's 1, 0 shall be located before
            }
            sb.append(tmp);
        }
        return sb.toString();
    }
}

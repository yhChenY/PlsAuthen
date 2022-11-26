import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Utils {
    
    public static String getRandom01(int length) {
        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(r.nextInt(2));
        }
        return sb.toString();
    }
    
    public static String SHA256(String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        byte[] ans = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(bytes);
            ans = md.digest();
        } catch (NoSuchAlgorithmException exception) {
            exception.printStackTrace();
        }
        return byteToHex(ans);
    }
    
    private static String byteToHex(byte[] a) {
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
    
    public static String intToBinStr(int n) {
        StringBuilder sb = new StringBuilder();
        byte[] bits = new byte[32];
        for (int i = 31; i >= 0; i--) {
            bits[i] = (byte) (n & 1);
            n = n >> 1;
        }
        for (int i = 0; i < 32; i++) {
            sb.append(bits[i]);
        }
        return sb.toString();
    }
    
    public static int binToInt(String s) {
        int ans = 0;
        int bi = 1;
        for (int i = 31; i >= 0; i--) {
            char c = s.charAt(i);
            ans += s.charAt(i) == '1' ? bi : 0;
            bi = bi << 1;
        }
        return ans;
    }
}

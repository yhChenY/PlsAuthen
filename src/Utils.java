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
}

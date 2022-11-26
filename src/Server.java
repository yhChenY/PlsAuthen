import java.security.Security;
import java.util.Random;

public class Server {
    String hostname;
    String curUid;
    String curRs;
    
    private String rChallenge(){
        StringBuilder sb = new StringBuilder();
        sb.append(hostname);
        curUid = Utils.getRandom01(128<<2);
        sb.append(curUid);
        curRs = Utils.getRandom01(128);
        sb.append(curRs);
        // send to client??
        // hostname 不定长
        return sb.toString();
    }
    
    

}

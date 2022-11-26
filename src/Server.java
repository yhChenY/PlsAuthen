import java.security.Security;
import java.util.Random;

public class Server {
    String hostname;
    String curUid;
    String curRs;
    
    private String rChallenge(){
        StringBuilder sb = new StringBuilder();
        curUid = Utils.getRandom01(128<<2);
        sb.append(curUid);
        curRs = Utils.getRandom01(128);
        sb.append(curRs);
        sb.append(hostname);
        // send to client??
        // hostname 不定长
        return sb.toString();
    }
    
    private String aChallenge(){
        StringBuilder sb = new StringBuilder();
        String rs = Utils.getRandom01(128);
        sb.append(rs);
        sb.append(hostname);
        // send to client
        return sb.toString();
    }
    
    boolean rCheck(String ad){
        Object[] info = adDecoder(ad,true);
        String h = (String)info[0];
        int n = (int)info[1];
        String cid = (String)info[2];
        String pk = (String)info[3];
        //check
        //TODO
        return false;
    }
    
    Object[] adDecoder(String ad,boolean r){
        Object[] ans;
        if(r){
            ans = new Object[4];
            String h = ad.substring(0,256);
            ans[0] = h;
            int n = Utils.binToInt(ad.substring(256,256+32));
            ans[1] = n;
            String cid = ad.substring(288,288+512);
            ans[2] = cid;
            String pk = ad.substring(800,928);
            ans[3] = pk;
        }else{
            ans = new Object[2];
            String h = ad.substring(0,256);
            ans[0] = h;
            int nt = Utils.binToInt(ad.substring(256,256+32));
            ans[1] = nt;
        }
        return ans;
    }
}

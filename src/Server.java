
public class Server {
    String ids;
    String curUid;
    String curRs;
    
    public String rChallenge() {
        StringBuilder sb = new StringBuilder();
        curUid = Utils.getRandom01(128 << 2);
        sb.append(curUid);
        curRs = Utils.getRandom01(128);
        sb.append(curRs);
        sb.append(ids);
        // send to client??
        // hostname 不定长
        return sb.toString();
    }
    
    public String aChallenge() {
        StringBuilder sb = new StringBuilder();
        String rs = Utils.getRandom01(128);
        sb.append(rs);
        sb.append(ids);
        // send to client
        return sb.toString();
    }
    
    boolean rCheck(String ad) {
        Object[] info = adDecoder(ad, true);
        String h = (String) info[0];
        int n = (int) info[1];
        String cid = (String) info[2];
        String pk = (String) info[3];
        //check (select from database and compare)
        // to do
        if (n != 0 || !h.equals(Utils.SHA256(ids))){
            return false;
        }
        //store info
        DatabaseOp db = new DatabaseOp();
        db.getConnection();
        db.insertServerCredential(ids,cid,curUid,n,pk);
        db.closeConnection();
        return true;
    }
    
    boolean aCheck(String ad) {
        Object[] info = adDecoder(ad, false);
        String h = (String) info[0];
        int nt = (int) info[1];
        String cid; //TODO
        //check... select and compare
        //update credential info
        DatabaseOp db = new DatabaseOp();
        db.getConnection();
        //TODO
        db.closeConnection();
        return false;
    }
    
    Object[] adDecoder(String ad, boolean r) {
        Object[] ans;
        if (r) {
            ans = new Object[4];
            String h = ad.substring(0, 64);
            ans[0] = h;
            int n = Utils.binToInt(ad.substring(64, 96));
            ans[1] = n;
            String cid = ad.substring(96, 224);
            ans[2] = cid;
            String pk = ad.substring(224);
            ans[3] = pk;
        } else {
            ans = new Object[2];
            String h = ad.substring(0, 64);
            ans[0] = h;
            int nt = Utils.binToInt(ad.substring(64, 96));
            ans[1] = nt;
        }
        return ans;
    }
}

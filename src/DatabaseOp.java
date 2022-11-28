import java.sql.*;

public class DatabaseOp {
    public Connection con = null;
    private String host, dbname, user, password, port;
    
    public DatabaseOp(String host, String dbname, String user, String password, String port) {
        this.host = host;
        this.dbname = dbname;
        this.user = user;
        this.password = password;
        this.port = port;
    }
    
    public DatabaseOp() {
        host = "localhost";
        dbname = "fido2";
        user = "postgres";
        password = "cyh.1592364780.";
        port = "5432";
    }
    
    public void getConnection() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (Exception e) {
            System.err.println("Cannot find the PostgreSQL driver. Check CLASSPATH.");
            System.exit(1);
        }
        
        try {
            String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbname;
            con = DriverManager.getConnection(url, user, password);
            
        } catch (SQLException e) {
            System.err.println("Database connection failed");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
    
    public void closeConnection() {
        if (con != null) {
            try {
                con.close();
                con = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void insert(String statement, String[] info) {
        String sql = statement + "values (?,?)";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            for (int i = 0; i < 2; i++) {
                ps.setString(i + 1, info[i]);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void delete(String sql) {
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public String select(String sql) {
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            
            ResultSet rs = stmt.executeQuery(sql);
            String pt = "";
            while (rs.next()) {
                pt = rs.getString("pt");
            }
            
            rs.close();
            stmt.close();
            return pt;
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return null;
    }
    
    public void insertTokenCredential(String tokenName, String ids, String cid, int n, String sk, String pk) {
        try {
            PreparedStatement ps;
            String sql2 = "insert into token_credential_info(token,ids,cid,n,sk,pk) values(?,?,?,?,?,?);";
            ps = con.prepareStatement(sql2);
            ps.setString(1,tokenName);
            ps.setString(2, ids);
            ps.setString(3, cid);
            ps.setInt(4, n);
            ps.setString(5, sk);
            ps.setString(6, pk);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void insertServerCredential(String ids, String cid, String uid, int n, String pk) {
        try {
            String sql = "insert into server_credential_info(ids,cid,uid,n,pk) values(?,?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1,ids);
            ps.setString(2,cid);
            ps.setString(3,uid);
            ps.setInt(4,n);
            ps.setString(5,pk);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}

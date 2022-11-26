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
        dbname = "postgres";
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
}

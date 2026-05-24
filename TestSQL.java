import java.sql.*;

public class TestSQL {
    public static void main(String[] args) throws Exception {
        Class.forName("org.hsqldb.jdbcDriver");
        String url = "jdbc:hsqldb:file:C:/Users/USUARIO/kriolopos;shutdown=true";
        System.out.println("user.home property: " + System.getProperty("user.home"));
        try (Connection con = DriverManager.getConnection(url, "SA", "")) {
            System.out.println("Connected to HSQLDB as SA!");
            try (Statement stmt = con.createStatement()) {
                DatabaseMetaData meta = con.getMetaData();
                try (ResultSet rs = meta.getTables(null, null, "%", new String[] {"TABLE"})) {
                    System.out.println("--- TABLES ---");
                    while (rs.next()) {
                        System.out.println("Table: " + rs.getString(3) + " | Schema: " + rs.getString(2));
                    }
                } catch (Exception e) {
                    System.out.println("Metadata Error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Connection Error: " + e.getMessage());
        }
    }
}

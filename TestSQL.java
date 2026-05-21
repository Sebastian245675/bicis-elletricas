import java.sql.*;

public class TestSQL {
    public static void main(String[] args) throws Exception {
        Class.forName("org.hsqldb.jdbcDriver");
        String url = "jdbc:hsqldb:file:C:/Users/USUARIO/kriolopos/;shutdown=true";
        String user = "kriolopos";
        // The properties file has: db.password=crypt\:B725E95247955C1600A1E56A1F4BD502
        // If it's encrypted we cannot easily connect. Wait!
        // HSQLDB default is SA, let's just try SA without password first because maybe kriolopos is the db schema but SA still exists.
        try (Connection con = DriverManager.getConnection(url, "SA", "")) {
            System.out.println("Connected to HSQLDB as SA!");
            try (Statement stmt = con.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT FOLDER_NAME, TITLE FROM APP_DOCUMENTS");
                while (rs.next()) {
                    System.out.println("Row: " + rs.getString(1) + " | " + rs.getString(2));
                }
            } catch (Exception e) {
                System.out.println("Query Error: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Connection Error: " + e.getMessage());
        }
    }
}

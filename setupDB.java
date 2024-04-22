import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;

public class setupDB {
    public static void main(String[] args) {
        String connectionUrl = "jdbc:sqlserver://cxp-sql-03\\sxp1220;"
                + "database=XC;"
                + "user=dbuser;"
                + "password=VBpQwY4WWoqR0y;"
                + "encrypt=true;"
                + "trustServerCertificate=true;"
                + "loginTimeout=15;";

        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
            // Execute CreateTablesSQLQueries.sql
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

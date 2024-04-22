import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class testing {
    public static void main(String[] args) {

        try (Connection conn = DriverManager.getConnection(connectionURL.getConnectionString())) {
            conn.setAutoCommit(false); // Disable auto-commit for transaction management

            try (CallableStatement cstmt = conn.prepareCall("{CALL TransferAthlete(?, ?, ?)}")) {
                // Set the input parameters for the stored procedure
                cstmt.setInt(1, 1006);
                cstmt.setInt(2, 1005);

                // Register the output parameter
                cstmt.registerOutParameter(3, Types.NVARCHAR);

                // Execute the stored procedure and retrieve the output parameter value
                cstmt.execute();
                String outputMessage = cstmt.getString(3);

                // Commit the transaction
                conn.commit();

                // Print the message to the console
                System.out.println(outputMessage);
            } catch (SQLException ex) {
                // If there is an exception, roll back the transaction
                conn.rollback();
                System.out.println("An error occurred: " + ex.getMessage());
                ex.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        /*
         * String callProcedure = "{call TransferAthlete(?, ?)}";
         * ResultSet rs = null;
         * 
         * try (Connection conn =
         * DriverManager.getConnection(connectionURL.getConnectionString());
         * CallableStatement stmt = conn.prepareCall(callProcedure)) {
         * 
         * // Set the parameters for the stored procedure
         * stmt.setInt(1, 1002);
         * stmt.setInt(2, 1004);
         * stmt.execute();
         * rs = stmt.getResultSet();
         * String responseMessage = rs.getString("ResponseMessage"); // Column name as
         * per your SP.
         * System.out.println(responseMessage);
         * System.out.println(stmt.getResultSet());
         * boolean hasResults = stmt.execute();
         * // Execute the stored procedure
         * if (hasResults) {
         * rs = stmt.getResultSet();
         * if (rs.next()) {
         * responseMessage = rs.getString("ResponseMessage");
         * System.out.println(responseMessage);
         * }
         * } else {
         * System.out.println("There is no resultset");
         * }
         * } catch (SQLException e) {
         * e.printStackTrace();
         * } finally {
         * if (rs != null)
         * try {
         * rs.close();
         * } catch (SQLException e) {
         * e.printStackTrace();
         * }
         * }
         * }
         */

    }
}

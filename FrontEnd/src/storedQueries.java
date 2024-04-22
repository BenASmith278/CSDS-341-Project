
//import java.util.*;
import java.sql.*;

public class storedQueries {

    static public Connection conn = storedQueries.makeConnection(connectionURL.getConnectionString());

    public static CallableStatement prepareCallableStatement(Connection connection, String sql) {
        try (CallableStatement stmt = connection.prepareCall(sql);) {
            return stmt;
        } catch (SQLException e) {
            System.out.println("Error preparing statement.");
            e.printStackTrace();
            return null;
        }
    }

    public static Connection makeConnection(String connectionUrl) {
        try {
            Connection conn = DriverManager.getConnection(connectionUrl);
            return conn;
        } catch (SQLException e) {
            System.out.println("Error connecting to database.");
            e.printStackTrace();
            return null;
        }
    }

    public static ResultSet executeQuery(CallableStatement stmt, String[] inputs, storedQueries.SQLType[] inputTypes,
            String[] outputs, storedQueries.SQLType[] outputTypes) {
        try {
            for (int i = 0; i < inputs.length; i++) {
                switch (inputTypes[i]) {
                    case INT:
                        stmt.setInt(i + 1, Integer.parseInt(inputs[i]));
                        break;
                    case VARCHAR:
                        stmt.setString(i + 1, inputs[i]);
                        break;
                    case DATE:
                        stmt.setDate(i + 1, Date.valueOf(inputs[i]));
                        break;
                    case TIME:
                        stmt.setTime(i + 1, Time.valueOf(inputs[i]));
                        break;
                    default:
                        break;
                }
            }

            for (int i = 0; i < outputs.length; i++) {
                switch (outputTypes[i]) {
                    case INT:
                        stmt.registerOutParameter(inputs.length + i + 1, Types.INTEGER);
                        break;
                    case VARCHAR:
                        stmt.registerOutParameter(inputs.length + i + 1, Types.VARCHAR);
                        break;
                    case DATE:
                        stmt.registerOutParameter(inputs.length + i + 1, Types.DATE);
                        break;
                    case TIME:
                        stmt.registerOutParameter(inputs.length + i + 1, Types.TIME);
                        break;
                    default:
                        break;
                }
            }

            ResultSet rs = stmt.executeQuery();
            return rs;
        } catch (SQLException e) {
            System.out.println("Error executing query.");
            e.printStackTrace();
            return null;
        }
    }

    public enum SQLType {
        INT, VARCHAR, DATE, TIME
    }

    public static void transferStudent(int athleteId, int schoolId) {
        try (Connection conn = DriverManager.getConnection(connectionURL.getConnectionString())) {
            conn.setAutoCommit(false); // Disable auto-commit for transaction management

            try (CallableStatement cstmt = conn.prepareCall("{CALL TransferAthlete(?, ?, ?)}")) {
                // Set the input parameters for the stored procedure
                cstmt.setInt(1, athleteId);
                cstmt.setInt(2, schoolId);

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
    }

    public static void addResultToMeet(int athleteId, int eventId, Time time, int place) {

        try (Connection conn = DriverManager.getConnection(connectionURL.getConnectionString())) {
            conn.setAutoCommit(false); // Managing transactions manually

            try (CallableStatement cstmt = conn.prepareCall("{CALL AddMeetResult(?, ?, ?, ?, ?)}")) {
                cstmt.setInt(1, athleteId);
                cstmt.setInt(2, eventId);
                cstmt.setTime(3, time);
                cstmt.setInt(4, place);
                cstmt.registerOutParameter(5, Types.NVARCHAR); // Register the output parameter

                cstmt.execute();
                String outputMessage = cstmt.getString(5); // Retrieve the output message
                conn.commit(); // Commit the transaction

                System.out.println(outputMessage); // Print the response message
            } catch (SQLException ex) {
                conn.rollback(); // Rollback the transaction in case of an error
                System.out.println("An error occurred: " + ex.getMessage());
                ex.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void createMeet(int hostSchoolId, String meetName, Date startDate, Time startTime, int season,
            String status) {
        try (Connection conn = DriverManager.getConnection(connectionURL.getConnectionString())) {
            conn.setAutoCommit(false); // Managing transactions manually

            try (CallableStatement cstmt = conn.prepareCall("{CALL CreateMeet(?, ?, ?, ?, ?, ?, ?)}")) {
                cstmt.setInt(1, hostSchoolId);
                cstmt.setString(2, meetName);
                cstmt.setDate(3, startDate);
                cstmt.setTime(4, startTime);
                cstmt.setInt(5, season);
                cstmt.setString(6, status);
                cstmt.registerOutParameter(7, Types.VARCHAR);

                cstmt.execute();
                String outputMessage = cstmt.getString(7); // Retrieve the output message
                conn.commit(); // Commit the transaction

                System.out.println(outputMessage); // Print the response message
            } catch (SQLException ex) {
                conn.rollback(); // Rollback the transaction in case of an error
                System.out.println("An error occurred: " + ex.getMessage());
                ex.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void findTopPerformers(int season, int raceId, int rows) {
        // SQL query to call the stored procedure
        String sql = "{CALL FindTopPerformers(?, ?, ?)}";

        try {
            CallableStatement stmt = prepareCallableStatement(conn, sql);
            // Set the parameters for the stored procedure
            stmt.setInt(1, season);
            stmt.setInt(2, raceId);
            stmt.setInt(3, rows);

            // Execute the stored procedure
            boolean hasResults = stmt.execute();

            // Check if there are results
            do {
                if (hasResults) {
                    try (ResultSet rs = stmt.getResultSet()) {
                        while (rs.next()) {
                            String performerName = rs.getString(1); // Concatenated first and last name
                            Time time = rs.getTime(2); // Result time
                            System.out.println("Performer: " + performerName + ", Time: " + time);
                        }
                    }
                } else {
                    int updateCount = stmt.getUpdateCount();
                    if (updateCount == -1) {
                        // No more results
                        break;
                    }
                }
                hasResults = stmt.getMoreResults();
            } while (true);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void calRaceResult(int raceId, int meetId) {
        // SQL query to call the stored procedure
        String sql = "{CALL CalculateScore(?, ?, ?, ?, ?)}";

        try {
            CallableStatement stmt = prepareCallableStatement(conn, sql);

            // Set the parameters for the stored procedure
            stmt.setInt(1, raceId);
            stmt.setInt(2, meetId);

            // Register output parameters
            stmt.registerOutParameter(3, Types.INTEGER); // school_id
            stmt.registerOutParameter(4, Types.VARCHAR); // school_name
            stmt.registerOutParameter(5, Types.INTEGER); // score

            // Execute the stored procedure
            boolean hasResults = stmt.execute();

            // Check if there are results, including PRINT statements
            do {
                if (hasResults) {
                    try (ResultSet rs = stmt.getResultSet()) {
                        while (rs.next()) {
                            // Handle any result set if necessary
                        }
                    }
                } else {
                    int updateCount = stmt.getUpdateCount();
                    if (updateCount == -1) {
                        // No more results
                        break;
                    }
                }
                hasResults = stmt.getMoreResults();
            } while (true);

            // Retrieve output parameters
            int schoolId = stmt.getInt(3);
            String schoolName = stmt.getString(4);
            int score = stmt.getInt(5);

            // Print or use the output parameters as needed
            System.out.println("School ID: " + schoolId);
            System.out.println("School Name: " + schoolName);
            System.out.println("Score: " + score);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void topKPerformers(int season, int raceId, int k) {
        // SQL query to call the stored procedure
        String sql = "{CALL FindTopPerformers(?, ?, ?)}";

        try {
            CallableStatement stmt = prepareCallableStatement(conn, sql);

            // Set the parameters for the stored procedure
            stmt.setInt(1, season);
            stmt.setInt(2, raceId);
            stmt.setInt(3, k);

            // Execute the stored procedure
            ResultSet rs = stmt.executeQuery();

            // Process the result set
            while (rs.next()) {
                String athleteName = rs.getString("AthleteName");
                Time resultTime = rs.getTime("ResultTime");
                System.out.println("Athlete: " + athleteName + ", Time: " + resultTime);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteResult(int athleteId) {
        // SQL query to call the stored procedure
        String sql = "{CALL DeleteAthleteAndResults(?)}";

        try {
            CallableStatement stmt = prepareCallableStatement(conn, sql);

            // Set the parameters for the stored procedure
            stmt.setInt(1, athleteId);

            // Execute the stored procedure
            stmt.execute();

            System.out.println("Result deleted successfully.");

        } catch (SQLException e) {
            System.out.println("Error deleting result.");
            e.printStackTrace();
        }
    }

}

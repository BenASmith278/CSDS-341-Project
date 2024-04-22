
//import java.util.*;
import java.sql.*;

public class storedQueries {

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

    public static void deleteAthleteAndResults(int athleteId) {
        String sql = "{CALL DeleteAthleteAndResults(?, ?)}";

        try (Connection conn = DriverManager.getConnection(connectionURL.getConnectionString());
                CallableStatement cstmt = conn.prepareCall(sql)) {

            // Set the input parameter for the stored procedure
            cstmt.setInt(1, athleteId);
            // Register the output parameter
            cstmt.registerOutParameter(2, Types.NVARCHAR);

            // Execute the stored procedure
            cstmt.execute();

            // Get the output message
            String outputMessage = cstmt.getString(2);
            System.out.println(outputMessage);

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void findTopPerformers(int season, int raceId, int rows) {
        String sql = "{CALL FindTopPerformers(?, ?, ?)}";

        try (Connection conn = DriverManager.getConnection(connectionURL.getConnectionString());
                CallableStatement cstmt = conn.prepareCall(sql)) {

            // Set the parameters for the stored procedure
            cstmt.setInt(1, season);
            cstmt.setInt(2, raceId);
            cstmt.setInt(3, rows);

            // Execute the stored procedure
            boolean hasResults = cstmt.execute();

            // Process the results
            if (hasResults) {
                try (ResultSet rs = cstmt.getResultSet()) {
                    while (rs.next()) {
                        String name = rs.getString("name");
                        Time time = rs.getTime("time");
                        System.out.printf("%s, Time: %s%n", name, time.toString());
                    }
                }
            } else {
                System.out.println("No results returned.");
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void calRaceResult(int eventId) {
        // SQL query to call the stored procedure
        String findSchools = "{CALL FindSchoolsInEvent(?)}";
        String getAllScores = "{CALL GetScores(?)}";

        try (Connection conn = DriverManager.getConnection(connectionURL.getConnectionString())) {
            conn.setAutoCommit(false);

            try (CallableStatement find = conn.prepareCall(findSchools);
                    CallableStatement scores = conn.prepareCall(getAllScores);) {
                int min = Integer.MAX_VALUE;
                int winner = 0;
                // Set the parameters for the stored procedures
                find.setInt(1, eventId);
                ResultSet schools = find.executeQuery();
                while (schools.next()) {
                    int schoolId = schools.getInt(1);
                    calculateResult(eventId, schoolId);
                }

                scores.setInt(1, eventId);
                ResultSet results = scores.executeQuery();
                while (results.next()) {
                    int schoolId = results.getInt(1);
                    int score = results.getInt(2);
                    if (score < min) {
                        min = score;
                        winner = schoolId;
                    }

                    System.out.println("School: " + schoolId + ", Score: " + score);
                }

                if (min > 14 && min != Integer.MAX_VALUE) {
                    System.out.println("The winner is: " + winner + " with " + min + " points");
                } else {
                    System.out.println("No scoring schools in the event.");
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("An error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void calculateResult(int eventId, int schoolId) {
        // SQL query to call the stored procedure
        String sql = "{CALL CalculateScore(?, ?, ?, ?)}";

        try (Connection conn = DriverManager.getConnection(connectionURL.getConnectionString());
            CallableStatement stmt = conn.prepareCall(sql);) {
            // Set the parameters for the stored procedure
            stmt.setInt(1, eventId);
            stmt.setInt(2, schoolId);
            stmt.registerOutParameter(3, Types.INTEGER);
            stmt.registerOutParameter(4, Types.VARCHAR);
            // Execute the stored procedure
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

import java.sql.Date;
import java.util.*;
import java.sql.*;

public class FrontEnd {
    static public Connection conn;

    public static void main(String[] args) {
        int athleteId;
        int raceId;
        int meetId;
        String time;
        int place;

        String connectionUrl = "jdbc:sqlserver://cxp-sql-03\\sxp1220;"
                + "database=CrossCountryDatabase;"
                + "user=dbuser;"
                + "password=VBpQwY4WWoqR0y;"
                + "encrypt=true;"
                + "trustServerCertificate=true;"
                + "loginTimeout=15;";
        conn = makeConnection(connectionUrl);

        try {
            Scanner sc = new Scanner(System.in);
            System.out.println("Enter the number of the operation you want to perform: ");
            System.out.println("1. Transfer Athlete");
            System.out.println("2. Add Result to Event");
            System.out.println("3. Get Season Top Performer");
            System.out.println("4. Calculate Race Winner");
            System.out.println("5. Create New Meet");
            System.out.println("6. Get K Top Performers");
            System.out.println("7. Delete Errant Result");
            int choice = sc.nextInt();
            switch (choice) {
                case 1:
                    System.out.println("Provide ID of the Athlete you want to transfer: ");
                    athleteId = sc.nextInt();
                    System.out.println("\nProvide the new School ID: ");
                    int schoolId = sc.nextInt();
                    transferStudent(athleteId, schoolId);
                    System.out.println("\nStudent Transferred Successfully");
                    break;
                case 2:
                    System.out.println("Provide ID of the Athlete: ");
                    athleteId = sc.nextInt();
                    System.out.println("\nProvide Race ID: ");
                    raceId = sc.nextInt();
                    System.out.println("\nProvide Meet ID: ");
                    meetId = sc.nextInt();
                    System.out.println("\nProvide Time (hh:mm:ss): ");
                    time = sc.next();
                    System.out.println("\nProvide Place: ");
                    place = sc.nextInt();
                    addResultToMeet(athleteId, raceId, meetId, time, place);
                    break;
                case 3:
                    System.out.println("Provide Season: ");
                    String season = sc.next();
                    System.out.println("\nProvide Race ID: ");
                    raceId = sc.nextInt();
                    System.out.println("\nProvide number of athletes to return: ");
                    int rows = sc.nextInt();
                    findTopPerformers(season, raceId, rows);
                    break;
                case 4:
                    System.out.println("Provide Race ID: ");
                    raceId = sc.nextInt();
                    System.out.println("\nProvide Meet ID: ");
                    meetId = sc.nextInt();
                    calRaceResult(raceId, meetId);
                    break;
                case 5:
                    System.out.println("Provide Host School ID: ");
                    int hostSchoolId = sc.nextInt();
                    System.out.println("\nProvide Meet Name: ");
                    String meetName = sc.next();
                    System.out.println("\nProvide Start Date (yyyy-[m]m-[d]d): ");
                    Date startDate = Date.valueOf(sc.next());
                    System.out.println("\nProvide Start Time: ");
                    Time startTime = Time.valueOf(sc.next());
                    System.out.println("\nProvide Season: ");
                    season = sc.next();
                    createMeet(hostSchoolId, meetName, startDate, startTime, season, "Upcoming");
                    break;
                case 6:
                    System.out.println("Provide Season: ");
                    season = sc.next();
                    System.out.println("\nProvide Race ID: ");
                    raceId = sc.nextInt();
                    System.out.println("\nProvide number of Performers: ");
                    int k = sc.nextInt();
                    topKPerformers(season, raceId, k);
                    break;
                case 7:
                    System.out.println("Provide Athlete ID: ");
                    athleteId = sc.nextInt();
                    System.out.println("\nProvide Event ID: ");
                    int eventId = sc.nextInt();
                    deleteResult(athleteId, eventId);
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number between 1 and 7");
            }
            sc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // use cases
    private static void transferStudent(int atheleteId, int schoolId) {
        String callProcedure = "{call TransferAthlete(?, ?)}";

        try (CallableStatement stmt = prepareCallableStatement(conn, callProcedure)) {
            // Set the parameters for the stored procedure
            String[] inputs = { Integer.toString(atheleteId), Integer.toString(schoolId) };
            SQLType[] inputTypes = { SQLType.INT, SQLType.INT };
            String[] outputs = {};
            SQLType[] outputTypes = {};
            // Execute the stored procedure
            executeQuery(stmt, inputs, inputTypes, outputs, outputTypes);
            System.out.println("Student transferred successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addResultToMeet(int athleteId, int raceId, int meetId, String time, int place) {
        // SQL query to call the stored procedure
        String sql = "{CALL AddMeetResult(?, ?, ?, ?, ?)}";

        try {
            CallableStatement stmt = prepareCallableStatement(conn, sql);
            // Set the parameters for the stored procedure
            String[] inputs = { Integer.toString(athleteId), Integer.toString(raceId), Integer.toString(meetId), time,
                    Integer.toString(place) };
            FrontEnd.SQLType[] inputTypes = { FrontEnd.SQLType.INT, FrontEnd.SQLType.INT, FrontEnd.SQLType.INT,
                    FrontEnd.SQLType.VARCHAR, FrontEnd.SQLType.INT };
            String[] outputs = {};
            FrontEnd.SQLType[] outputTypes = {};

            // Execute the stored procedure
            ResultSet rs = executeQuery(stmt, inputs, inputTypes, outputs, outputTypes);

            // Check if there are results, including PRINT statements
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void findTopPerformers(String season, int raceId, int rows) {
        // SQL query to call the stored procedure
        String sql = "{CALL FindTopPerformers(?, ?, ?)}";

        try {
            CallableStatement stmt = prepareCallableStatement(conn, sql);
            // Set the parameters for the stored procedure
            stmt.setString(1, season);
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

    private static void calRaceResult(int raceId, int meetId) {
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

    private static void createMeet(int hostSchoolId, String meetName, Date startDate, Time startTime, String season,
            String status) {
        // SQL query to call the stored procedure
        String sql = "{CALL CreateMeet(?, ?, ?, ?, ?, ?)}";

        try {
            CallableStatement stmt = prepareCallableStatement(conn, sql);

            // Set the parameters for the stored procedure
            stmt.setInt(1, hostSchoolId);
            stmt.setString(2, meetName);
            stmt.setDate(3, startDate);
            stmt.setTime(4, startTime);
            stmt.setString(5, season);
            stmt.setString(6, status);

            // Execute the stored procedure
            boolean hasResults = stmt.execute();

            // Check if there are results
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

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void topKPerformers(String season, int raceId, int k) {
        // SQL query to call the stored procedure
        String sql = "{CALL FindTopPerformers(?, ?, ?)}";

        try {
            CallableStatement stmt = prepareCallableStatement(conn, sql);

            // Set the parameters for the stored procedure
            stmt.setString(1, season);
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

    private static void deleteResult(int athleteId, int eventId) {
        // SQL query to call the stored procedure
        String sql = "{CALL DeleteResult(?, ?)}";

        try {
            CallableStatement stmt = prepareCallableStatement(conn, sql);

            // Set the parameters for the stored procedure
            stmt.setInt(1, athleteId);
            stmt.setInt(2, eventId);

            // Execute the stored procedure
            stmt.execute();

            System.out.println("Result deleted successfully.");

        } catch (SQLException e) {
            System.out.println("Error deleting result.");
            e.printStackTrace();
        }
    }

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

    public static ResultSet executeQuery(CallableStatement stmt, String[] inputs, FrontEnd.SQLType[] inputTypes,
            String[] outputs, FrontEnd.SQLType[] outputTypes) {
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
}

import java.sql.Date;
import java.sql.Time;
import java.util.Scanner;

public class ShouryaTesting {
    public static void main(String[] args) {
        int athleteId = 1001;
        int raceId = 3001;
        int meetId = 4001;
        String time = "00:20:45";
        int place = 5;
        int schoolId = 2002;
        String season = "Summer";

        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the number of the operation you want to perform: ");
        System.out.println("1. Transfer Athlete");
        System.out.println("2. Add Result to Event");
        System.out.println("3. Get Season Top Performer");
        System.out.println("4.5 Calculate Race Winner");
        System.out.println("5. Create New Meet");
        System.out.println("6. Get Top Performers");
        System.out.println("7. Delete Errant Result");

        int choice = sc.nextInt(); // Read user input
        try {
            switch (choice) {
                case 5:
                    String meetName = "Summer Sprint";
                    Date startDate = Date.valueOf("2024-07-15");
                    Time startTime = Time.valueOf("08:00:00");
                    // Assuming storedQueries is an instance of a class containing the createMeet method
                    storedQueries.createMeet(schoolId, meetName, startDate, startTime, season, "Upcoming");
                    break;
                case 6:
                    // Assuming storedQueries is an instance of a class containing the topKPerformers method
                    storedQueries.topKPerformers(season, raceId, 5);
                    break;
                case 7:
                    // Assuming storedQueries is an instance of a class containing the deleteResult method
                    storedQueries.deleteResult(athleteId, 4003);
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number between 1 and 7");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sc.close(); // Close the scanner
        }
    }
}

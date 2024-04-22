import java.sql.Date;
import java.sql.Time;
import java.util.Scanner;

public class userInterface {
    public static void main(String[] args) {
        int athleteId;
        int raceId;
        String timeString;
        int place;
        int eventId;
        int schoolId;
        int season;
        Scanner sc = new Scanner(System.in);
        boolean running = true;
        while (running) {
            try {
                System.out.println("\n");
                System.out.println("Here are the queries you are able to execute within the Cross Country Database:");
                System.out.println("1. Transfer Athlete");
                System.out.println("2. Add Result to Event");
                System.out.println("3. Get Season Top Performer");
                System.out.println("4. Calculate Race Winner");
                System.out.println("5. Create New Meet");
                System.out.println("6. Ban an Athlete (Delete all records of athlete)");
                System.out.println("0. Exit the interface.");
                System.out.println("Enter the number of the operation you want to perform:  ");
                int choice = sc.nextInt();
                switch (choice) {
                    case 1:
                        System.out.println("Provide ID of the Athlete you want to transfer: ");
                        athleteId = sc.nextInt();
                        System.out.println("\nProvide the new School ID: ");
                        schoolId = sc.nextInt();
                        storedQueries.transferStudent(athleteId, schoolId);
                        break;
                    case 2:
                        System.out.println("Provide ID of the Athlete: ");
                        athleteId = sc.nextInt();
                        System.out.println("\nProvide Event ID: ");
                        eventId = sc.nextInt();
                        System.out.println("\nProvide Time (hh:mm:ss): ");
                        timeString = sc.next();
                        Time time = Time.valueOf(timeString);
                        System.out.println("\nProvide Place: ");
                        place = sc.nextInt();
                        storedQueries.addResultToMeet(athleteId, eventId, time, place);
                        break;
                    case 3:
                        System.out.println("Provide Season: ");
                        season = sc.nextInt();
                        System.out.println("\nProvide Race ID: ");
                        raceId = sc.nextInt();
                        System.out.println("\nProvide number of athletes to return: ");
                        int rows = sc.nextInt();
                        storedQueries.findTopPerformers(season, raceId, rows);
                        break;
                    case 4:
                        System.out.println("Provide Event ID: ");
                        eventId = sc.nextInt();
                        storedQueries.calRaceResult(eventId);
                        break;
                    case 5:
                        System.out.println("Provide Host School ID: ");
                        int hostSchoolId = sc.nextInt();
                        sc.nextLine();
                        System.out.println("\nProvide Meet Name: ");
                        String meetName = sc.nextLine();
                        System.out.println("\nProvide Start Date (yyyy-[m]m-[d]d): ");
                        String tempDate = sc.next();
                        Date startDate = Date.valueOf(tempDate);
                        System.out.println("\nProvide Start Time (hh:mm:ss): ");
                        String tempTime = sc.next();
                        Time startTime = Time.valueOf(tempTime);
                        System.out.println(
                                "\nProvide Season: (This value is the integer year the competition scores will fall under)");
                        season = sc.nextInt();
                        storedQueries.createMeet(hostSchoolId, meetName, startDate, startTime, season, "upcoming");
                        break;
                    case 6:
                        System.out.println("Provide the Athlete ID: ");
                        athleteId = sc.nextInt();
                        storedQueries.deleteAthleteAndResults(athleteId);
                        break;
                    case 0:
                        running = false;
                        System.out.println("Exiting program...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 7");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        sc.close();
    }
}

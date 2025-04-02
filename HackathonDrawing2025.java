import java.util.Random;
import java.util.Scanner;

public class HackathonDrawing2025 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the number of players/teams:");
        int numParticipants = scanner.nextInt();
        scanner.nextLine(); // Consume the newline

        if (numParticipants <= 0) {
            System.out.println("Invalid number of participants. Exiting program.");
            return;
        }

        String[] participants = new String[numParticipants];
        System.out.println("Enter the names of players/teams:");

        for (int i = 0; i < numParticipants; i++) {
            participants[i] = scanner.nextLine().trim();
        }

        scanner.close();

        // Generate a random winner
        Random random = new Random();
        int winnerIndex = random.nextInt(numParticipants);
        
        System.out.println("The winner of the drawing is: " + participants[winnerIndex]);
    }
}

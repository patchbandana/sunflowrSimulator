/* JournalActions.java
 * Handles all journal-related actions
 * Created to modularize sunflowerSimulator.java
 * 
 * UPDATES:
 * - Added dream journal submenu
 * - Entries now displayed in reverse chronological order (newest first)
 * - Save game / Exit options
 * - Reset game (New Game+)
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class JournalActions {
    
    /**
     * Handles the complete journal menu workflow
     * @param player The current player
     * @param scanner Scanner for user input
     * @return Updated player reference (may be new player if reset)
     */
    public static Player1 handleJournal(Player1 player, Scanner scanner) {
        boolean inJournal = true;
        int currentPage = 0;
        int totalPages = Math.max(1, (int)Math.ceil((double)player.getJournalEntries().size() / Journal.ENTRIES_PER_PAGE));

        while (inJournal) {
            System.out.println("\n📖 Journal Menu 📖");
            System.out.println("1. View Journal Entries");
            System.out.println("2. Add New Entry");
            System.out.println("3. View Dream Journal");
            System.out.println("4. Save Game / Exit");
            System.out.println("5. Return to Main Menu");
            System.out.println("6. Reset Game (New Game+)");

            System.out.print("\nEnter your choice: ");
            String journalChoice = scanner.next();
            scanner.nextLine();

            switch (journalChoice) {
            case "1":
                // View journal entries with pagination
                viewJournalEntries(player, scanner, currentPage, totalPages);
                break;

            case "2":
                // Add new journal entry
                addJournalEntry(player, scanner);
                totalPages = Math.max(1, (int)Math.ceil((double)player.getJournalEntries().size() / Journal.ENTRIES_PER_PAGE));
                break;

            case "3":
                // NEW: View dream journal
                viewDreamJournal(player, scanner);
                break;

            case "4":
                // Save/Exit options
                boolean shouldExit = handleSaveAndExit(player, scanner);
                if (shouldExit) {
                    System.exit(0);
                }
                break;

            case "5":
                // Return to main menu
                inJournal = false;
                break;

            case "6":
                // Reset game
                Player1 resetPlayer = handleResetGame(player, scanner);
                if (resetPlayer != null) {
                    player = resetPlayer;
                    inJournal = false;
                }
                break;

            default:
                System.out.println("Invalid choice. Please try again.");
            }
        }
        
        return player;
    }
    
    /**
     * Displays journal entries in REVERSE chronological order (newest first)
     * @param player The player
     * @param scanner Scanner for user input
     * @param currentPage Starting page number (0-based)
     * @param totalPages Total number of pages
     */
    private static void viewJournalEntries(Player1 player, Scanner scanner, int currentPage, int totalPages) {
        boolean viewingEntries = true;

        List<String> allEntries = player.getJournalEntries();
        
        // UPDATED: Reverse the list to show newest first
        List<String> reversedEntries = new ArrayList<>(allEntries);
        Collections.reverse(reversedEntries);
        
        totalPages = Math.max(1, (int)Math.ceil((double)reversedEntries.size() / Journal.ENTRIES_PER_PAGE));

        while (viewingEntries) {
            System.out.println("\n=== Journal Entries (Page " + (currentPage + 1) + " of " + totalPages + ") ===");
            System.out.println("(Showing newest entries first)");

            int startIndex = currentPage * Journal.ENTRIES_PER_PAGE;
            int endIndex = Math.min(startIndex + Journal.ENTRIES_PER_PAGE, reversedEntries.size());

            if (reversedEntries.isEmpty()) {
                System.out.println("No journal entries yet. Add some with option 2!");
                viewingEntries = false;
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
                break;
            } else if (startIndex >= reversedEntries.size()) {
                System.out.println("No entries on this page.");
                currentPage = 0;
                continue;
            } else {
                for (int i = startIndex; i < endIndex; i++) {
                    System.out.println(reversedEntries.get(i));
                }
            }

            System.out.println("\nNavigation:");
            if (currentPage > 0) {
                System.out.println("P: Previous Page");
            }
            if (currentPage < totalPages - 1 && endIndex < reversedEntries.size()) {
                System.out.println("N: Next Page");
            }
            System.out.println("B: Back to Journal Menu");

            System.out.print("\nEnter your choice: ");
            String pageChoice = scanner.next().toUpperCase();
            scanner.nextLine();

            switch (pageChoice) {
            case "P":
                if (currentPage > 0) {
                    currentPage--;
                }
                break;
            case "N":
                if (currentPage < totalPages - 1 && endIndex < reversedEntries.size()) {
                    currentPage++;
                }
                break;
            case "B":
                viewingEntries = false;
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    /**
     * NEW: Displays the dream journal showing unlocked dreams
     * @param player The player
     * @param scanner Scanner for user input
     */
    private static void viewDreamJournal(Player1 player, Scanner scanner) {
        System.out.println("\n✨ Dream Journal ✨");
        
        // Get total dream count from DreamReader
        int totalDreams = DreamReader.getDreamCount();
        int unlockedCount = player.getUnlockedDreamCount();
        
        System.out.println("Dreams Unlocked: " + unlockedCount + "/" + totalDreams);
        System.out.println();
        
        if (unlockedCount == 0) {
            System.out.println("You haven't experienced any dreams yet.");
            System.out.println("Dreams occur randomly when you go to sleep.");
            System.out.println("\nPress Enter to return to journal menu...");
            scanner.nextLine();
            return;
        }
        
        // Convert unlocked dreams to a sorted list for display
        List<String> dreamList = new ArrayList<>(player.getUnlockedDreams());
        Collections.sort(dreamList);
        
        // Display each unlocked dream with a number
        for (int i = 0; i < dreamList.size(); i++) {
            System.out.println("Dream " + (i + 1) + ": " + dreamList.get(i).replace(".txt", ""));
        }
        
        System.out.println("\nWould you like to read a specific dream? (1-" + dreamList.size() + ", or 0 to return)");
        System.out.print("Choice: ");
        
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }
        
        if (choice == 0) {
            return;
        }
        
        if (choice < 1 || choice > dreamList.size()) {
            System.out.println("Invalid dream number.");
            return;
        }
        
        // Read and display the selected dream
        String dreamFile = dreamList.get(choice - 1);
        System.out.println("\n╔════════════════════════════════════╗");
        
        // Use DreamReader to get the dream content (it's cached)
        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.FileReader("dream.txt/" + dreamFile));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
        } catch (java.io.IOException e) {
            System.out.println("Error reading dream file.");
        }
        
        System.out.println("╚════════════════════════════════════╝");
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Adds a new journal entry
     * @param player The player
     * @param scanner Scanner for user input
     */
    private static void addJournalEntry(Player1 player, Scanner scanner) {
        System.out.println("\nWrite a new journal entry:");
        String newEntry = scanner.nextLine();
        if (Journal.addJournalEntry(player, newEntry)) {
            System.out.println("Journal entry added successfully!");
            
            // Check if we hit the limit and notify user
            if (player.getJournalEntries().size() >= Journal.MAX_ENTRIES) {
                System.out.println("(Note: Journal is at maximum capacity. Oldest entries are being archived.)");
            }
            
            Journal.saveGame(player);
        } else {
            System.out.println("Failed to add journal entry.");
        }
    }
    
    /**
     * Handles save and exit options
     * @param player The player
     * @param scanner Scanner for user input
     * @return true if game should exit, false otherwise
     */
    private static boolean handleSaveAndExit(Player1 player, Scanner scanner) {
        System.out.println("\nSave / Exit Options:");
        System.out.println("1. Save Game and Continue");
        System.out.println("2. Save Game and Exit");
        System.out.print("\nEnter your choice: ");
        String saveChoice = scanner.next();
        scanner.nextLine();

        switch (saveChoice) {
        case "1":
            if (Journal.saveGame(player)) {
                System.out.println("Game saved successfully!");
            } else {
                System.out.println("Failed to save game.");
            }
            return false;
            
        case "2":
            System.out.println("Saving game and exiting...");
            Journal.addJournalEntry(player, "Ended gardening session on day " + player.getDay() + ".");
            if (Journal.saveGame(player)) {
                System.out.println("Game saved successfully. Thanks for playing!");
            } else {
                System.out.println("Warning: There was an issue saving the game.");
                System.out.println("Exiting anyway. Thanks for playing!");
            }
            return true;
            
        default:
            System.out.println("Invalid choice. Returning to Journal Menu.");
            return false;
        }
    }
    
    /**
     * Handles game reset (New Game+)
     * @param player The current player
     * @param scanner Scanner for user input
     * @return New player if reset confirmed, null if cancelled
     */
    private static Player1 handleResetGame(Player1 player, Scanner scanner) {
        System.out.println("\n⚠️ WARNING: This will reset your game while keeping your name! ⚠️");
        System.out.println("All progress, inventory items, and stats will be reset to default values.");
        System.out.println("This cannot be undone. Your previous save will be overwritten.");
        System.out.print("\nAre you sure you want to reset? (yes/no): ");
        String confirmReset = scanner.next().toLowerCase();
        scanner.nextLine();

        if (confirmReset.equals("yes")) {
            String nameToKeep = player.getName();
            Player1 newPlayer = new Player1(nameToKeep);

            FlowerInstance starterSeed = new FlowerInstance(
                    "Mammoth Sunflower", "Seed", 0, 10, 1, 5);
            newPlayer.addToInventory(starterSeed);

            Journal.resetGame(newPlayer);
            Journal.addJournalEntry(newPlayer, "Started a new adventure! (New Game+)");

            System.out.println("\n🔄 Game has been reset successfully!");
            System.out.println("Welcome to your new adventure, " + nameToKeep + "!");
            System.out.println("It is day " + newPlayer.getDay() + ".");
            System.out.println("You have " + newPlayer.getNRG() + " NRG and " + newPlayer.getCredits() + " credits.");
            System.out.println("A new Mammoth Sunflower seed has been added to your inventory.");

            return newPlayer;
        } else {
            System.out.println("Reset cancelled. Your game remains unchanged.");
            return null;
        }
    }
}
/* Creator: Pat Eizenga
 * Created: 6/18/2024
 * Last Updated: 4/5/2025
 * Project: Open source, open dialog, gardening game developed with love, focus and dreams.
 * */

package sunflowrSimulator;

import java.util.Random;
import java.util.Scanner;
import java.util.List;

/**
 * Main game executed here.
 */
public class sunflowerSimulator {

    /**
     * Main game.
     * @param args NOT USED
     */
    public static void main(String[] args) {
        // Tutorial and introductions
        System.out.println("🌻 Welcome to Sunflower Simulator! 🌻");
        System.out.println("A gardening game developed with love, focus, and dreams.\n");
                
        // Declare a scanner for user input
        Scanner scanner = new Scanner(System.in);
        
        // Get player name and check for existing save
        System.out.print("Please enter your name: ");
        String playerName = scanner.nextLine();
        
        // Check if a save file exists for this player
        Player1 player;
        boolean newGame = !Journal.saveExists(playerName);
        
        if (newGame) {
            // Create a new player
            player = new Player1(playerName);
            
            // New game introduction
            System.out.println("\nHey there, " + player.getName() + "! It's a pleasure to meet you. :)");
            System.out.println("Let's get started with some of the basics!");
            System.out.println("Each day, you will start with " + player.getNRG() + " NRG.");
            System.out.println("Spend your energy wisely! When you run out you will need to go to bed.");
            System.out.println("You start with " + player.getCredits() + " credits to buy seeds.");
            
            // Add a starting flower seed to the player's inventory
            MammothSunflower starterSeed = new MammothSunflower(
                "Mammoth Sunflower", "Seed", 0, 10, 1, 5);
            player.addToInventory(starterSeed);
            System.out.println("\nYou've been given a Mammoth Sunflower seed to start your garden!");
            
            // Create first journal entry
            Journal.saveGame(player);
            Journal.addJournalEntry(player, "Started my gardening adventure!");
        } else {
            // Load existing player
            player = Journal.loadGame(playerName);
            System.out.println("\nWelcome back, " + player.getName() + "!");
            System.out.println("Your game has been loaded from day " + player.getDay() + ".");
            System.out.println("You have " + player.getNRG() + " NRG and " + player.getCredits() + " credits.");
            Journal.addJournalEntry(player, "Resumed my gardening adventure.");
        }
        
        // Main game loop
        boolean gameContinues = true;
        
        do {
            // Display menu
            System.out.println("\n=== Day " + player.getDay() + " ===");
            System.out.println("NRG: " + player.getNRG() + " | Credits: " + player.getCredits());
            System.out.println("\nWhat would you like to do?");
            System.out.println("1: Weed Garden");
            System.out.println("2: Water Garden");
            System.out.println("3: Plant");
            System.out.println("4: Build");
            System.out.println("5: Shop");
            System.out.println("6: Backpack");
            System.out.println("7: Trim Plants");
            System.out.println("8: Check");
            System.out.println("9: Journal");
            System.out.println("0: Go to bed");
            
            System.out.print("\nEnter your choice: ");
            char actionMenuChoice = scanner.next().charAt(0);
            scanner.nextLine(); // Clear input buffer
            
            // Process menu choice
            switch(actionMenuChoice) {
                case '0': // Go to bed
                    System.out.print("Going to bed will restore NRG and prepare you for the next day. Are you ready? (Y/N) ");
                    String choice = scanner.next();
                    scanner.nextLine(); // Clear buffer
                    
                    if (choice.charAt(0) == 'Y' || choice.charAt(0) == 'y') {
                        // Save game before advancing day
                        Journal.saveGame(player);
                        
                        // Handle dream chance
                        Random random = new Random();
                        int chanceOfDreamRoll = random.nextInt(100);
                        
                        if (chanceOfDreamRoll > 50) {
                            System.out.println("\n💤 You had a strange dream about your garden...");
                            // TODO: Implement dream content
                            System.out.println("You wake up feeling inspired.");
                            Journal.addJournalEntry(player, "Had a strange dream about the garden.");
                        } else {
                            System.out.println("\nYou wake up feeling refreshed and rested. It's a new day! :D");
                            Journal.addJournalEntry(player, "Slept soundly through the night.");
                        }
                        
                        // Advance to next day
                        player.advanceDay();
                        System.out.println("It is now day " + player.getDay() + ".");
                        
                        // Advance growth of planted flowers
                        // This would require tracking planted flowers separately
                    }
                    break;
                    
                case '1': // Weed Garden
                    if (player.getNRG() <= 0) {
                        System.out.println("You're too tired to do that. You need to go to bed first!");
                        break;
                    }
                    
                    System.out.println("You spend some time weeding your garden.");
                    // Implement garden weeding mechanics TBD
                    player.setNRG(player.getNRG() - 1);
                    System.out.println("You used 1 NRG. Remaining NRG: " + player.getNRG());
                    Journal.addJournalEntry(player, "Spent time weeding the garden.");
                    break;
                    
                case '2': // Water Garden
                    if (player.getNRG() <= 0) {
                        System.out.println("You're too tired to do that. You need to go to bed first!");
                        break;
                    }
                    
                    System.out.println("You water your plants.");
                    // Implement garden watering mechanics TBD
                    player.setNRG(player.getNRG() - 1);
                    System.out.println("You used 1 NRG. Remaining NRG: " + player.getNRG());
                    Journal.addJournalEntry(player, "Watered the garden.");
                    break;
                    
                case '3': // Plant
                    if (player.getNRG() <= 0) {
                        System.out.println("You're too tired to do that. You need to go to bed first!");
                        break;
                    }
                    
                    // Check if player has any seeds
                    boolean hasSeeds = false;
                    for (Object item : player.getInventory()) {
                        if (item instanceof Flower && ((Flower)item).getGrowthStage().equals("Seed")) {
                            hasSeeds = true;
                            break;
                        }
                    }
                    
                    if (!hasSeeds) {
                        System.out.println("You don't have any seeds to plant! Visit the shop to buy some.");
                    } else {
                        System.out.println("Planting functionality coming soon!");
                        // Implement planting mechanics TBD
                        player.setNRG(player.getNRG() - 2);
                        System.out.println("You used 2 NRG. Remaining NRG: " + player.getNRG());
                        Journal.addJournalEntry(player, "Planted some seeds in the garden.");
                    }
                    break;
                    
                case '4': // Build
                    if (player.getNRG() <= 0) {
                        System.out.println("You're too tired to do that. You need to go to bed first!");
                        break;
                    }
                    
                    System.out.println("Building functionality coming soon!");
                    // Implement building mechanics TBD
                    player.setNRG(player.getNRG() - 3);
                    System.out.println("You used 3 NRG. Remaining NRG: " + player.getNRG());
                    Journal.addJournalEntry(player, "Worked on building something in the garden.");
                    break;
                    
                case '5': // Shop
                    boolean inShop = true;
                    while (inShop) {
                        System.out.println("\n🌼 Welcome to the Flower Shop! 🌼");
                        System.out.println("You have " + player.getCredits() + " credits.");
                        System.out.println("Here are today's seeds for sale:");
                        
                        System.out.println("1. Mammoth Sunflower Seed - 5 credits");
                        System.out.println("2. Mammoth Sunflower Seed - 5 credits");
                        System.out.println("3. Mammoth Sunflower Seed - 5 credits");
                        System.out.println("4. Mammoth Sunflower Seed - 5 credits");
                        System.out.println("5. Leave Shop");
                        
                        System.out.print("Pick a seed to buy (1-5): ");
                        String shopChoice = scanner.next();
                        scanner.nextLine(); // Clear buffer
                        
                        // Handle seed buying
                        switch (shopChoice) {
                            case "1":
                            case "2":
                            case "3":
                            case "4":
                                if (player.getCredits() >= 5) {
                                    // Create a unique seed name
                                    int seedNumber = player.getInventory().size() + 1;
                                    Flower seed = new MammothSunflower(
                                        "Mammoth Sunflower #" + seedNumber, 
                                        "Seed", 0, 100, 1, 5.0);
                                    player.addToInventory(seed);
                                    player.setCredits(player.getCredits() - 5);
                                    System.out.println("✅ You bought a Mammoth Sunflower seed!");
                                    Journal.addJournalEntry(player, "Purchased a Mammoth Sunflower seed from the shop.");
                                    Journal.saveGame(player); // Save after purchase
                                } else {
                                    System.out.println("❌ You don't have enough credits!");
                                }
                                break;
                            case "5":
                                inShop = false;
                                System.out.println("Thank you for visiting the shop!");
                                break;
                            default:
                                System.out.println("Please enter a valid choice (1-5).");
                        }
                    }
                    break;
                    
                case '6': // Backpack/Inventory
                    System.out.println("\n📦 Checking your backpack...");
                    if (player.getInventory().isEmpty()) {
                        System.out.println("Your backpack is empty.");
                    } else {
                        System.out.println("Items in your backpack:");
                        for (int i = 0; i < player.getInventory().size(); i++) {
                            System.out.println((i+1) + ". " + player.getInventory().get(i));
                        }
                    }
                    
                    System.out.println("\nPress Enter to return to the main menu...");
                    scanner.nextLine();
                    break;
                    
                case '7': // Trim Plants
                    if (player.getNRG() <= 0) {
                        System.out.println("You're too tired to do that. You need to go to bed first!");
                        break;
                    }
                    
                    System.out.println("Trimming functionality coming soon!");
                    // Implement plant trimming mechanics TBD
                    player.setNRG(player.getNRG() - 1);
                    System.out.println("You used 1 NRG. Remaining NRG: " + player.getNRG());
                    Journal.addJournalEntry(player, "Trimmed some plants in the garden.");
                    break;
                    
                case '8': // Check
                    System.out.println("Check functionality coming soon!");
                    // Implement garden checking mechanics TBD
                    break;
                    
                case '9': // Journal
                    boolean inJournal = true;
                    int currentPage = 0; // Start at first page
                    int totalPages = Math.max(1, (int)Math.ceil((double)player.getJournalEntries().size() / Journal.ENTRIES_PER_PAGE));
                    
                    while (inJournal) {
                        System.out.println("\n📓 Journal Menu 📓");
                        System.out.println("1. View Journal Entries");
                        System.out.println("2. Add New Entry");
                        System.out.println("3. Save Game");
                        System.out.println("4. Return to Main Menu");
                        
                        System.out.print("\nEnter your choice: ");
                        String journalChoice = scanner.next();
                        scanner.nextLine(); // Clear buffer
                        
                        switch (journalChoice) {
                            case "1":
                                // View journal entries with pagination
                                boolean viewingEntries = true;
                                
                                // Recalculate total pages based on current journal entries
                                List<String> allEntries = player.getJournalEntries();
                                totalPages = Math.max(1, (int)Math.ceil((double)allEntries.size() / Journal.ENTRIES_PER_PAGE));
                                
                                while (viewingEntries) {
                                    System.out.println("\n=== Journal Entries (Page " + (currentPage + 1) + " of " + totalPages + ") ===");
                                    
                                    // Calculate indices for current page
                                    int startIndex = currentPage * Journal.ENTRIES_PER_PAGE;
                                    int endIndex = Math.min(startIndex + Journal.ENTRIES_PER_PAGE, allEntries.size());
                                    
                                    if (allEntries.isEmpty()) {
                                        System.out.println("No journal entries yet. Add some with option 2!");
                                        viewingEntries = false;
                                        System.out.println("\nPress Enter to continue...");
                                        scanner.nextLine();
                                        break;
                                    } else if (startIndex >= allEntries.size()) {
                                        System.out.println("No entries on this page.");
                                        currentPage = 0; // Reset to first page
                                        continue;
                                    } else {
                                        // Display entries for current page
                                        for (int i = startIndex; i < endIndex; i++) {
                                            System.out.println(allEntries.get(i));
                                        }
                                    }
                                    
                                    // Navigation options
                                    System.out.println("\nNavigation:");
                                    if (currentPage > 0) {
                                        System.out.println("P: Previous Page");
                                    }
                                    if (currentPage < totalPages - 1 && endIndex < allEntries.size()) {
                                        System.out.println("N: Next Page");
                                    }
                                    System.out.println("B: Back to Journal Menu");
                                    
                                    System.out.print("\nEnter your choice: ");
                                    String pageChoice = scanner.next().toUpperCase();
                                    scanner.nextLine(); // Clear buffer
                                    
                                    switch (pageChoice) {
                                        case "P":
                                            if (currentPage > 0) {
                                                currentPage--;
                                            }
                                            break;
                                        case "N":
                                            if (currentPage < totalPages - 1 && endIndex < allEntries.size()) {
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
                                break;
                                
                            case "2":
                                // Add new entry
                                System.out.println("\nWrite a new journal entry:");
                                String newEntry = scanner.nextLine();
                                if (Journal.addJournalEntry(player, newEntry)) {
                                    System.out.println("Journal entry added successfully!");
                                    
                                    // Save game immediately after adding entry to ensure it persists
                                    Journal.saveGame(player);
                                    
                                    // Recalculate total pages
                                    totalPages = Math.max(1, (int)Math.ceil((double)player.getJournalEntries().size() / Journal.ENTRIES_PER_PAGE));
                                } else {
                                    System.out.println("Failed to add journal entry.");
                                }
                                break;
                                
                            case "3":
                                // Save game
                                if (Journal.saveGame(player)) {
                                    System.out.println("Game saved successfully!");
                                } else {
                                    System.out.println("Failed to save game.");
                                }
                                break;
                                
                            case "4":
                                inJournal = false;
                                break;
                                
                            default:
                                System.out.println("Invalid choice. Please try again.");
                        }
                    }
                    break;
                    
                default:
                    System.out.println("Invalid choice! Please try again.");
                    break;
            }
            
            // Check if player ran out of energy
            if (player.getNRG() <= 0) {
                System.out.println("\nYou've run out of energy! You need to go to bed (option 0).");
            }
            
        } while (gameContinues);
        
        // Close the scanner
        scanner.close();
    }
}
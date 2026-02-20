/* JournalActions.java
 * Handles all journal-related actions
 * FIXED: Removed duplicate "Story loaded successfully!" message
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Map;

public class JournalActions {

	public static Player1 handleJournal(Player1 player, Scanner scanner) {
		boolean inJournal = true;
		int currentPage = 0;

		int totalPages = Journal.getTotalJournalPages(player.getName());

		while (inJournal) {
			totalPages = Journal.getTotalJournalPages(player.getName()); 

			System.out.println("\nðŸ“– Journal Menu ðŸ“–");
			System.out.println("1. View Journal Entries");
			System.out.println("2. Add New Entry");
			System.out.println("3. View Dream Journal");
			System.out.println("4. View Tips Collection");
			System.out.println("5. Save Game / Exit");
			System.out.println("6. Return to Main Menu");
			System.out.println("7. Reset Game (New Game+)");
			System.out.print("\nEnter choice: ");


			if (scanner.hasNextInt()) {
				int choice = scanner.nextInt();
				scanner.nextLine(); 

				switch (choice) {
				case 1:
					handleViewJournal(player, scanner, currentPage, totalPages);
					break;
				case 2:
					handleAddNewEntry(player, scanner);
					currentPage = 0;
					totalPages = Journal.getTotalJournalPages(player.getName()); 
					break;
				case 3:
					handleViewDreamJournal(player, scanner); 
					break;
				case 4:
					handleViewTipsCollection(player, scanner);
					break;
				case 5:
					handleSaveGame(player); 
					inJournal = false;
					System.exit(0);
					break;
				case 6:
					handleSaveGame(player); 
					inJournal = false;
					break;
				case 7:
					Player1 newPlayer = handleResetGame(player, scanner);
					if (newPlayer != null) {
						player = newPlayer; 
						currentPage = 0;
						totalPages = Journal.getTotalJournalPages(player.getName());
					}
					break;
				default:
					System.out.println("Invalid choice. Please enter a number between 1 and 7.");
					break;
				}
			}
			else {
				System.out.println("Invalid input. Please enter a number.");
				scanner.nextLine(); 
			}
		}
		return player;
	}

	private static void handleViewJournal(Player1 player, Scanner scanner, int currentPage, int totalPages) {
		boolean viewing = true;

		totalPages = Journal.getTotalJournalPages(player.getName());
		if (totalPages == 0) {
			System.out.println("\nYour journal is empty.");
			return;
		}

		if (currentPage >= totalPages) {
			currentPage = Math.max(0, totalPages - 1);
		}

		while (viewing) {
			// Use player object entries for immediate display
			List<String> allEntries = player.getJournalEntries();
			
			// Calculate pagination
			final int ENTRIES_PER_PAGE = 5;
			totalPages = (int) Math.ceil((double) allEntries.size() / ENTRIES_PER_PAGE);
			if (totalPages == 0) totalPages = 1;
			
			// Get entries for current page (newest first)
			int startIdx = Math.max(0, allEntries.size() - ((currentPage + 1) * ENTRIES_PER_PAGE));
			int endIdx = allEntries.size() - (currentPage * ENTRIES_PER_PAGE);
			endIdx = Math.min(endIdx, allEntries.size());
			
			List<String> entries = new ArrayList<>();
			for (int i = endIdx - 1; i >= startIdx; i--) {
				entries.add(allEntries.get(i));
			}

			System.out.println("\n=== Journal Entries (Page " + (currentPage + 1) + " of " + totalPages + ") ===");
			System.out.println("(Showing newest entries first)");

			for (String entry : entries) {
				System.out.println(entry);
			}

			System.out.println("\nNavigation:");
			if (currentPage > 0) {
				System.out.print("P: Previous Page | ");
			}
			if (currentPage < totalPages - 1) {
				System.out.print("N: Next Page | ");
			}
			System.out.println("B: Back to Journal Menu");
			System.out.print("Enter command: ");

			String command = scanner.nextLine().trim().toUpperCase();

			switch (command) {
			case "P":
				if (currentPage > 0) {
					currentPage--;
				}
				break;
			case "N":
				if (currentPage < totalPages - 1) {
					currentPage++;
				}
				break;
			case "B":
				viewing = false;
				break;
			default:
				System.out.println("Invalid command.");
				break;
			}
			totalPages = Journal.getTotalJournalPages(player.getName());
		}
	}

	private static void handleAddNewEntry(Player1 player, Scanner scanner) {
		System.out.println("\n✏️ Add New Journal Entry ✏️");
		System.out.print("Enter your entry: ");
		String newEntry = scanner.nextLine().trim();

		if (newEntry.isEmpty()) {
			System.out.println("Entry cannot be empty. No entry added.");
			return;
		}

		boolean success = Journal.addJournalEntry(player, newEntry);

		if (success) {
			System.out.println("âœ… New entry added and game saved successfully.");
		} else {
			System.out.println("âŒ Failed to add entry or save game.");
		}
	}

	private static void handleViewDreamJournal(Player1 player, Scanner scanner) {
		System.out.println("\nðŸ˜´ Dream Journal ðŸ˜´");

		if (player.getUnlockedDreams().isEmpty()) {
			System.out.println("Your dream journal is empty. Keep sleeping to unlock new dreams!");
			return;
		}

		Map<String, String> dreamMap = DreamReader.getUnlockedDreamsWithTitles(player.getUnlockedDreams());
		List<String> dreamFiles = new ArrayList<>(dreamMap.keySet());

		if (dreamFiles.isEmpty()) {
			System.out.println("Could not load any dream files.");
			return;
		}

		boolean inDreamMenu = true;
		while(inDreamMenu) {
			System.out.println("\n--- Unlocked Dreams ---");
			for (int i = 0; i < dreamFiles.size(); i++) {
				String fileName = dreamFiles.get(i);
				System.out.println((i + 1) + ". " + fileName); 
			}
			System.out.println("0. Back to Journal Menu");
			System.out.print("\nEnter the number of the dream to view: ");

			if (scanner.hasNextInt()) {
				int choice = scanner.nextInt();
				scanner.nextLine(); 

				if (choice == 0) {
					inDreamMenu = false;
				} else if (choice > 0 && choice <= dreamFiles.size()) {
					String selectedFile = dreamFiles.get(choice - 1);
					displayDreamContent(selectedFile); 
				} else {
					System.out.println("Invalid choice. Please enter a number from the list.");
				}
			} else {
				System.out.println("Invalid input. Please enter a number.");
				scanner.nextLine(); 
			}
		}
	}

	private static void handleViewTipsCollection(Player1 player, Scanner scanner) {
		System.out.println("\nðŸ’¡ Tips Collection ðŸ’¡");

		if (player.getUnlockedHints().isEmpty()) {
			System.out.println("Your tips collection is empty. Keep sleeping past day 20 to unlock helpful tips!");
			System.out.println("Press Enter to continue...");
			scanner.nextLine();
			return;
		}

		Map<String, String> hintMap = HintReader.getUnlockedHintsWithTitles(player.getUnlockedHints());
		List<String> hintFiles = new ArrayList<>(hintMap.keySet());

		if (hintFiles.isEmpty()) {
			System.out.println("Could not load any hint files.");
			System.out.println("Press Enter to continue...");
			scanner.nextLine();
			return;
		}

		boolean inTipsMenu = true;
		while(inTipsMenu) {
			System.out.println("\n--- Unlocked Tips (" + hintFiles.size() + "/" + HintReader.getHintCount() + ") ---");
			for (int i = 0; i < hintFiles.size(); i++) {
				String fileName = hintFiles.get(i);
				System.out.println((i + 1) + ". " + fileName); 
			}
			System.out.println("0. Back to Journal Menu");
			System.out.print("\nEnter the number of the tip to view: ");

			if (scanner.hasNextInt()) {
				int choice = scanner.nextInt();
				scanner.nextLine(); 

				if (choice == 0) {
					inTipsMenu = false;
				} else if (choice > 0 && choice <= hintFiles.size()) {
					String selectedFile = hintFiles.get(choice - 1);
					displayTipContent(selectedFile); 
				} else {
					System.out.println("Invalid choice. Please enter a number from the list.");
				}
			} else {
				System.out.println("Invalid input. Please enter a number.");
				scanner.nextLine(); 
			}
		}
	}

	private static void displayTipContent(String hintFile) {
		String hintText = HintReader.readHintFile(hintFile);

		System.out.println("\n======================================");
		System.out.println("ðŸ’¡ TIP: " + hintFile); 
		System.out.println("======================================");

		if (hintText != null) {
			System.out.println(hintText); 
		} else {
			System.out.println("[Error: Could not read tip content for " + hintFile + "]");
		}
		System.out.println("======================================");
		System.out.println("\nPress ENTER to continue...");

		try {
			System.in.read(); 
		} catch(Exception e) {
			// Do nothing
		}
	}

	private static void displayDreamContent(String dreamFile) {
		String dreamText = DreamReader.readDreamFile(dreamFile);

		System.out.println("\n======================================");
		System.out.println("â­ DREAM: " + dreamFile); 
		System.out.println("======================================");

		if (dreamText != null) {
			System.out.println(dreamText); 
		} else {
			System.out.println("[Error: Could not read dream content for " + dreamFile + "]");
		}
		System.out.println("======================================");
		System.out.println("\nPress ENTER to continue...");

		try {
			System.in.read(); 
		} catch(Exception e) {
			// Do nothing
		}
	}

	private static void handleSaveGame(Player1 player) {
		System.out.println("\nðŸ’¾ Saving Game...");
		boolean success = Journal.saveGame(player);
		if (success) {
			System.out.println("âœ… Adventure saved successfully!");
		} else {
			System.out.println("âŒ Error saving game.");
		}
	}

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

			System.out.println("\nðŸ”„ Game has been reset successfully!");
			System.out.println("Welcome to your new adventure, " + nameToKeep + "!");
			System.out.println("It is day " + newPlayer.getDay() + ".");
			System.out.println("You have " + newPlayer.getNRG() + " NRG and " + newPlayer.getCredits() + " credits.");
			System.out.println("You have 1 " + starterSeed.getName() + " Seed in your inventory.");
			return newPlayer;
		} else {
			System.out.println("\nOperation cancelled. Returning to Journal Menu.");
			return null;
		}
	}
}
/* JournalActions.java
 * Handles all journal-related actions
 * Created to modularize sunflowerSimulator.java
 * * FINAL FIXES:
 * - TotalPages is calculated robustly using Journal.getTotalJournalPages() to prevent the 21-page bug.
 * - Save/Load success messages are consolidated here to prevent double printing.
 * - handleViewDreamJournal reverted to display raw dream filenames and full dream content as requested.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Map;

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
		
		// CRITICAL FIX: Force the totalPages calculation to rely on 
		// the robust Journal.getTotalJournalPages(), which calls loadGame() and prunes the list.
		int totalPages = Journal.getTotalJournalPages(player.getName());
		
		while (inJournal) {
			// Update totalPages at the start of the loop for safety
			totalPages = Journal.getTotalJournalPages(player.getName()); 

			System.out.println("\n📖 Journal Menu 📖");
			System.out.println("1. View Journal Entries");
			System.out.println("2. Add New Entry");
			System.out.println("3. View Dream Journal");
			System.out.println("4. Save Game / Exit");
			System.out.println("5. Return to Main Menu");
			System.out.println("6. Reset Game (New Game+)");
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
						// Reset page counter and update total pages after adding new entry
						currentPage = 0;
						totalPages = Journal.getTotalJournalPages(player.getName()); 
						break;
					case 3:
						handleViewDreamJournal(player, scanner); 
						break;
					case 4:
						handleSaveGame(player); 
						inJournal = false;
						System.exit(0);
						break;
					case 5:
						handleSaveGame(player); 
						inJournal = false;
						break;
					case 6:
						Player1 newPlayer = handleResetGame(player, scanner);
						if (newPlayer != null) {
							player = newPlayer; 
							currentPage = 0;
							totalPages = Journal.getTotalJournalPages(player.getName());
						}
						break;
					default:
						System.out.println("Invalid choice. Please enter a number between 1 and 6.");
						break;
				}
			} else {
				System.out.println("Invalid input. Please enter a number.");
				scanner.nextLine(); 
			}
		}
		return player;
	}

	/**
	 * Handles viewing and navigating journal entries
	 */
	private static void handleViewJournal(Player1 player, Scanner scanner, int currentPage, int totalPages) {
		boolean viewing = true;

		// Re-fetch total pages before entering viewing loop for robustness
		totalPages = Journal.getTotalJournalPages(player.getName());
		if (totalPages == 0) {
			System.out.println("\nYour journal is empty.");
			return;
		}

		// Adjust currentPage if it's now out of bounds (e.g., if entries were pruned)
		if (currentPage >= totalPages) {
			currentPage = Math.max(0, totalPages - 1);
		}
		
		while (viewing) {
			// Get entries for the current page
			List<String> entries = Journal.getJournalEntries(player.getName(), currentPage);
			
			// Reverse the 5 entries so the newest one is listed first on the page.
			Collections.reverse(entries); 

			System.out.println("\n=== Journal Entries (Page " + (currentPage + 1) + " of " + totalPages + ") ===");
			System.out.println("(Showing newest entries first)");

			for (String entry : entries) {
				System.out.println(entry);
			}

			// Navigation
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
			// Update totalPages in case another action changed the list size (for robustness)
			totalPages = Journal.getTotalJournalPages(player.getName());
		}
	}

	/**
	 * Handles adding a new journal entry
	 */
	private static void handleAddNewEntry(Player1 player, Scanner scanner) {
		System.out.println("\n✍️ Add New Journal Entry ✍️");
		System.out.print("Enter your entry: ");
		String newEntry = scanner.nextLine().trim();

		if (newEntry.isEmpty()) {
			System.out.println("Entry cannot be empty. No entry added.");
			return;
		}

		// Journal.addJournalEntry is now silent and saves the game internally
		boolean success = Journal.addJournalEntry(player, newEntry);

		// Only print success message once here
		if (success) {
			System.out.println("✅ New entry added and game saved successfully.");
		} else {
			System.out.println("❌ Failed to add entry or save game.");
		}
	}

	/**
	 * Handles viewing and selecting a dream from the dream journal
	 */
	private static void handleViewDreamJournal(Player1 player, Scanner scanner) {
		System.out.println("\n😴 Dream Journal 😴");
		
		if (player.getUnlockedDreams().isEmpty()) {
			System.out.println("Your dream journal is empty. Keep sleeping to unlock new dreams!");
			return;
		}
		
		// Map dream file names to themselves for display
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
				// Display the dream number and the raw filename
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
					// Display the content of the selected dream
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
	
	/**
	 * Displays the full content of the selected dream file, using the filename as the header.
	 */
	private static void displayDreamContent(String dreamFile) {
		String dreamText = DreamReader.readDreamFile(dreamFile);
		
		System.out.println("\n======================================");
		// Display the raw filename in the header
		System.out.println("⭐ DREAM: " + dreamFile); 
		System.out.println("======================================");
		
		if (dreamText != null) {
			// Print the ENTIRE content as returned by DreamReader (no skipping or parsing)
			System.out.println(dreamText); 
		} else {
			System.out.println("[Error: Could not read dream content for " + dreamFile + "]");
		}
		System.out.println("======================================");
		System.out.println("\nPress ENTER to continue...");
		
		// Wait for user to press enter before returning to menu
		try {
			System.in.read(); 
		} catch(Exception e) {
			// Do nothing
		}
	}

	/**
	 * Handles saving the game
	 * Prints success message here, after the Journal.saveGame() call finishes silently.
	 */
	private static void handleSaveGame(Player1 player) {
		System.out.println("\n💾 Saving Game...");
		boolean success = Journal.saveGame(player);
		if (success) {
			System.out.println("✅ Adventure saved successfully!");
		} else {
			System.out.println("❌ Error saving game.");
		}
	}

	/**
	 * Handles the game reset (New Game+) functionality
	 * @return new player if reset confirmed, null if cancelled
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

			// Give the player a starting item for the new game
			FlowerInstance starterSeed = new FlowerInstance(
					"Mammoth Sunflower", "Seed", 0, 10, 1, 5);
			newPlayer.addToInventory(starterSeed);

			Journal.resetGame(newPlayer); // This calls saveGame silently
			Journal.addJournalEntry(newPlayer, "Started a new adventure! (New Game+)"); // This calls saveGame silently

			System.out.println("\n🔄 Game has been reset successfully!");
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
/* Creator: Pat Eizenga
 * Created: 6/18/2024
 * Last Updated: 11/19/2025
 * Project: Open source, open dialog, gardening game developed with love, focus and dreams.
 * 
 */

import java.util.Scanner;

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

		// Load flower database, dreams, and hints at game start
		FlowerRegistry.loadFlowerData();
		DreamReader.loadDreamFiles();
		HintReader.loadHintFiles();

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
			FlowerInstance starterSeed = new FlowerInstance(
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
			System.out.println("X: Save & Exit Game");

			System.out.print("\nEnter your choice: ");
			String actionMenuChoice = scanner.next();
			scanner.nextLine(); // Clear the buffer

			// Process menu choice
			switch(actionMenuChoice.toUpperCase()) {

			case "0": // Go to bed
				handleBedtimeMenu(player, scanner);
				break;

			case "1": // Weed Garden
				GardenActions.weedGarden(player);
				break;

			case "2": // Water Garden
				GardenActions.waterGarden(player);
				break;

			case "3": // Plant
				PlantingActions.handlePlanting(player, scanner);
				break;

			case "4": // Build
				BuildingActions.handleBuildMenu(player, scanner);
				break;

			case "5": // Shop
				ShopActions.handleShop(player, scanner);
				break;

			case "6": // Backpack/Inventory
				BackpackActions.displayBackpack(player, scanner);
				break;

			case "7": // Trim Plants
				TrimmingActions.handleTrimming(player, scanner);
				break;

			case "8": // Check
				GardenCheckActions.handleGardenCheck(player, scanner);
				break;

			case "9": // Journal
				player = JournalActions.handleJournal(player, scanner);
				break;

			case "X": // Save & Exit
				System.out.println("Saving game and exiting...");
				Journal.addJournalEntry(player, "Ended gardening session on day " + player.getDay() + ".");
				if (Journal.saveGame(player)) {
					System.out.println("Game saved successfully. Thanks for playing!");
				} else {
					System.out.println("Warning: There was an issue saving the game.");
					System.out.println("Exiting anyway. Thanks for playing!");
				}
				gameContinues = false;
				break;

			default:
				System.out.println("Invalid choice! Please try again.");
				break;
			}

			// Check if player ran out of energy
			if (player.getNRG() <= 0 && gameContinues) {
				System.out.println("\nYou've run out of energy! You need to go to bed (option 0) or save & exit (option X).");
			}

		} while (gameContinues);

		// Close the scanner
		scanner.close();
	}

	// ========================================
	// BEDTIME & DAY ADVANCEMENT
	// ========================================

	/**
	 * Handles the bedtime menu and advancing to the next day
	 */
	private static void handleBedtimeMenu(Player1 player, Scanner scanner) {
		System.out.println("What would you like to do?");
		System.out.println("1: Go to bed (save & continue)");
		System.out.println("2: Save & exit game");

		System.out.print("\nEnter your choice: ");
		String bedChoice = scanner.next();
		scanner.nextLine(); // Clear the buffer

		switch (bedChoice) {
		case "1": // Go to bed and continue
			advanceDay(player);
			break;

		case "2": // Save and exit
			System.out.println("Saving game and exiting...");
			Journal.saveGame(player);
			Journal.addJournalEntry(player, "Ended gardening session on day " + player.getDay() + ".");
			System.out.println("Game saved successfully. Thanks for playing!");
			System.exit(0);
			break;

		default:
			System.out.println("Invalid choice. Please select 1 or 2.");
		}
	}

	/**
	 * Advances to the next day, handling dreams/hints and garden updates
	 */
	private static void advanceDay(Player1 player) {
		// Save game before advancing day
		Journal.saveGame(player);

		System.out.println("\n💤 You drift off to sleep...");

		// UPDATED: Dream or hint system with tracking
		String dreamContent = null;
		String dreamFilename = null;
		boolean showedHint = false;

		// Check if we should show a hint (after day 30, if player hasn't built extra plot)
		if (player.getDay() >= 30 && !player.hasBuiltExtraPlot() && HintReader.hasHints()) {
			// 50% chance to show hint instead of dream
			if (Math.random() < 0.5) {
				dreamContent = HintReader.getSpecificHint("build_expansion.txt");
				if (dreamContent == null) {
					dreamContent = HintReader.getRandomHint(100); // Get any hint
				}
				showedHint = true;
			}
		}

		// If no hint, try for a regular dream
		if (dreamContent == null) {
			// Try to get a random dream - need to track which file it came from
			if (DreamReader.hasDreams() && Math.random() < 0.25) { // 25% chance
				// Get list of dream files and pick one
				java.util.List<String> allDreamFiles = new java.util.ArrayList<>();
				try {
					java.io.File dreamDir = new java.io.File("dream.txt/");
					if (dreamDir.exists() && dreamDir.isDirectory()) {
						java.io.File[] files = dreamDir.listFiles((dir, name) -> name.endsWith(".txt"));
						if (files != null) {
							for (java.io.File file : files) {
								allDreamFiles.add(file.getName());
							}
						}
					}

					if (!allDreamFiles.isEmpty()) {
						// Pick a random dream
						dreamFilename = allDreamFiles.get((int)(Math.random() * allDreamFiles.size()));

						// Read the dream content
						try (java.io.BufferedReader reader = new java.io.BufferedReader(
								new java.io.FileReader("dream.txt/" + dreamFilename))) {
							StringBuilder dream = new StringBuilder();
							String line;
							while ((line = reader.readLine()) != null) {
								dream.append(line).append("\n");
							}
							dreamContent = dream.toString().trim();
						}
					}
				} catch (java.io.IOException e) {
					// Silent fail
				}
			}
		}

		if (dreamContent != null) {
			System.out.println("\n✨ You had a strange dream...\n");
			System.out.println("╔════════════════════════════════════╗");
			System.out.println(dreamContent);
			System.out.println("╚════════════════════════════════════╝");

			if (showedHint) {
				System.out.println("\nYou wake up feeling thoughtful about your garden's potential.");
				Journal.addJournalEntry(player, "Had a dream about expanding the garden.");
			} else {
				System.out.println("\nYou wake up feeling inspired.");

				// UPDATED: Track this dream as unlocked (only if not a hint)
				if (dreamFilename != null) {
					player.unlockDream(dreamFilename);
					Journal.addJournalEntry(player, "Had a vivid dream tonight.");
				}
			}
		} else {
			System.out.println("\nYou slept soundly through the night. It's a new day! :D");
			Journal.addJournalEntry(player, "Slept soundly through the night.");
		}

		// Advance to next day
		player.advanceDay();
		// Reset the shop inventory for the new day
		ShopActions.resetShopInventory(); 

		System.out.println("\n🌅 Day " + player.getDay() + " begins.");
		System.out.println("You feel refreshed! (NRG restored to " + player.getNRG() + ")");

		// Check garden for any notable changes
		displayGardenSummary(player);
	}

	// sunflowerSimulator.java

	// ... replace the entire displayGardenSummary(Player1 player) method with this:

	/**
	 * Displays a summary of overnight garden changes by reading the journal entries 
	 * that were just added by Player1.advanceDay() (which uses the correct stage change logic).
	 * This implementation is robust against journal entry character corruption by stripping 
	 * the leading character and prepending a known-working console character.
	 */
	private static void displayGardenSummary(Player1 player) {
		// Keywords that identify the correct summary messages added by Player1.advanceDay()
		final String[] SUMMARY_KEYWORDS = {
				"grew overnight!", 
				"mutated into something special!", 
				"withered overnight."
		};

		// Map of journal content to console-safe emoji/character
		// These characters are used for the console output to avoid '?'
		final java.util.Map<String, String> CONSOLE_EMOJI_MAP = new java.util.HashMap<>();
		CONSOLE_EMOJI_MAP.put("grew overnight!", "✓"); // Console checkmark
		CONSOLE_EMOJI_MAP.put("mutated into something special!", "✨"); // Mutated emoji is assumed to be compatible
		CONSOLE_EMOJI_MAP.put("withered overnight.", "⚠"); // Console warning

		// Collect the relevant summary messages
		java.util.List<String> summaryMessages = new java.util.ArrayList<>();
		java.util.List<String> allEntries = player.getJournalEntries();

		int totalEntries = allEntries.size();
		int checkStart = Math.max(0, totalEntries - 10); 

		for (int i = totalEntries - 1; i >= checkStart; i--) {
			String entry = allEntries.get(i);

			String matchingKeyword = null;
			for (String keyword : SUMMARY_KEYWORDS) {
				if (entry.contains(keyword)) {
					matchingKeyword = keyword;
					break;
				}
			}

			if (matchingKeyword != null) {
				// Extract the message content from the journal entry (e.g., "? 3 plants grew overnight!")
				int contentStart = entry.lastIndexOf(':') + 2;
				if (contentStart < entry.length()) {
					String message = entry.substring(contentStart).trim();

					// 1. Find the start of the numeric count to skip the leading corrupted character(s).
					int firstDigitIndex = -1;
					for (int k = 0; k < message.length(); k++) {
						if (Character.isDigit(message.charAt(k))) {
							firstDigitIndex = k;
							break;
						}
					}

					// 2. Remove leading corrupted characters/emoji and get the console-safe emoji
					String consoleEmoji = CONSOLE_EMOJI_MAP.get(matchingKeyword);
					String bodyMessage = "";

					if (firstDigitIndex != -1) {
						bodyMessage = message.substring(firstDigitIndex);
					} else {
						// Fallback in case no digit is found, use the entire message body
						bodyMessage = message;
					}

					// Final formatted message: e.g., "✓ 3 plants grew overnight!"
					message = consoleEmoji + " " + bodyMessage;

					summaryMessages.add(message);
				}
			} else {
				// Optimization: Stop searching once we hit an entry from before the "go to bed" action.
				if (entry.contains("Slept soundly") || entry.contains("vivid dream")) {
					break;
				}
			}
		}

		// Summary of overnight changes
		if (!summaryMessages.isEmpty()) {
			// FIX: The title emoji '🌱' is working, so we restore it.
			System.out.println("\n🌱 Garden Update:");

			// Print the messages in the correct order (Grew, Mutated, Withered)
			// Iterate backwards over the collected list to print in the journal creation order
			for (int i = summaryMessages.size() - 1; i >= 0; i--) {
				System.out.println("  " + summaryMessages.get(i));
			}
		}
	}
}
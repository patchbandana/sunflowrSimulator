/* Creator: Pat Eizenga
 * Created: 6/18/2024
 * Last Updated: 11/11/2025
 * Project: Open source, open dialog, gardening game developed with love, focus and dreams.
 * * REFACTORING NOTES:
 * - Extracted water and weed garden functionality into GardenActions.java
 * - Extracted plant menu functionality into PlantingActions.java
 * - Extracted build menu functionality into BuildingActions.java
 * - Extracted shop functionality into ShopActions.java
 * - Extracted backpack display into BackpackActions.java
 * - Extracted trimming functionality into TrimmingActions.java
 * - Extracted garden check functionality into GardenCheckActions.java
 * - Extracted journal functionality into JournalActions.java
 * - Main file now focuses on core game loop and day advancement
 * - Maintained all original gameplay and flavor text
 * * FIX: Now resets shop inventory when the day advances.
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

		// Dream or hint system
		String dreamOrHint = null;
		boolean showedHint = false;

		// Check if we should show a hint (after day 30, if player hasn't built extra plot)
		if (player.getDay() >= 30 && !player.hasBuiltExtraPlot() && HintReader.hasHints()) {
			// 50% chance to show hint instead of dream
			if (Math.random() < 0.5) {
				dreamOrHint = HintReader.getSpecificHint("build_expansion.txt");
				if (dreamOrHint == null) {
					dreamOrHint = HintReader.getRandomHint(100); // Get any hint
				}
				showedHint = true;
			}
		}

		// If no hint, try for a regular dream
		if (dreamOrHint == null) {
			dreamOrHint = DreamReader.getRandomDream(25);
		}

		if (dreamOrHint != null) {
			System.out.println("\n✨ You had a strange dream...\n");
			System.out.println("╔═══════════════════════════════════╗");
			System.out.println(dreamOrHint);
			System.out.println("╚═══════════════════════════════════╝");

			if (showedHint) {
				System.out.println("\nYou wake up feeling thoughtful about your garden's potential.");
				Journal.addJournalEntry(player, "Had a dream about expanding the garden.");
			} else {
				System.out.println("\nYou wake up feeling inspired.");
				Journal.addJournalEntry(player, "Had a vivid dream about the garden tonight.");
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

	/**
	 * Displays a summary of overnight garden changes
	 */
	private static void displayGardenSummary(Player1 player) {
		int growthCount = 0;
		int witheredCount = 0;
		int mutationCount = 0;

		for (gardenPlot plot : player.getGardenPlots()) {
			if (plot.isOccupied()) {
				Flower flower = plot.getPlantedFlower();
				String stage = flower.getGrowthStage();

				if (stage.equals("Seedling") || stage.equals("Bloomed") || stage.equals("Matured")) {
					growthCount++;
				} else if (stage.equals("Withered")) {
					witheredCount++;
				} else if (stage.equals("Mutated")) {
					mutationCount++;
				}
			}
		}

		// Summary of overnight changes
		if (growthCount > 0 || witheredCount > 0 || mutationCount > 0) {
			System.out.println("\n🌱 Garden Update:");
			if (growthCount > 0) {
				System.out.println("  ✓ " + growthCount + " plant(s) grew overnight!");
			}
			if (mutationCount > 0) {
				System.out.println("  ✨ " + mutationCount + " plant(s) mutated into something special!");
			}
			if (witheredCount > 0) {
				System.out.println("  ⚠ " + witheredCount + " plant(s) withered. Consider harvesting them.");
			}
		}
	}
}
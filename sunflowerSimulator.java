/* Creator: Pat Eizenga
 * Created: 6/18/2024
 * Last Updated: 11/08/2025
 * Project: Open source, open dialog, gardening game developed with love, focus and dreams.
 * 
 * REFACTORING NOTES:
 * - Extracted water and weed garden functionality into GardenActions.java
 * - Extracted plant menu functionality into PlantingActions.java
 * - Extracted build menu functionality into BuildingActions.java
 * - Extracted shop functionality into ShopActions.java
 * - Extracted backpack display into BackpackActions.java
 * - Main game loop now focuses on core game flow and coordination
 * - Maintained all original gameplay and flavor text
 */

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

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
			scanner.nextLine(); // Clear input buffer

			// Process menu choice
			switch(actionMenuChoice) {

			case "0": // Go to bed
				handleBedtimeMenu(player, scanner);
				break;

			case "1": // Weed Garden - REFACTORED
				GardenActions.weedGarden(player);
				break;

			case "2": // Water Garden - REFACTORED
				GardenActions.waterGarden(player);
				break;

			case "3": // Plant - REFACTORED
				PlantingActions.handlePlanting(player, scanner);
				break;

			case "4": // Build - REFACTORED
				BuildingActions.handleBuildMenu(player, scanner);
				break;

			case "5": // Shop - REFACTORED
				ShopActions.handleShop(player, scanner);
				break;

			case "6": // Backpack/Inventory - REFACTORED
				BackpackActions.displayBackpack(player, scanner);
				break;

			case "7": // Trim Plants
				handleTrimming(player, scanner);
				break;

			case "8": // Check
				handleGardenCheck(player, scanner);
				break;

			case "9": // Journal
				player = handleJournal(player, scanner);
				break;

			case "X":
			case "x": // Save & Exit
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
		scanner.nextLine(); // Clear buffer

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
			System.out.println("╔════════════════════════════════════╗");
			System.out.println(dreamOrHint);
			System.out.println("╚════════════════════════════════════╝");

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

	// ========================================
	// TRIMMING
	// ========================================

	/**
	 * Handles plant trimming
	 */
	private static void handleTrimming(Player1 player, Scanner scanner) {
		if (player.getNRG() <= 0) {
			System.out.println("You're too tired to do that. You need to go to bed first!");
			return;
		}

		// Check if there are any mature plants to trim
		boolean hasTrimablePlants = false;
		for (gardenPlot plot : player.getGardenPlots()) {
			if (plot.isOccupied()) {
				String stage = plot.getPlantedFlower().getGrowthStage();
				if (stage.equals("Bloomed") || stage.equals("Matured")) {
					hasTrimablePlants = true;
					break;
				}
			}
		}

		if (!hasTrimablePlants) {
			System.out.println("You don't have any plants that need trimming yet!");
			System.out.println("Plants need to be at least in the 'Bloomed' stage to be trimmed.");
			return;
		}

		// Display garden plots with their plants
		System.out.println("\n🌱 Your Garden Plants 🌱");
		List<gardenPlot> gardenPlots = player.getGardenPlots();
		List<Integer> trimablePlotIndices = new ArrayList<>();

		for (int i = 0; i < gardenPlots.size(); i++) {
			gardenPlot plot = gardenPlots.get(i);
			String plotType = plot.isFlowerPot() ? "[🪴]" : "[📦]";
			if (plot.isOccupied()) {
				Flower plant = plot.getPlantedFlower();
				String stage = plant.getGrowthStage();
				System.out.println("Plot #" + (i+1) + " " + plotType + ": " + plant.getName() + 
						" (" + stage + ")" + 
						(stage.equals("Bloomed") || stage.equals("Matured") ? 
								" - Can be trimmed" : ""));

				if (stage.equals("Bloomed") || stage.equals("Matured")) {
					trimablePlotIndices.add(i);
				}
			} else {
				System.out.println("Plot #" + (i+1) + " " + plotType + ": [Empty]");
			}
		}

		// Ask which plot to trim
		System.out.print("\nWhich plot would you like to trim? (");
		for (int i = 0; i < trimablePlotIndices.size(); i++) {
			System.out.print((trimablePlotIndices.get(i) + 1));
			if (i < trimablePlotIndices.size() - 1) {
				System.out.print(", ");
			}
		}
		System.out.print(", or 0 to cancel): ");

		int plotChoice;
		try {
			plotChoice = Integer.parseInt(scanner.nextLine());
		} catch (NumberFormatException e) {
			System.out.println("Invalid input. Please enter a number.");
			return;
		}

		if (plotChoice == 0) {
			System.out.println("Trimming cancelled.");
			return;
		}

		if (plotChoice < 1 || plotChoice > gardenPlots.size() || 
				!trimablePlotIndices.contains(plotChoice - 1)) {
			System.out.println("Invalid plot choice. Please select a plot with a trimable plant.");
			return;
		}

		// Trim the plant
		gardenPlot selectedPlot = gardenPlots.get(plotChoice - 1);
		Flower plant = selectedPlot.getPlantedFlower();

		// Increase durability slightly when trimmed
		plant.setDurability(plant.getDurability() + 2);

		// Use energy
		player.setNRG(player.getNRG() - 1);

		System.out.println("You carefully trim the " + plant.getName() + ".");
		System.out.println("It looks healthier now! Durability increased.");
		System.out.println("You used 1 NRG. Remaining NRG: " + player.getNRG());

		Journal.addJournalEntry(player, "Trimmed a " + plant.getName() + " in plot #" + plotChoice + ".");
	}

	// ========================================
	// GARDEN CHECK & PLOT ACTIONS
	// ========================================

	/**
	 * Handles the garden check menu and plot-specific actions
	 */
	private static void handleGardenCheck(Player1 player, Scanner scanner) {
		System.out.println("\n🌱 Checking your garden... 🌱");

		List<gardenPlot> plots = player.getGardenPlots();
		if (plots.isEmpty()) {
			System.out.println("You don't have any garden plots yet!");
			return;
		}

		player.printGarden();

		// Offer actions on specific plots
		System.out.println("\nWould you like to perform an action on a specific plot?");
		System.out.println("1: Water a plot");
		System.out.println("2: Weed a plot");
		System.out.println("3: Fertilize a plot");
		System.out.println("4: Harvest a plant");
		System.out.println("0: Return to main menu");

		System.out.print("\nEnter your choice: ");
		String gardenAction = scanner.nextLine();

		if (gardenAction.equals("0")) {
			return;
		}

		// Energy check for all garden actions
		if (player.getNRG() <= 0) {
			System.out.println("You're too tired to do that. You need to go to bed first!");
			return;
		}

		// Ask which plot to perform action on
		System.out.print("\nWhich plot would you like to work with? (1-" + plots.size() + "): ");

		int plotChoice;
		try {
			plotChoice = Integer.parseInt(scanner.nextLine());
		} catch (NumberFormatException e) {
			System.out.println("Invalid input. Please enter a number.");
			return;
		}

		if (plotChoice < 1 || plotChoice > plots.size()) {
			System.out.println("Invalid plot number. Please choose a valid plot.");
			return;
		}

		gardenPlot selectedPlot = plots.get(plotChoice - 1);

		switch (gardenAction) {
		case "1": // Water
			handleWaterPlot(player, selectedPlot);
			break;

		case "2": // Weed
			handleWeedPlot(player, selectedPlot, plotChoice);
			break;

		case "3": // Fertilize
			handleFertilizePlot(player, selectedPlot);
			break;

		case "4": // Harvest
			handleHarvestPlot(player, selectedPlot, scanner);
			break;

		default:
			System.out.println("Invalid choice! Please try again.");
		}
	}

	/**
	 * Waters a specific plot
	 */
	private static void handleWaterPlot(Player1 player, gardenPlot selectedPlot) {
		if (selectedPlot.isOccupied()) {
			if (selectedPlot.isWatered()) {
				System.out.println("This plot is already watered today!");
			} else {
				selectedPlot.waterPlot();
				System.out.println("You watered the " + 
						selectedPlot.getPlantedFlower().getName() + ".");
				if (selectedPlot.isFlowerPot()) {
					System.out.println("💡 Good! Flower pot plants need daily watering to avoid durability loss.");
				}
				player.setNRG(player.getNRG() - 1);
				System.out.println("You used 1 NRG. Remaining NRG: " + player.getNRG());
				Journal.addJournalEntry(player, "Watered a " + 
						selectedPlot.getPlantedFlower().getName() + ".");
			}
		} else {
			System.out.println("There's nothing planted in this plot to water!");
		}
	}

	/**
	 * Weeds a specific plot
	 */
	private static void handleWeedPlot(Player1 player, gardenPlot selectedPlot, int plotChoice) {
		if (selectedPlot.isFlowerPot()) {
			System.out.println("Flower pots don't need weeding!");
		} else if (!selectedPlot.isWeeded()) {
			selectedPlot.weedPlot();
			System.out.println("You removed the weeds from plot #" + plotChoice + ".");
			player.setNRG(player.getNRG() - 1);
			System.out.println("You used 1 NRG. Remaining NRG: " + player.getNRG());
			Journal.addJournalEntry(player, "Weeded plot #" + plotChoice + ".");
		} else {
			System.out.println("This plot is already free of weeds!");
		}
	}

	/**
	 * Fertilizes a specific plot
	 */
	private static void handleFertilizePlot(Player1 player, gardenPlot selectedPlot) {
		if (selectedPlot.isOccupied()) {
			if (selectedPlot.isFertilized()) {
				System.out.println("This plot is already fertilized!");
			} else {
				selectedPlot.fertilizePlot();
				System.out.println("You fertilized the " + 
						selectedPlot.getPlantedFlower().getName() + ".");
				player.setNRG(player.getNRG() - 1);
				System.out.println("You used 1 NRG. Remaining NRG: " + player.getNRG());
				Journal.addJournalEntry(player, "Fertilized a " + 
						selectedPlot.getPlantedFlower().getName() + ".");
			}
		} else {
			System.out.println("There's nothing planted in this plot to fertilize!");
		}
	}

	/**
	 * Harvests a plant from a specific plot
	 */
	private static void handleHarvestPlot(Player1 player, gardenPlot selectedPlot, Scanner scanner) {
		if (!selectedPlot.isOccupied()) {
			System.out.println("There's nothing planted in this plot to harvest!");
			return;
		}

		Flower plant = selectedPlot.getPlantedFlower();
		String growthStage = plant.getGrowthStage();

		if (growthStage.equals("Seed") || growthStage.equals("Seedling")) {
			// SPECIAL FLOWER POT BEHAVIOR
			if (selectedPlot.isFlowerPot()) {
				System.out.println("This plant is still young.");
				System.out.println("🪴 Since it's in a flower pot, you can harvest it AND take the pot with you!");
				System.out.print("Harvest and pack the flower pot? (yes/no): ");
				String confirm = scanner.nextLine().toLowerCase();

				if (confirm.equals("yes")) {
					// Harvest the flower
					Flower harvestedFlower = selectedPlot.harvestFlower();
					player.addToInventory(harvestedFlower);

					// Remove the flower pot from garden plots
					player.getGardenPlots().remove(selectedPlot);

					// Add the empty flower pot back to inventory
					gardenPlot emptyPot = new gardenPlot(true);
					player.addToInventory(emptyPot);

					System.out.println("✅ You harvested the " + harvestedFlower.getName() + 
							" (" + harvestedFlower.getGrowthStage() + ").");
					System.out.println("🪴 The flower pot has been returned to your inventory!");

					player.setNRG(player.getNRG() - 2);
					System.out.println("You used 2 NRG. Remaining NRG: " + player.getNRG());
					Journal.addJournalEntry(player, "Harvested a " + harvestedFlower.getName() + 
							" (" + harvestedFlower.getGrowthStage() + ") and packed the flower pot.");
				} else {
					System.out.println("Harvest cancelled.");
				}
			} else {
				System.out.println("This plant is too young to harvest!");
			}
		} else {
			// Regular harvest for Bloomed/Matured/Withered
			Flower harvestedFlower = selectedPlot.harvestFlower();
			player.addToInventory(harvestedFlower);

			System.out.println("You harvested the " + harvestedFlower.getName() + 
					" (" + harvestedFlower.getGrowthStage() + ").");
			System.out.println("It has been added to your inventory.");

			if (selectedPlot.isFlowerPot()) {
				System.out.println("🪴 The flower pot remains in your garden for replanting.");
			}

			player.setNRG(player.getNRG() - 2);
			System.out.println("You used 2 NRG. Remaining NRG: " + player.getNRG());
			Journal.addJournalEntry(player, "Harvested a " + harvestedFlower.getName() + 
					" (" + harvestedFlower.getGrowthStage() + ").");
		}
	}

	// ========================================
	// JOURNAL
	// ========================================

	/**
	 * Handles the journal menu
	 */
	private static Player1 handleJournal(Player1 player, Scanner scanner) {
		boolean inJournal = true;
		int currentPage = 0;
		int totalPages = Math.max(1, (int)Math.ceil((double)player.getJournalEntries().size() / Journal.ENTRIES_PER_PAGE));

		while (inJournal) {
			System.out.println("\n📖 Journal Menu 📖");
			System.out.println("1. View Journal Entries");
			System.out.println("2. Add New Entry");
			System.out.println("3. Save Game / Exit");
			System.out.println("4. Return to Main Menu");
			System.out.println("5. Reset Game (New Game+)");

			System.out.print("\nEnter your choice: ");
			String journalChoice = scanner.next();
			scanner.nextLine();

			switch (journalChoice) {
			case "1":
				// View journal entries with pagination
				boolean viewingEntries = true;

				List<String> allEntries = player.getJournalEntries();
				totalPages = Math.max(1, (int)Math.ceil((double)allEntries.size() / Journal.ENTRIES_PER_PAGE));

				while (viewingEntries) {
					System.out.println("\n=== Journal Entries (Page " + (currentPage + 1) + " of " + totalPages + ") ===");

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
						currentPage = 0;
						continue;
					} else {
						for (int i = startIndex; i < endIndex; i++) {
							System.out.println(allEntries.get(i));
						}
					}

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
					scanner.nextLine();

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
				System.out.println("\nWrite a new journal entry:");
				String newEntry = scanner.nextLine();
				if (Journal.addJournalEntry(player, newEntry)) {
					System.out.println("Journal entry added successfully!");
					Journal.saveGame(player);
					totalPages = Math.max(1, (int)Math.ceil((double)player.getJournalEntries().size() / Journal.ENTRIES_PER_PAGE));
				} else {
					System.out.println("Failed to add journal entry.");
				}
				break;

			case "3":
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
					break;
				case "2":
					System.out.println("Saving game and exiting...");
					Journal.addJournalEntry(player, "Ended gardening session on day " + player.getDay() + ".");
					if (Journal.saveGame(player)) {
						System.out.println("Game saved successfully. Thanks for playing!");
					} else {
						System.out.println("Warning: There was an issue saving the game.");
						System.out.println("Exiting anyway. Thanks for playing!");
					}
					System.exit(0);
					break;
				default:
					System.out.println("Invalid choice. Returning to Journal Menu.");
				}
				break;

			case "4":
				inJournal = false;
				break;

			case "5":
				System.out.println("\n⚠️ WARNING: This will reset your game while keeping your name! ⚠️");
				System.out.println("All progress, inventory items, and stats will be reset to default values.");
				System.out.println("This cannot be undone. Your previous save will be overwritten.");
				System.out.print("\nAre you sure you want to reset? (yes/no): ");
				String confirmReset = scanner.next().toLowerCase();
				scanner.nextLine();

				if (confirmReset.equals("yes")) {
					String nameToKeep = player.getName();
					player = new Player1(nameToKeep);

					FlowerInstance starterSeed = new FlowerInstance(
							"Mammoth Sunflower", "Seed", 0, 10, 1, 5);
					player.addToInventory(starterSeed);

					Journal.resetGame(player);
					Journal.addJournalEntry(player, "Started a new adventure! (New Game+)");

					System.out.println("\n🔄 Game has been reset successfully!");
					System.out.println("Welcome to your new adventure, " + nameToKeep + "!");
					System.out.println("It is day " + player.getDay() + ".");
					System.out.println("You have " + player.getNRG() + " NRG and " + player.getCredits() + " credits.");
					System.out.println("A new Mammoth Sunflower seed has been added to your inventory.");

					inJournal = false;
				} else {
					System.out.println("Reset cancelled. Your game remains unchanged.");
				}
				break;

			default:
				System.out.println("Invalid choice. Please try again.");
			}
		}
		
		return player;
	}
}
/* CompostActions.java
 * Handles all compost bin related actions
 * Created for compost bin feature implementation
 * 
 * FEATURES:
 * - Fertilize all plots for half NRG cost (1 NRG per plot instead of 2)
 * - Collect withered flowers to upgrade soil quality
 * - Requires 10 withered flowers to trigger soil upgrade on next fertilize all
 */

import java.util.Scanner;

public class CompostActions {

	/**
	 * Handles the complete compost bin workflow
	 * @param player The player performing the action
	 * @param scanner Scanner for user input
	 */
	public static void handleCompostBin(Player1 player, Scanner scanner) {
		if (!player.hasCompostBin()) {
			System.out.println("You don't have a compost bin yet!");
			System.out.println("Build one from the Build menu (available after digging your first garden plot).");
			return;
		}

		boolean inCompostMenu = true;

		while (inCompostMenu) {
			displayCompostMenu(player);

			System.out.print("\nChoice: ");
			String choice = scanner.nextLine();

			switch (choice) {
			case "1": // Fertilize All
				fertilizeAll(player, scanner);
				break;

			case "2": // Add Withered Flower
				addWitheredFlower(player, scanner);
				break;

			case "3": // Return
				inCompostMenu = false;
				break;

			default:
				System.out.println("Invalid choice. Please try again.");
			}
		}
	}

	/**
	 * Displays the compost bin menu
	 * @param player The player
	 */
	private static void displayCompostMenu(Player1 player) {
		System.out.println("\n===== Compost Bin =====");
		System.out.println("Current resources: " + player.getNRG() + " NRG | " + player.getCredits() + " credits");
		System.out.println("Withered flowers composting: " + player.getCompostWitheredCount() + "/10");

		if (player.getCompostWitheredCount() >= 10) {
			System.out.println("[*] Compost is ready! Next fertilize all will upgrade soil quality!");
		}

		System.out.println("\nWhat would you like to do?");
		System.out.println("1: Fertilize All Plots (1 NRG per plot)");
		System.out.println("2: Add Withered Flower from Inventory");
		System.out.println("3: Return to Build/Check Menus");
	}

	/**
	 * Fertilizes all occupied garden plots for half NRG cost
	 * If 10+ withered flowers are composted, upgrades soil quality
	 * @param player The player
	 * @param scanner Scanner for user input
	 */
	private static void fertilizeAll(Player1 player, Scanner scanner) {
		// Count plots that can be fertilized
		int unfertilizedCount = 0;
		for (gardenPlot plot : player.getGardenPlots()) {
			if (plot.isOccupied() && !plot.isFertilized()) {
				unfertilizedCount++;
			}
		}

		if (unfertilizedCount == 0) {
			System.out.println("All your plants are already fertilized!");
			return;
		}

		int nrgCost = unfertilizedCount; // Half cost (1 NRG per plot instead of 2)
		boolean willUpgradeSoil = player.getCompostWitheredCount() >= 10;

		System.out.println("\nFertilize All Summary:");
		System.out.println("  - Plots to fertilize: " + unfertilizedCount);
		System.out.println("  - NRG cost: " + nrgCost + " (50% discount from compost bin!)");

		if (willUpgradeSoil) {
			System.out.println("  - [*] BONUS: Will upgrade soil quality in all plots!");
			System.out.println("  - (Consumes 10 composted withered flowers)");
		}

		if (player.getNRG() < nrgCost) {
			System.out.println("\n[!] You don't have enough energy! Need " + nrgCost + " NRG, have " + player.getNRG());
			System.out.println("Press Enter to continue...");
			scanner.nextLine();
			return;
		}

		System.out.print("\nProceed with fertilizing all? (yes/no): ");
		String confirm = scanner.nextLine().toLowerCase();

		if (!confirm.equals("yes")) {
			System.out.println("Fertilization cancelled.");
			return;
		}

		// Perform fertilization
		int fertilizedCount = 0;
		int soilUpgradeCount = 0;

		for (gardenPlot plot : player.getGardenPlots()) {
			if (plot.isOccupied() && !plot.isFertilized()) {
				plot.fertilizePlot();
				fertilizedCount++;

				// Upgrade soil if we have enough compost
				if (willUpgradeSoil) {
					String beforeSoil = plot.getSoilQuality();
					boolean upgraded = upgradePlotSoil(plot);
					if (upgraded) {
						soilUpgradeCount++;
						String afterSoil = plot.getSoilQuality();
						System.out.println("  [*] Plot soil upgraded: " + beforeSoil + " -> " + afterSoil);
					}
				}
			}
		}

		// Deduct NRG
		player.setNRG(player.getNRG() - nrgCost);

		// Consume withered flowers if soil was upgraded
		if (willUpgradeSoil) {
			player.setCompostWitheredCount(player.getCompostWitheredCount() - 10);
		}

		System.out.println("\n[OK] Fertilized " + fertilizedCount + " plots!");

		if (soilUpgradeCount > 0) {
			System.out.println("[*] Upgraded soil quality in " + soilUpgradeCount + " plots!");
			Journal.addJournalEntry(player, "Used compost bin to fertilize all plants and upgrade " + 
					soilUpgradeCount + " plots' soil quality.");
		} else {
			Journal.addJournalEntry(player, "Used compost bin to fertilize " + fertilizedCount + " plants.");
		}

		System.out.println("Remaining NRG: " + player.getNRG());

		if (player.getCompostWitheredCount() > 0) {
			System.out.println("\n[Compost] Withered flowers remaining in compost: " + player.getCompostWitheredCount() + "/10");
		}

		Journal.saveGame(player);
	}

	/**
	 * Upgrades a plot's soil quality by one tier
	 * @param plot The plot to upgrade
	 * @return true if upgraded, false if already at max
	 */
	private static boolean upgradePlotSoil(gardenPlot plot) {
		String currentSoil = plot.getSoilQuality();

		switch (currentSoil) {
		case "Bad":
			plot.setSoilQuality("Average");
			return true;
		case "Average":
			plot.setSoilQuality("Good");
			return true;
		case "Good":
			plot.setSoilQuality("Great");
			return true;
		case "Great":
			plot.setSoilQuality("Magic");
			return true;
		case "Magic":
			return false; // Already at max
		default:
			return false;
		}
	}

	/**
	 * Adds a withered flower from inventory to the compost bin
	 * @param player The player
	 * @param scanner Scanner for user input
	 */
	private static void addWitheredFlower(Player1 player, Scanner scanner) {
		// Find withered flowers in inventory
		java.util.List<Flower> witheredFlowers = new java.util.ArrayList<>();
		for (Object item : player.getInventory()) {
			if (item instanceof Flower) {
				Flower flower = (Flower) item;
				if (flower.getGrowthStage().equals("Withered")) {
					witheredFlowers.add(flower);
				}
			}
		}

		if (witheredFlowers.isEmpty()) {
			System.out.println("\nYou don't have any withered flowers to compost!");
			System.out.println("Withered flowers can be harvested from your garden when plants die.");
			System.out.println("Press Enter to continue...");
			scanner.nextLine();
			return;
		}

		System.out.println("\n[Withered] Withered Flowers in Inventory:");
		for (int i = 0; i < witheredFlowers.size(); i++) {
			Flower flower = witheredFlowers.get(i);
			System.out.println((i + 1) + ": " + flower.getName() + " (Withered)");
		}

		System.out.println("\nCurrent compost: " + player.getCompostWitheredCount() + "/10");
		System.out.println("[Tip] 10 withered flowers enable soil upgrades on next fertilize all!");

		System.out.print("\nWhich flower would you like to compost? (1-" + witheredFlowers.size() + 
				", or 0 to cancel): ");

		int choice;
		try {
			choice = Integer.parseInt(scanner.nextLine());
		} catch (NumberFormatException e) {
			System.out.println("Invalid input.");
			return;
		}

		if (choice == 0) {
			System.out.println("Composting cancelled.");
			return;
		}

		if (choice < 1 || choice > witheredFlowers.size()) {
			System.out.println("Invalid choice.");
			return;
		}

		Flower selectedFlower = witheredFlowers.get(choice - 1);

		// Remove from inventory and add to compost count
		player.removeFromInventory(selectedFlower);
		player.setCompostWitheredCount(player.getCompostWitheredCount() + 1);

		System.out.println("\n[OK] Added " + selectedFlower.getName() + " to the compost bin!");
		System.out.println("Compost progress: " + player.getCompostWitheredCount() + "/10");

		if (player.getCompostWitheredCount() >= 10) {
			System.out.println("[*] You have enough compost! Next fertilize all will upgrade soil quality!");
		}

		Journal.addJournalEntry(player, "Added a withered " + selectedFlower.getName() + " to the compost bin.");
		Journal.saveGame(player);
	}

	/**
	 * Displays detailed compost bin status
	 * @param player The player
	 */
	private static void viewCompostStatus(Player1 player) {
		System.out.println("\n===== Compost Bin Status =====");
		System.out.println("=========================================");
		System.out.println("Withered flowers composting: " + player.getCompostWitheredCount() + "/10");

		if (player.getCompostWitheredCount() >= 10) {
			System.out.println("Status: [*] READY - Next fertilize all will upgrade soil!");
		} else {
			int needed = 10 - player.getCompostWitheredCount();
			System.out.println("Status: [~] Composting - Need " + needed + " more withered flowers");
		}

		System.out.println("\nBenefits:");
		System.out.println("  - Fertilize all plots for 1 NRG each (50% discount)");
		System.out.println("  - 10 withered flowers = soil quality upgrade bonus");
		System.out.println("  - Upgrades all plots: Bad->Average, Average->Good, etc.");

		System.out.println("\nHow to get withered flowers:");
		System.out.println("  - Harvest withered plants from your garden");
		System.out.println("  - Trim withered plants (also harvests them)");
		System.out.println("  - Plants wither from neglect or reaching end of lifecycle");
	}
}
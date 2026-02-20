/* BuildingActions.java
 * Handles all building-related actions including flower pot crafting and garden plot expansion
 * Updated: November 23, 2025 - Added compost bin access
 */

import java.util.Scanner;

public class BuildingActions {

	/**
	 * Handles the complete building menu workflow
	 * @param player The player performing the action
	 * @param scanner Scanner for user input
	 */
	public static void handleBuildMenu(Player1 player, Scanner scanner) {
	    boolean inBuildMenu = true;

	    while (inBuildMenu) {
	        displayBuildMenu(player);
	        
	        System.out.print("\nChoice: ");
	        String buildChoice = scanner.nextLine();

	        // Dynamic option mapping based on what's available
	        int currentOption = 1;
	        
	        // Option 1: Always Craft Flower Pot
	        if (buildChoice.equals(String.valueOf(currentOption))) {
	            craftFlowerPot(player, scanner);
	            currentOption++;
	            continue;
	        }
	        currentOption++;
	        
	        // Option 2: Always Dig New Garden Plot
	        if (buildChoice.equals(String.valueOf(currentOption))) {
	            digNewGardenPlot(player, scanner);
	            currentOption++;
	            continue;
	        }
	        currentOption++;
	        
	        // Option 3: Build Compost Bin (if not built)
	        if (!player.hasCompostBin()) {
	            if (buildChoice.equals(String.valueOf(currentOption))) {
	                buildCompostBin(player, scanner);
	                currentOption++;
	                continue;
	            }
	            currentOption++;
	        }
	        
	        // NEW: Use Compost Bin (if built)
	        if (player.hasCompostBin()) {
	            if (buildChoice.equals(String.valueOf(currentOption))) {
	                CompostActions.handleCompostBin(player, scanner);
	                currentOption++;
	                continue;
	            }
	            currentOption++;
	        }
	        
	        // Compost upgrades (if compost bin exists)
	        if (player.hasCompostBin()) {
	            // Mulcher option
	            if (!player.hasMulcher()) {
	                if (buildChoice.equals(String.valueOf(currentOption))) {
	                    installMulcher(player, scanner);
	                    currentOption++;
	                    continue;
	                }
	                currentOption++;
	            }
	            
	            // Sprinkler option
	            if (!player.hasSprinklerSystem()) {
	                if (buildChoice.equals(String.valueOf(currentOption))) {
	                    installSprinklerSystem(player, scanner);
	                    currentOption++;
	                    continue;
	                }
	                currentOption++;
	            }
	        }
	        
	        // Return option (always last)
	        if (buildChoice.equals(String.valueOf(currentOption))) {
	            inBuildMenu = false;
	            continue;
	        }
	        
	        System.out.println("Invalid choice. Please try again.");
	    }
	}


	/**
	 * Displays the build menu with current stats and options
	 * @param player The player
	 */
	private static void displayBuildMenu(Player1 player) {
	    System.out.println("\n🔨 Build Menu 🔨");
	    System.out.println("Current resources: " + player.getNRG() + " NRG | " + player.getCredits() + " credits");
	    System.out.println();

	    // Show current stats
	    int currentPlots = player.getGardenPlots().size();
	    int placedFlowerPots = player.getPlacedFlowerPotCount();
	    int inventoryFlowerPots = player.getInventoryFlowerPotCount();
	    int totalFlowerPots = placedFlowerPots + inventoryFlowerPots;

	    System.out.println("📊 Your Garden Status:");
	    System.out.println("  • Regular Garden Plots: " + (currentPlots - placedFlowerPots));
	    System.out.println("  • Flower Pots (placed): " + placedFlowerPots);
	    System.out.println("  • Flower Pots (inventory): " + inventoryFlowerPots);
	    System.out.println("  • Total Flower Pots: " + totalFlowerPots + "/10");
	    
	    // Show compost bin upgrades if applicable
	    if (player.hasCompostBin()) {
	        System.out.println("\n🔧 Compost Bin Status:");
	        System.out.println("  ✅ Compost Bin built");
	        System.out.println("     Withered flowers: " + player.getCompostWitheredCount() + "/10");
	        
	        if (player.hasMulcher()) {
	            System.out.println("  ✅ Mulcher installed");
	            if (player.isMulcherActive()) {
	                System.out.println("     (Active: " + player.getMulcherDaysRemaining() + " days remaining)");
	            }
	        } else {
	            System.out.println("  ❌ Mulcher not installed");
	        }
	        
	        if (player.hasSprinklerSystem()) {
	            System.out.println("  ✅ Sprinkler system installed");
	        } else {
	            System.out.println("  ❌ Sprinkler system not installed");
	        }
	    }
	    
	    System.out.println();

	    int optionNum = 1;
	    System.out.println("What would you like to build?");
	    System.out.println(optionNum + ": Craft a Flower Pot (20 credits, 2 NRG)");
	    optionNum++;

	    // Calculate cost for next garden plot
	    int regularPlotCount = currentPlots - placedFlowerPots;
	    int[] costs = calculatePlotCost(regularPlotCount);
	    int plotNRGCost = costs[0];
	    int plotCreditCost = costs[1];

	    System.out.println(optionNum + ": Dig a New Garden Plot (" + plotCreditCost + " credits, " + plotNRGCost + " NRG)");
	    optionNum++;
	    
	    // Compost bin option (only if not built yet)
	    if (!player.hasCompostBin()) {
	        System.out.println(optionNum + ": Build Compost Bin (50 credits, 10 NRG)");
	        optionNum++;
	    }
	    
	    // NEW: Use compost bin (only if built)
	    if (player.hasCompostBin()) {
	        System.out.println(optionNum + ": Use Compost Bin (fertilize all, add withered flowers)");
	        optionNum++;
	    }
	    
	    // Compost bin upgrades (only if compost bin exists)
	    if (player.hasCompostBin()) {
	        if (!player.hasMulcher()) {
	            System.out.println(optionNum + ": Install Mulcher (300 credits, 1 NRG)");
	            optionNum++;
	        }
	        
	        if (!player.hasSprinklerSystem()) {
	            System.out.println(optionNum + ": Install Sprinkler System (20 credits, 20 NRG)");
	            optionNum++;
	        }
	    }
	    
	    System.out.println(optionNum + ": Return to Main Menu");
	}


	/**
	 * Calculates the cost for the next garden plot expansion
	 * Formula:
	 * - 4th plot: 10 NRG, 10 credits
	 * - 5th plot: 11 NRG, 50 credits
	 * - 6th plot: 12 NRG, 100 credits
	 * - 7th+ plots: +1 NRG, +75 credits per expansion
	 * 
	 * @param currentRegularPlots Number of regular plots (excluding flower pots)
	 * @return Array [nrgCost, creditCost]
	 */
	private static int[] calculatePlotCost(int currentRegularPlots) {
		int nextPlotNumber = currentRegularPlots + 1; // What plot number we're building
		int nrgCost;
		int creditCost;

		switch (nextPlotNumber) {
		case 4:
			nrgCost = 10;
			creditCost = 10;
			break;
		case 5:
			nrgCost = 11;
			creditCost = 50;
			break;
		case 6:
			nrgCost = 12;
			creditCost = 100;
			break;
		default:
			// 7th and beyond: each plot costs 1 more NRG and 75 more credits
			int expansionsBeyondSix = nextPlotNumber - 6;
			nrgCost = 12 + expansionsBeyondSix;
			creditCost = 100 + (expansionsBeyondSix * 75);
			break;
		}

		return new int[]{nrgCost, creditCost};
	}

	/**
	 * Handles flower pot crafting
	 * @param player The player
	 * @param scanner Scanner for user input
	 */
	private static void craftFlowerPot(Player1 player, Scanner scanner) {
		System.out.println("\n🪴 Flower Pot Crafting 🪴");
		System.out.println("Cost: 20 credits, 2 NRG");
		System.out.println();
		System.out.println("Flower pots are portable planters with special properties:");
		System.out.println("  ✓ No weeding required");
		System.out.println("  ✓ Can be moved (placed in backpack when harvesting seed/seedling)");
		System.out.println("  ✗ Cannot plant bushes, trees, or 4★+ difficulty flowers");
		System.out.println("  ✗ Plants take DOUBLE durability damage if not watered");
		System.out.println();

		int totalFlowerPots = player.getTotalFlowerPots();
		System.out.println("You can craft up to 10 flower pots total.");
		System.out.println("Current total: " + totalFlowerPots + "/10");

		// Check if at limit
		if (totalFlowerPots >= 10) {
			System.out.println("\n❌ You've already crafted the maximum number of flower pots!");
			System.out.println("Press Enter to continue...");
			scanner.nextLine();
			return;
		}

		// Check resources
		if (!hasResourcesForFlowerPot(player, scanner)) {
			return;
		}

		// Confirm crafting
		System.out.print("\nCraft a flower pot? (yes/no): ");
		String confirmCraft = scanner.nextLine().toLowerCase();

		if (confirmCraft.equals("yes")) {
			player.setCredits(player.getCredits() - 20);
			player.setNRG(player.getNRG() - 2);
			player.craftFlowerPot();

			// Create flower pot and add to inventory
			gardenPlot newFlowerPot = new gardenPlot(true);
			player.addToInventory(newFlowerPot);

			System.out.println("\n✅ You crafted a flower pot!");
			System.out.println("The flower pot has been added to your inventory.");
			System.out.println("You can place it in your garden when planting a seed.");
			System.out.println();
			System.out.println("Remaining: " + player.getNRG() + " NRG | " + player.getCredits() + " credits");

			Journal.addJournalEntry(player, "Crafted a flower pot.");
			Journal.saveGame(player);
		} else {
			System.out.println("Crafting cancelled.");
		}
	}

	/**
	 * Checks if player has resources to craft a flower pot
	 * @param player The player
	 * @param scanner Scanner for user input
	 * @return true if player has resources
	 */
	private static boolean hasResourcesForFlowerPot(Player1 player, Scanner scanner) {
		if (player.getCredits() < 20) {
			System.out.println("\n❌ You don't have enough credits! Need 20, have " + player.getCredits());
			System.out.println("Press Enter to continue...");
			scanner.nextLine();
			return false;
		}

		if (player.getNRG() < 2) {
			System.out.println("\n❌ You don't have enough energy! Need 2 NRG, have " + player.getNRG());
			System.out.println("Press Enter to continue...");
			scanner.nextLine();
			return false;
		}

		return true;
	}

	/**
	 * Handles garden plot expansion
	 * @param player The player
	 * @param scanner Scanner for user input
	 */
	private static void digNewGardenPlot(Player1 player, Scanner scanner) {
		int currentPlots = player.getGardenPlots().size();
		int placedFlowerPots = player.getPlacedFlowerPotCount();
		int regularPlotCount = currentPlots - placedFlowerPots;

		int[] costs = calculatePlotCost(regularPlotCount);
		int plotNRGCost = costs[0];
		int plotCreditCost = costs[1];

		System.out.println("\n🌱 Garden Plot Expansion 🌱");
		System.out.println("Cost: " + plotCreditCost + " credits, " + plotNRGCost + " NRG");
		System.out.println();
		System.out.println("This will be regular garden plot #" + (regularPlotCount + 1));
		System.out.println("(Total plots including flower pots: " + (currentPlots + 1) + ")");
		System.out.println();
		System.out.println("Regular garden plots can plant any flower with no restrictions.");
		System.out.println("Each additional plot requires more resources than the last.");

		// Check resources
		if (!hasResourcesForGardenPlot(player, plotCreditCost, plotNRGCost, scanner)) {
			return;
		}

		// Confirm digging
		System.out.print("\nDig a new garden plot? (yes/no): ");
		String confirmDig = scanner.nextLine().toLowerCase();

		if (confirmDig.equals("yes")) {
			player.setCredits(player.getCredits() - plotCreditCost);
			player.setNRG(player.getNRG() - plotNRGCost);
			player.addGardenPlot();
			player.setHasBuiltExtraPlot(true); // Track for hint system

			System.out.println("\n✅ You dug a new garden plot!");
			System.out.println("Your garden now has " + (regularPlotCount + 1) + " regular plots.");
			System.out.println("(Total plots including flower pots: " + (currentPlots + 1) + ")");
			System.out.println();
			System.out.println("Remaining: " + player.getNRG() + " NRG | " + player.getCredits() + " credits");

			Journal.addJournalEntry(player, "Expanded the garden by digging a new plot.");
			Journal.saveGame(player);
		} else {
			System.out.println("Expansion cancelled.");
		}
	}

	/**
	 * Checks if player has resources to dig a new garden plot
	 * @param player The player
	 * @param creditCost Cost in credits
	 * @param nrgCost Cost in NRG
	 * @param scanner Scanner for user input
	 * @return true if player has resources
	 */
	private static boolean hasResourcesForGardenPlot(Player1 player, int creditCost, 
			int nrgCost, Scanner scanner) {
		if (player.getCredits() < creditCost) {
			System.out.println("\n❌ You don't have enough credits! Need " + creditCost + 
					", have " + player.getCredits());
			System.out.println("Press Enter to continue...");
			scanner.nextLine();
			return false;
		}

		if (player.getNRG() < nrgCost) {
			System.out.println("\n❌ You don't have enough energy! Need " + nrgCost + 
					" NRG, have " + player.getNRG());
			System.out.println("Press Enter to continue...");
			scanner.nextLine();
			return false;
		}

		return true;
	}

	/**
	 * Handles compost bin construction
	 * Only available after building first extra garden plot
	 * @param player The player
	 * @param scanner Scanner for user input
	 */
	private static void buildCompostBin(Player1 player, Scanner scanner) {
		System.out.println("\n♻️ Compost Bin Construction ♻️");
		System.out.println("Cost: 50 credits, 10 NRG");
		System.out.println();
		System.out.println("The compost bin is an advanced gardening structure:");
		System.out.println("  ✓ Fertilize all plots at once for 1 NRG each (50% discount!)");
		System.out.println("  ✓ Compost withered flowers (10 flowers = soil upgrade bonus)");
		System.out.println("  ✓ When you have 10+ withered flowers, next fertilize all");
		System.out.println("    upgrades soil quality in all plots simultaneously");
		System.out.println();

		// Check resources
		if (player.getCredits() < 50) {
			System.out.println("\n❌ You don't have enough credits! Need 50, have " + player.getCredits());
			System.out.println("Press Enter to continue...");
			scanner.nextLine();
			return;
		}

		if (player.getNRG() < 10) {
			System.out.println("\n❌ You don't have enough energy! Need 10 NRG, have " + player.getNRG());
			System.out.println("Press Enter to continue...");
			scanner.nextLine();
			return;
		}

		System.out.print("\nBuild compost bin? (yes/no): ");
		String confirm = scanner.nextLine().toLowerCase();

		if (confirm.equals("yes")) {
			player.setCredits(player.getCredits() - 50);
			player.setNRG(player.getNRG() - 10);
			player.buildCompostBin();

			System.out.println("\n✅ You built a compost bin!");
			System.out.println("You can now access it from the Build menu!");
			System.out.println("Start composting withered flowers to unlock soil upgrades.");
			System.out.println();
			System.out.println("Remaining: " + player.getNRG() + " NRG | " + player.getCredits() + " credits");

			Journal.addJournalEntry(player, "Built a compost bin for efficient gardening.");
			Journal.saveGame(player);
		} else {
			System.out.println("Construction cancelled.");
		}
	}
	
	/**
	 * Handles mulcher installation
	 */
	private static void installMulcher(Player1 player, Scanner scanner) {
	    System.out.println("\n🔧 Mulcher Installation 🔧");
	    System.out.println("Cost: 300 credits, 1 NRG");
	    System.out.println();
	    System.out.println("The mulcher upgrade processes weeds into compost material.");
	    System.out.println();
	    System.out.println("Benefits:");
	    System.out.println("  ✓ After weeding garden, weeds grow at 0.25x speed for 7 days");
	    System.out.println("  ✓ Reduces risk of durability damage from neglected weeds");
	    System.out.println("  ✓ Frees up NRG for other tasks");
	    System.out.println("  ✓ Does not apply to flower pots (they never need weeding)");
	    System.out.println();
	    
	    // Check resources
	    if (player.getCredits() < 300) {
	        System.out.println("❌ You don't have enough credits! Need 300, have " + player.getCredits());
	        System.out.println("Press Enter to continue...");
	        scanner.nextLine();
	        return;
	    }
	    
	    if (player.getNRG() < 1) {
	        System.out.println("❌ You don't have enough energy! Need 1 NRG, have " + player.getNRG());
	        System.out.println("Press Enter to continue...");
	        scanner.nextLine();
	        return;
	    }
	    
	    System.out.print("Install the mulcher? (yes/no): ");
	    String confirm = scanner.nextLine().toLowerCase();
	    
	    if (confirm.equals("yes")) {
	        player.setCredits(player.getCredits() - 300);
	        player.setNRG(player.getNRG() - 1);
	        player.installMulcher();
	        
	        System.out.println("\n✅ Mulcher installed successfully!");
	        System.out.println("The next time you weed your garden, weeds will grow much slower for 7 days!");
	        System.out.println();
	        System.out.println("Remaining: " + player.getNRG() + " NRG | " + player.getCredits() + " credits");
	        
	        Journal.addJournalEntry(player, "Installed a mulcher on the compost bin.");
	        Journal.saveGame(player);
	    } else {
	        System.out.println("Installation cancelled.");
	    }
	}

	/**
	 * Handles sprinkler system installation
	 */
	private static void installSprinklerSystem(Player1 player, Scanner scanner) {
	    System.out.println("\n💧 Sprinkler System Installation 💧");
	    System.out.println("Cost: 20 credits, 20 NRG");
	    System.out.println();
	    System.out.println("An automated sprinkler system for your garden plots.");
	    System.out.println();
	    System.out.println("Benefits:");
	    System.out.println("  ✓ Watering garden plots costs 0 NRG");
	    System.out.println("  ✓ Watering specific plots costs 0 NRG");
	    System.out.println("  ✓ Only applies to regular garden plots");
	    System.out.println("  ✗ Flower pots still require 1 NRG to water");
	    System.out.println("  ℹ️  Plants still dry out daily - you must remember to water!");
	    System.out.println();
	    
	    // Check resources
	    if (player.getCredits() < 20) {
	        System.out.println("❌ You don't have enough credits! Need 20, have " + player.getCredits());
	        System.out.println("Press Enter to continue...");
	        scanner.nextLine();
	        return;
	    }
	    
	    if (player.getNRG() < 20) {
	        System.out.println("❌ You don't have enough energy! Need 20 NRG, have " + player.getNRG());
	        System.out.println("Press Enter to continue...");
	        scanner.nextLine();
	        return;
	    }
	    
	    System.out.print("Install the sprinkler system? (yes/no): ");
	    String confirm = scanner.nextLine().toLowerCase();
	    
	    if (confirm.equals("yes")) {
	        player.setCredits(player.getCredits() - 20);
	        player.setNRG(player.getNRG() - 20);
	        player.installSprinklerSystem();
	        
	        System.out.println("\n✅ Sprinkler system installed successfully!");
	        System.out.println("Watering garden plots now costs 0 NRG!");
	        System.out.println("(Flower pots still require manual watering for 1 NRG each)");
	        System.out.println();
	        System.out.println("Remaining: " + player.getNRG() + " NRG | " + player.getCredits() + " credits");
	        
	        Journal.addJournalEntry(player, "Installed an automated sprinkler system.");
	        Journal.saveGame(player);
	    } else {
	        System.out.println("Installation cancelled.");
	    }
	}
}
/* BuildingActions.java
 * Handles all building-related actions including flower pot crafting and garden plot expansion
 * Created to modularize sunflowerSimulator.java
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

            switch (buildChoice) {
                case "1": // Craft Flower Pot
                    craftFlowerPot(player, scanner);
                    break;

                case "2": // Dig New Garden Plot
                    digNewGardenPlot(player, scanner);
                    break;

                case "3": // Return to main menu
                    inBuildMenu = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
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
        System.out.println();

        System.out.println("What would you like to build?");
        System.out.println("1: Craft a Flower Pot (20 credits, 2 NRG)");

        // Calculate cost for next garden plot
        int nextPlotNumber = currentPlots - placedFlowerPots + 1; // Number of regular plots + 1
        int plotNRGCost = (nextPlotNumber - 3) * 5 + 10; // 4th=10, 5th=15, 6th=20, etc.
        int plotCreditCost = (nextPlotNumber - 3) * 5 + 10;

        System.out.println("2: Dig a New Garden Plot (" + plotCreditCost + " credits, " + plotNRGCost + " NRG)");
        System.out.println("3: Return to Main Menu");
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
        int nextPlotNumber = currentPlots - placedFlowerPots + 1;
        int plotNRGCost = (nextPlotNumber - 3) * 5 + 10;
        int plotCreditCost = (nextPlotNumber - 3) * 5 + 10;
        
        System.out.println("\n🌱 Garden Plot Expansion 🌱");
        System.out.println("Cost: " + plotCreditCost + " credits, " + plotNRGCost + " NRG");
        System.out.println();
        System.out.println("This will be garden plot #" + (currentPlots + 1));
        System.out.println("Regular garden plots can plant any flower with no restrictions.");
        System.out.println("Each additional plot costs more energy and credits than the last.");

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
            System.out.println("Your garden now has " + (currentPlots + 1) + " total plots.");
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
}
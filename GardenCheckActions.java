/* GardenCheckActions.java
 * Handles garden checking and plot-specific actions
 * Updated: November 23, 2025 - Added compost bin fertilize all option
 */

import java.util.List;
import java.util.Scanner;

public class GardenCheckActions {
    
    /**
     * Handles the complete garden check workflow and plot-specific actions
     * @param player The player performing the action
     * @param scanner Scanner for user input
     */
    public static void handleGardenCheck(Player1 player, Scanner scanner) {
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
        System.out.println("5: Pick up flower pot (with plant)");
        
        // NEW: Add fertilize all option if compost bin exists
        if (player.hasCompostBin()) {
            System.out.println("6: Fertilize All (via Compost Bin)");
        }
        
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
        
        // NEW: Handle fertilize all via compost bin
        if (player.hasCompostBin() && gardenAction.equals("6")) {
            fertilizeAllViaCompost(player, scanner);
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
            waterPlot(player, selectedPlot);
            break;

        case "2": // Weed
            weedPlot(player, selectedPlot, plotChoice);
            break;

        case "3": // Fertilize
            fertilizePlot(player, selectedPlot);
            break;

        case "4": // Harvest
            harvestPlot(player, selectedPlot, scanner);
            break;
            
        case "5": // Pick up flower pot
            pickUpFlowerPot(player, selectedPlot, scanner);
            break;

        default:
            System.out.println("Invalid choice! Please try again.");
        }
    }
    
    /**
     * NEW: Fertilizes all plots via compost bin (50% NRG discount + soil upgrades)
     * @param player The player
     * @param scanner Scanner for user input
     */
    private static void fertilizeAllViaCompost(Player1 player, Scanner scanner) {
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

        int nrgCost = unfertilizedCount; // 1 NRG per plot (50% discount)
        boolean willUpgradeSoil = player.getCompostWitheredCount() >= 10;

        System.out.println("\n♻️ Fertilize All (Compost Bin) ♻️");
        System.out.println("  • Plots to fertilize: " + unfertilizedCount);
        System.out.println("  • NRG cost: " + nrgCost);

        if (willUpgradeSoil) {
            System.out.println("  • ✨ BONUS: Will upgrade soil quality in all plots!");
            
        }

        if (player.getNRG() < nrgCost) {
            System.out.println("\n❌ You don't have enough energy! Need " + nrgCost + " NRG, have " + player.getNRG());
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
                        System.out.println("  ✨ Plot soil upgraded: " + beforeSoil + " → " + afterSoil);
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

        System.out.println("\n✅ Fertilized " + fertilizedCount + " plots!");

        if (soilUpgradeCount > 0) {
            System.out.println("✨ Upgraded soil quality in " + soilUpgradeCount + " plots!");
            Journal.addJournalEntry(player, "Used compost bin to fertilize all plants and upgrade " + 
                    soilUpgradeCount + " plots' soil quality.");
        } else {
            Journal.addJournalEntry(player, "Used compost bin to fertilize " + fertilizedCount + " plants.");
        }

        System.out.println("Remaining NRG: " + player.getNRG());

        if (player.getCompostWitheredCount() > 0) {
            System.out.println("\n♻️ Withered flowers remaining in compost: " + player.getCompostWitheredCount() + "/10");
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
     * Waters a specific plot
     * UPDATED: Sprinkler system makes regular plots cost 0 NRG
     * @param player The player
     * @param selectedPlot The plot to water
     */
    private static void waterPlot(Player1 player, gardenPlot selectedPlot) {
        if (selectedPlot.isOccupied()) {
            if (selectedPlot.isWatered()) {
                System.out.println("This plot is already watered today!");
            } else {
                selectedPlot.waterPlot();
                
                // Calculate NRG cost
                int nrgCost = 1; // Default cost
                
                if (player.hasSprinklerSystem() && !selectedPlot.isFlowerPot()) {
                    nrgCost = 0; // Free for regular plots with sprinkler
                    System.out.println("💧 You watered the " + 
                            selectedPlot.getPlantedFlower().getName() + " using the sprinkler system (0 NRG).");
                } else {
                    System.out.println("You watered the " + 
                            selectedPlot.getPlantedFlower().getName() + ".");
                    if (selectedPlot.isFlowerPot()) {
                        System.out.println("💡 Good! Flower pot plants need daily watering to avoid durability loss.");
                    }
                }
                
                player.setNRG(player.getNRG() - nrgCost);
                
                if (nrgCost > 0) {
                    System.out.println("You used " + nrgCost + " NRG. Remaining NRG: " + player.getNRG());
                } else {
                    System.out.println("Remaining NRG: " + player.getNRG() + " (sprinkler system used)");
                }
                
                Journal.addJournalEntry(player, "Watered a " + 
                        selectedPlot.getPlantedFlower().getName() + ".");
            }
        } else {
            System.out.println("There's nothing planted in this plot to water!");
        }
    }
    
    /**
     * Weeds a specific plot
     * @param player The player
     * @param selectedPlot The plot to weed
     * @param plotChoice The plot number (1-based) for display
     */
    private static void weedPlot(Player1 player, gardenPlot selectedPlot, int plotChoice) {
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
     * @param player The player
     * @param selectedPlot The plot to fertilize
     */
    private static void fertilizePlot(Player1 player, gardenPlot selectedPlot) {
        if (selectedPlot.isOccupied()) {
            if (selectedPlot.isFertilized()) {
                System.out.println("This plot is already fertilized!");
            } else {
                selectedPlot.fertilizePlot();
                System.out.println("You fertilized the " + 
                        selectedPlot.getPlantedFlower().getName() + ".");
                player.setNRG(player.getNRG() - 2);
                System.out.println("You used 2 NRG. Remaining NRG: " + player.getNRG());
                
                if (player.hasCompostBin()) {
                    System.out.println("💡 Tip: Use the compost bin to fertilize all plots at once for 1 NRG each!");
                }
                
                Journal.addJournalEntry(player, "Fertilized a " + 
                        selectedPlot.getPlantedFlower().getName() + ".");
            }
        } else {
            System.out.println("There's nothing planted in this plot to fertilize!");
        }
    }
    
    /**
     * Harvests a plant from a specific plot (for regular plots only)
     * For flower pots, use pickUpFlowerPot instead
     * @param player The player
     * @param selectedPlot The plot to harvest from
     * @param scanner Scanner for user input
     */
    private static void harvestPlot(Player1 player, gardenPlot selectedPlot, Scanner scanner) {
        if (!selectedPlot.isOccupied()) {
            System.out.println("There's nothing planted in this plot to harvest!");
            return;
        }

        if (selectedPlot.isFlowerPot()) {
            System.out.println("This is a flower pot! Use option 5 to pick it up with the plant.");
            return;
        }

        Flower plant = selectedPlot.getPlantedFlower();
        String growthStage = plant.getGrowthStage();

        if (growthStage.equals("Seed") || growthStage.equals("Seedling")) {
            System.out.println("This plant is too young to harvest!");
            return;
        }

        // Regular harvest for Bloomed/Matured/Withered/Mutated
        Flower harvestedFlower = selectedPlot.harvestFlower();
        player.addToInventory(harvestedFlower);

        System.out.println("You harvested the " + harvestedFlower.getName() + 
                " (" + harvestedFlower.getGrowthStage() + ").");
        System.out.println("It has been added to your inventory.");

        if (player.hasSeedStartingTray() &&
                (growthStage.equals("Withered") || growthStage.equals("Mutated"))) {
            Flower seed = FlowerRegistry.createSeed(harvestedFlower.getName());
            if (seed != null) {
                player.addToInventory(seed);
                System.out.println("🌱 Seed Starting Tray produced 1 " + harvestedFlower.getName() + " seed.");
            }
        }

        player.setNRG(player.getNRG() - 2);
        System.out.println("You used 2 NRG. Remaining NRG: " + player.getNRG());
        Journal.addJournalEntry(player, "Harvested a " + harvestedFlower.getName() + 
                " (" + harvestedFlower.getGrowthStage() + ").");
    }
    
    /**
     * Picks up a flower pot with its plant at any stage
     * The entire pot + plant is moved to inventory
     * @param player The player
     * @param selectedPlot The plot to pick up
     * @param scanner Scanner for user input
     */
    private static void pickUpFlowerPot(Player1 player, gardenPlot selectedPlot, Scanner scanner) {
        if (!selectedPlot.isFlowerPot()) {
            System.out.println("This is not a flower pot! You can only pick up flower pots.");
            return;
        }
        
        if (!selectedPlot.isOccupied()) {
            System.out.println("This flower pot is empty! Use the build menu to pick up empty pots.");
            System.out.print("Pick up the empty pot? (yes/no): ");
            String confirm = scanner.nextLine().toLowerCase();
            
            if (confirm.equals("yes")) {
                player.getGardenPlots().remove(selectedPlot);
                player.addToInventory(selectedPlot);
                
                System.out.println("✅ Empty flower pot added to your inventory!");
                player.setNRG(player.getNRG() - 1);
                System.out.println("You used 1 NRG. Remaining NRG: " + player.getNRG());
                Journal.addJournalEntry(player, "Picked up an empty flower pot.");
            } else {
                System.out.println("Cancelled.");
            }
            return;
        }
        
        Flower plant = selectedPlot.getPlantedFlower();
        
        System.out.println("\n🪴 This flower pot contains: " + plant.getName() + 
                " (" + plant.getGrowthStage() + ")");
        System.out.println("Days planted: " + plant.getDaysPlanted());
        System.out.println("Durability: " + plant.getDurability());
        System.out.println("\nPick up the entire flower pot with the plant? (yes/no): ");
        
        String confirm = scanner.nextLine().toLowerCase();
        
        if (confirm.equals("yes")) {
            // Remove from garden plots
            player.getGardenPlots().remove(selectedPlot);
            
            // Add entire pot (with plant) to inventory
            player.addToInventory(selectedPlot);
            
            System.out.println("✅ You picked up the flower pot with " + plant.getName() + 
                    " (" + plant.getGrowthStage() + ")!");
            System.out.println("The entire pot has been added to your inventory.");
            
            player.setNRG(player.getNRG() - 1);
            System.out.println("You used 1 NRG. Remaining NRG: " + player.getNRG());
            
            Journal.addJournalEntry(player, "Picked up a flower pot with " + plant.getName() + 
                    " (" + plant.getGrowthStage() + ").");
        } else {
            System.out.println("Pickup cancelled.");
        }
    }
}
/* GardenCheckActions.java
 * Handles garden checking and plot-specific actions
 * Created to modularize sunflowerSimulator.java
 * 
 * Includes:
 * - Garden display
 * - Water single plot
 * - Weed single plot
 * - Fertilize single plot
 * - Harvest single plot (with flower pot mechanics)
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

        default:
            System.out.println("Invalid choice! Please try again.");
        }
    }
    
    /**
     * Waters a specific plot
     * @param player The player
     * @param selectedPlot The plot to water
     */
    private static void waterPlot(Player1 player, gardenPlot selectedPlot) {
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
     * Includes special flower pot behavior for seeds/seedlings
     * @param player The player
     * @param selectedPlot The plot to harvest from
     * @param scanner Scanner for user input
     */
    private static void harvestPlot(Player1 player, gardenPlot selectedPlot, Scanner scanner) {
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
            // Regular harvest for Bloomed/Matured/Withered/Mutated
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
}
/* GardenCheckActions.java
 * Handles garden checking and plot-specific actions
 * Created to modularize sunflowerSimulator.java
 * 
 * UPDATES:
 * - Modified harvest to allow picking up any flower pot + plant at any stage (fix #3)
 * - Flower pots can always be picked up with their contents
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
            
        case "5": // Pick up flower pot (fix #3)
            pickUpFlowerPot(player, selectedPlot, scanner);
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

        player.setNRG(player.getNRG() - 2);
        System.out.println("You used 2 NRG. Remaining NRG: " + player.getNRG());
        Journal.addJournalEntry(player, "Harvested a " + harvestedFlower.getName() + 
                " (" + harvestedFlower.getGrowthStage() + ").");
    }
    
    /**
     * Picks up a flower pot with its plant at any stage (fix #3)
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
/* PlantingActions.java
 * Handles all planting-related actions including seed selection and flower pot placement
 * Created to modularize sunflowerSimulator.java
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PlantingActions {
    
    /**
     * Handles the complete planting workflow including plot/pot selection and seed planting
     * @param player The player performing the action
     * @param scanner Scanner for user input
     * @return true if planting was successful, false otherwise
     */
    public static boolean handlePlanting(Player1 player, Scanner scanner) {
        if (player.getNRG() <= 0) {
            System.out.println("You're too tired to do that. You need to go to bed first!");
            return false;
        }

        // Check if player has any seeds
        List<Flower> availableSeeds = new ArrayList<>();
        for (Object item : player.getInventory()) {
            if (item instanceof Flower && ((Flower)item).getGrowthStage().equals("Seed")) {
                availableSeeds.add((Flower)item);
            }
        }

        // Check if player has flower pots in inventory
        List<gardenPlot> availableFlowerPots = new ArrayList<>();
        for (Object item : player.getInventory()) {
            if (item instanceof gardenPlot && ((gardenPlot)item).isFlowerPot()) {
                availableFlowerPots.add((gardenPlot)item);
            }
        }

        if (availableSeeds.isEmpty()) {
            System.out.println("You don't have any seeds to plant! Visit the shop to buy some.");
            return false;
        }

        // Display garden plots and options
        displayGardenPlots(player.getGardenPlots());

        // Show option to place flower pot if player has any
        if (!availableFlowerPots.isEmpty()) {
            System.out.println("\n🪴 You have " + availableFlowerPots.size() + " flower pot(s) in your backpack!");
            System.out.println("You can place a flower pot and plant in it.");
        }

        System.out.println("\nWhat would you like to do?");
        System.out.println("1: Plant in an existing plot");
        if (!availableFlowerPots.isEmpty()) {
            System.out.println("2: Place a flower pot and plant in it");
        }
        System.out.println("0: Cancel");

        System.out.print("\nChoice: ");
        String plantChoice = scanner.nextLine();

        if (plantChoice.equals("0")) {
            System.out.println("Planting cancelled.");
            return false;
        }

        gardenPlot selectedPlot = null;
        boolean placingFlowerPot = false;

        if (plantChoice.equals("2") && !availableFlowerPots.isEmpty()) {
            // Place flower pot from inventory
            selectedPlot = placeFlowerPotFromInventory(player, availableFlowerPots);
            placingFlowerPot = true;
        } else if (plantChoice.equals("1")) {
            // Select existing plot
            selectedPlot = selectExistingPlot(player.getGardenPlots(), scanner);
            if (selectedPlot == null) {
                return false; // User cancelled or invalid selection
            }
        } else {
            System.out.println("Invalid choice.");
            return false;
        }

        // Select and plant seed
        boolean success = selectAndPlantSeed(player, selectedPlot, availableSeeds, scanner);
        
        // If planting failed and we just placed a flower pot, return it to inventory
        if (!success && placingFlowerPot) {
            player.getGardenPlots().remove(selectedPlot);
            player.addToInventory(selectedPlot);
            System.out.println("Flower pot returned to inventory.");
        }
        
        return success;
    }
    
    /**
     * Displays all garden plots with their current status
     * @param gardenPlots List of garden plots to display
     */
    private static void displayGardenPlots(List<gardenPlot> gardenPlots) {
        System.out.println("\n🌱 Your Garden Plots 🌱");
        for (int i = 0; i < gardenPlots.size(); i++) {
            gardenPlot plot = gardenPlots.get(i);
            String plotType = plot.isFlowerPot() ? "[🪴 Flower Pot]" : "[📦 Garden Plot]";
            System.out.println("Plot #" + (i+1) + " " + plotType + ": " + 
                    (plot.isOccupied() ? 
                            "[Occupied - " + plot.getPlantedFlower().getName() + " (" + 
                            plot.getPlantedFlower().getGrowthStage() + ")]" : 
                            "[Empty]"));
        }
    }
    
    /**
     * Places a flower pot from inventory into the garden
     * @param player The player
     * @param availableFlowerPots List of flower pots in inventory
     * @return The placed flower pot
     */
    private static gardenPlot placeFlowerPotFromInventory(Player1 player, List<gardenPlot> availableFlowerPots) {
        gardenPlot selectedPlot = availableFlowerPots.get(0); // Get first flower pot
        player.removeFromInventory(selectedPlot); // Remove from inventory
        player.addFlowerPotToGarden(selectedPlot); // Add to garden
        System.out.println("✅ You placed a flower pot in your garden!");
        return selectedPlot;
    }
    
    /**
     * Allows player to select an existing garden plot
     * @param gardenPlots List of garden plots
     * @param scanner Scanner for user input
     * @return Selected plot or null if cancelled/invalid
     */
    private static gardenPlot selectExistingPlot(List<gardenPlot> gardenPlots, Scanner scanner) {
        System.out.print("\nWhich plot would you like to plant in? (1-" + gardenPlots.size() + 
                ", or 0 to cancel): ");
        int plotChoice;
        try {
            plotChoice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return null;
        }

        if (plotChoice == 0) {
            System.out.println("Planting cancelled.");
            return null;
        }

        if (plotChoice < 1 || plotChoice > gardenPlots.size()) {
            System.out.println("Invalid plot number. Please choose a valid plot.");
            return null;
        }

        gardenPlot selectedPlot = gardenPlots.get(plotChoice - 1);

        if (selectedPlot.isOccupied()) {
            System.out.println("This plot is already occupied! Choose an empty plot.");
            return null;
        }
        
        return selectedPlot;
    }
    
    /**
     * Displays available seeds and handles seed selection and planting
     * @param player The player
     * @param selectedPlot The plot to plant in
     * @param availableSeeds List of available seeds
     * @param scanner Scanner for user input
     * @return true if planting was successful
     */
    private static boolean selectAndPlantSeed(Player1 player, gardenPlot selectedPlot, 
                                               List<Flower> availableSeeds, Scanner scanner) {
        // Display available seeds
        System.out.println("\nAvailable Seeds:");
        for (int i = 0; i < availableSeeds.size(); i++) {
            Flower seed = availableSeeds.get(i);
            displaySeedInfo(seed, i + 1, selectedPlot);
        }

        // Ask which seed to plant
        System.out.print("\nWhich seed would you like to plant? (1-" + availableSeeds.size() + 
                ", or 0 to cancel): ");
        int seedChoice;
        try {
            seedChoice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return false;
        }

        if (seedChoice == 0) {
            System.out.println("Planting cancelled.");
            return false;
        }

        if (seedChoice < 1 || seedChoice > availableSeeds.size()) {
            System.out.println("Invalid seed number. Please choose a valid seed.");
            return false;
        }

        Flower selectedSeed = availableSeeds.get(seedChoice - 1);

        // Check flower pot restrictions
        if (!canPlantInPlot(selectedPlot, selectedSeed)) {
            return false;
        }

        // Plant the seed
        return plantSeed(player, selectedPlot, selectedSeed);
    }
    
    /**
     * Displays information about a seed including difficulty and restrictions
     * @param seed The seed to display
     * @param index The display index
     * @param selectedPlot The plot it would be planted in
     */
    private static void displaySeedInfo(Flower seed, int index, gardenPlot selectedPlot) {
        int difficulty = FlowerRegistry.getFlowerDifficulty(seed.getName());
        String species = FlowerRegistry.getFlowerInfo(seed.getName());

        // Build difficulty stars
        StringBuilder stars = new StringBuilder();
        for (int j = 0; j < difficulty; j++) {
            stars.append("★");
        }
        for (int j = difficulty; j < 5; j++) {
            stars.append("☆");
        }

        System.out.print(index + ": " + seed.getName() + " " + stars + " - Value: " + seed.getCost());

        // Show if seed can't be planted in flower pot
        if (selectedPlot != null && selectedPlot.isFlowerPot() && !selectedPlot.canPlantInFlowerPot(seed)) {
            if (difficulty >= 4) {
                System.out.print(" [❌ Too difficult for flower pot]");
            } else if (species != null && (species.toLowerCase().contains("bush") || 
                                          species.toLowerCase().contains("tree"))) {
                System.out.print(" [❌ Bush/Tree - can't use flower pot]");
            }
        }
        System.out.println();
    }
    
    /**
     * Checks if a seed can be planted in the selected plot
     * @param selectedPlot The plot to plant in
     * @param selectedSeed The seed to plant
     * @return true if planting is allowed
     */
    private static boolean canPlantInPlot(gardenPlot selectedPlot, Flower selectedSeed) {
        if (selectedPlot.isFlowerPot() && !selectedPlot.canPlantInFlowerPot(selectedSeed)) {
            System.out.println("\n❌ This flower can't be planted in a flower pot!");
            int difficulty = FlowerRegistry.getFlowerDifficulty(selectedSeed.getName());
            String species = FlowerRegistry.getFlowerInfo(selectedSeed.getName());

            if (difficulty >= 4) {
                System.out.println("   Reason: Too difficult (4★+ flowers need regular garden plots)");
            }
            if (species != null && (species.toLowerCase().contains("bush") || 
                                   species.toLowerCase().contains("tree"))) {
                System.out.println("   Reason: Bushes and trees need regular garden plots");
            }
            return false;
        }
        return true;
    }
    
    /**
     * Plants a seed in the selected plot and handles energy/inventory updates
     * @param player The player
     * @param selectedPlot The plot to plant in
     * @param selectedSeed The seed to plant
     * @return true if planting was successful
     */
    private static boolean plantSeed(Player1 player, gardenPlot selectedPlot, Flower selectedSeed) {
        if (selectedPlot.plantFlower(selectedSeed)) {
            // Remove the seed from inventory
            player.removeFromInventory(selectedSeed);

            String plotType = selectedPlot.isFlowerPot() ? "flower pot" : "plot";
            System.out.println("\n✅ You successfully planted " + selectedSeed.getName() + 
                    " in the " + plotType + "!");

            if (selectedPlot.isFlowerPot()) {
                System.out.println("💡 Tip: Flower pots don't need weeding, but plants take double durability damage if not watered!");
            } else {
                System.out.println("Remember to water it regularly for it to grow!");
            }

            // Add journal entry
            Journal.addJournalEntry(player, "Planted a " + selectedSeed.getName() + " seed.");

            // Use energy
            player.setNRG(player.getNRG() - 2);
            System.out.println("You used 2 NRG. Remaining NRG: " + player.getNRG());
            return true;
        } else {
            System.out.println("\n❌ Something went wrong. The seed couldn't be planted.");
            return false;
        }
    }
}
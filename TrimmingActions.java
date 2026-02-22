/* TrimmingActions.java
 * Handles all plant trimming actions with enhanced mechanics
 * Created to modularize sunflowerSimulator.java
 * 
 * TRIMMING MECHANICS (UPDATED):
 * - Bloomed: Durability +2 (only stage that increases durability)
 * - Matured: 1 bloomed flower (NO durability increase)
 * - Mutated: 5-8 bloomed flowers (NO durability increase)
 * - Withered: Same as harvesting (removes plant, adds to inventory)
 * - Each plant can only be trimmed ONCE per day
 * 
 * UPDATES:
 * - Added daily trim tracking (resets at day advancement)
 * - Removed durability bonuses from matured/mutated stages
 * - Trimming withered plants now harvests them
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class TrimmingActions {
    
    private static final Random random = new Random();
    
    // Track which plots have been trimmed today (plot index as key)
    private static Set<Integer> trimmedPlotsToday = new HashSet<>();
    private static int lastTrimDay = -1;
    
    /**
     * Resets the daily trim tracker when a new day begins
     * MUST be called from sunflowerSimulator.advanceDay()
     * @param currentDay The current day number
     */
    public static void resetDailyTrims(int currentDay) {
        if (currentDay != lastTrimDay) {
            trimmedPlotsToday.clear();
            lastTrimDay = currentDay;
        }
    }
    
    /**
     * Handles the complete trimming workflow
     * @param player The player performing the action
     * @param scanner Scanner for user input
     */
    public static void handleTrimming(Player1 player, Scanner scanner) {
        // Reset trim tracker if day has changed
        resetDailyTrims(player.getDay());
        
        if (player.getNRG() <= 0) {
            System.out.println("You're too tired to do that. You need to go to bed first!");
            return;
        }

        // Check if there are any trimmable plants
        List<Integer> trimmablePlotIndices = getTrimmablePlots(player);
        
        if (trimmablePlotIndices.isEmpty()) {
            System.out.println("You don't have any plants that need trimming yet!");
            System.out.println("Plants need to be at least in the 'Bloomed' stage to be trimmed.");
            return;
        }

        if (player.hasBuzzsaw()) {
            trimAllPlants(player, trimmablePlotIndices);
            return;
        }

        // Display garden with trimmable plants
        displayTrimmableGarden(player, trimmablePlotIndices);

        // Ask which plot to trim
        int plotChoice = selectPlotToTrim(player, trimmablePlotIndices, scanner);

        if (plotChoice == -1) {
            System.out.println("Trimming cancelled.");
            return;
        }

        // Perform the trim action
        trimPlant(player, plotChoice);
    }
    
    /**
     * Gets indices of all plots with trimmable plants (not already trimmed today)
     * @param player The player
     * @return List of plot indices that can be trimmed
     */
    private static List<Integer> getTrimmablePlots(Player1 player) {
        List<Integer> trimmablePlotIndices = new ArrayList<>();
        List<gardenPlot> gardenPlots = player.getGardenPlots();
        
        for (int i = 0; i < gardenPlots.size(); i++) {
            gardenPlot plot = gardenPlots.get(i);
            if (plot.isOccupied()) {
                String stage = plot.getPlantedFlower().getGrowthStage();
                // Include Withered in trimmable stages (it harvests instead)
                if ((stage.equals("Bloomed") || stage.equals("Matured") || 
                     stage.equals("Mutated") || stage.equals("Withered")) &&
                    !trimmedPlotsToday.contains(i)) {
                    trimmablePlotIndices.add(i);
                }
            }
        }
        
        return trimmablePlotIndices;
    }
    
    /**
     * Displays the garden with trimmable plants highlighted
     * @param player The player
     * @param trimmablePlotIndices List of indices that can be trimmed
     */
    private static void displayTrimmableGarden(Player1 player, List<Integer> trimmablePlotIndices) {
        System.out.println("\n🌱 Your Garden Plants 🌱");
        List<gardenPlot> gardenPlots = player.getGardenPlots();
        
        for (int i = 0; i < gardenPlots.size(); i++) {
            gardenPlot plot = gardenPlots.get(i);
            String plotType = plot.isFlowerPot() ? "[🪴]" : "[📦]";
            
            if (plot.isOccupied()) {
                Flower plant = plot.getPlantedFlower();
                String stage = plant.getGrowthStage();
                String trimInfo = getTrimInfo(stage, trimmedPlotsToday.contains(i));
                
                System.out.println("Plot #" + (i+1) + " " + plotType + ": " + plant.getName() + 
                        " (" + stage + ")" + trimInfo);
            } else {
                System.out.println("Plot #" + (i+1) + " " + plotType + ": [Empty]");
            }
        }
    }
    
    /**
     * Gets descriptive trimming information for each growth stage
     * @param stage The growth stage
     * @param alreadyTrimmed Whether this plant was already trimmed today
     * @return Description of what trimming will do
     */
    private static String getTrimInfo(String stage, boolean alreadyTrimmed) {
        if (alreadyTrimmed) {
            return " - [Already trimmed today]";
        }
        
        switch (stage) {
            case "Bloomed":
                return " - Can be trimmed (Durability +2)";
            case "Matured":
                return " - Can be trimmed (+1 Bloomed flower)";
            case "Mutated":
                return " - Can be trimmed (5-8 Bloomed flowers!)";
            case "Withered":
                return " - Can be trimmed (harvests plant)";
            default:
                return "";
        }
    }
    
    /**
     * Allows player to select which plot to trim
     * @param player The player
     * @param trimmablePlotIndices List of valid plot indices
     * @param scanner Scanner for user input
     * @return Selected plot index (0-based) or -1 if cancelled
     */
    private static int selectPlotToTrim(Player1 player, List<Integer> trimmablePlotIndices, Scanner scanner) {
        System.out.print("\nWhich plot would you like to trim? (");
        for (int i = 0; i < trimmablePlotIndices.size(); i++) {
            System.out.print((trimmablePlotIndices.get(i) + 1));
            if (i < trimmablePlotIndices.size() - 1) {
                System.out.print(", ");
            }
        }
        System.out.print(", or 0 to cancel): ");

        int plotChoice;
        try {
            plotChoice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return -1;
        }

        if (plotChoice == 0) {
            return -1;
        }

        if (plotChoice < 1 || plotChoice > player.getGardenPlots().size() || 
                !trimmablePlotIndices.contains(plotChoice - 1)) {
            System.out.println("Invalid plot choice. Please select a plot with a trimmable plant.");
            return -1;
        }
        
        return plotChoice - 1; // Return 0-based index
    }
    
    /**
     * Performs the trim action on the selected plant
     * @param player The player
     * @param plotIndex The 0-based index of the plot to trim
     */
    private static void trimPlant(Player1 player, int plotIndex) {
        if (player.getNRG() < 1) {
            System.out.println("You do not have enough NRG to trim.");
            return;
        }
        player.setNRG(player.getNRG() - 1);
        executeTrim(player, plotIndex, true);
        System.out.println("You used 1 NRG. Remaining NRG: " + player.getNRG());
        Journal.saveGame(player);
    }
    

    private static void trimAllPlants(Player1 player, List<Integer> trimmablePlotIndices) {
        int individualCost = trimmablePlotIndices.size();
        int buzzsawCost = (int) Math.ceil(individualCost * 0.5);

        if (player.getNRG() < buzzsawCost) {
            System.out.println("❌ Not enough NRG for Trim All. Need " + buzzsawCost + ", have " + player.getNRG());
            return;
        }

        System.out.println("\n🪚 Buzzsaw engaged! Trimming all eligible plants...");
        player.setNRG(player.getNRG() - buzzsawCost);

        int trimmedCount = 0;
        List<Integer> toTrim = new ArrayList<>(trimmablePlotIndices);
        for (int plotIndex : toTrim) {
            if (executeTrim(player, plotIndex, false)) {
                trimmedCount++;
            }
        }

        System.out.println("✅ Trimmed " + trimmedCount + " plants for " + buzzsawCost + " NRG (50% bulk cost).");
        System.out.println("Remaining NRG: " + player.getNRG());
        Journal.addJournalEntry(player, "Used buzzsaw to trim all eligible plants (" + trimmedCount + ").");
        Journal.saveGame(player);
    }

    private static boolean executeTrim(Player1 player, int plotIndex, boolean verboseHeader) {
        gardenPlot selectedPlot = player.getGardenPlots().get(plotIndex);
        if (!selectedPlot.isOccupied()) {
            return false;
        }

        Flower plant = selectedPlot.getPlantedFlower();
        String stage = plant.getGrowthStage();
        String plantName = plant.getName();

        if (verboseHeader) {
            System.out.println("\nYou carefully trim the " + plantName + "...");
        }

        switch (stage) {
            case "Bloomed":
                trimBloomedPlant(player, plant, plantName);
                trimmedPlotsToday.add(plotIndex);
                return true;
            case "Matured":
                trimMaturedPlant(player, plant, plantName);
                trimmedPlotsToday.add(plotIndex);
                return true;
            case "Mutated":
                trimMutatedPlant(player, plant, plantName);
                trimmedPlotsToday.add(plotIndex);
                return true;
            case "Withered":
                trimWitheredPlant(player, selectedPlot, plant, plantName);
                return true;
            default:
                return false;
        }
    }

    /**
     * Handles trimming a bloomed plant (ONLY stage that increases durability)
     * @param player The player
     * @param plant The plant being trimmed
     * @param plantName The plant's name
     */
    private static void trimBloomedPlant(Player1 player, Flower plant, String plantName) {
        plant.setDurability(plant.getDurability() + 2);
        
        System.out.println("✨ The bloomed " + plantName + " looks healthier now!");
        System.out.println("   Durability increased by 2.");
        System.out.println("   💡 Tip: Bloomed is the only stage where trimming increases durability!");
        
        Journal.addJournalEntry(player, "Trimmed a bloomed " + plantName + ".");
    }
    
    /**
     * Handles trimming a matured plant (NO durability increase)
     * @param player The player
     * @param plant The plant being trimmed
     * @param plantName The plant's name
     */
    private static void trimMaturedPlant(Player1 player, Flower plant, String plantName) {
        // NO durability increase for matured plants
        
        // Create a bloomed flower to add to inventory
        double bloomedValue = FlowerRegistry.getFlowerValue(plantName, "Bloomed");
        FlowerInstance bloomedFlower = new FlowerInstance(
            plantName, 
            "Bloomed", 
            0, // Not planted
            plant.getDurability(), 
            1, // NRG restored (seeds only, but setting default)
            bloomedValue
        );
        
        player.addToInventory(bloomedFlower);
        
        System.out.println("🌸 You carefully trim the mature " + plantName + "!");
        System.out.println("   You harvested 1 bloomed " + plantName + " flower!");
        System.out.println("   (The plant remains in the ground but gains no durability)");
        
        Journal.addJournalEntry(player, "Trimmed a matured " + plantName + " and harvested 1 bloomed flower.");
    }
    
    /**
     * Handles trimming a mutated plant (NO durability increase)
     * @param player The player
     * @param plant The plant being trimmed
     * @param plantName The plant's name
     */
    private static void trimMutatedPlant(Player1 player, Flower plant, String plantName) {
        // Mutated plants yield 5-8 bloomed flowers but NO durability increase
        int flowerCount = 5 + random.nextInt(4); // 5-8 flowers
        double bloomedValue = FlowerRegistry.getFlowerValue(plantName, "Bloomed");
        
        // Add multiple bloomed flowers to inventory
        for (int i = 0; i < flowerCount; i++) {
            FlowerInstance bloomedFlower = new FlowerInstance(
                plantName, 
                "Bloomed", 
                0, 
                plant.getDurability(), 
                1,
                bloomedValue
            );
            player.addToInventory(bloomedFlower);
        }
        
        System.out.println("✨💫 The mutated " + plantName + " produces an abundance of flowers!");
        System.out.println("   You harvested " + flowerCount + " bloomed " + plantName + " flowers!");
        System.out.println("   (The mutated plant remains in the ground)");
        System.out.println("   🔮 Mutated plants don't gain durability from trimming.");

        if (player.hasSeedStartingTray()) {
            Flower seed = FlowerRegistry.createSeed(plantName);
            if (seed != null) {
                player.addToInventory(seed);
                System.out.println("   🌱 Seed Starting Tray produced 1 " + plantName + " seed.");
            }
        }

        Journal.addJournalEntry(player, "Trimmed a mutated " + plantName + " and harvested " + 
                              flowerCount + " bloomed flowers!");
    }
    
    /**
     * Handles trimming a withered plant (same as harvesting - removes plant)
     * @param player The player
     * @param selectedPlot The plot containing the withered plant
     * @param plant The plant being trimmed
     * @param plantName The plant's name
     */
    private static void trimWitheredPlant(Player1 player, gardenPlot selectedPlot, 
                                          Flower plant, String plantName) {
        // Trimming a withered plant is the same as harvesting it
        Flower harvestedFlower = selectedPlot.harvestFlower();
        player.addToInventory(harvestedFlower);
        
        System.out.println("🥀 You trim away the withered " + plantName + ".");
        System.out.println("   The plant has been removed and added to your inventory.");
        System.out.println("   💡 Trimming withered plants is the same as harvesting them.");

        if (player.hasSeedStartingTray()) {
            Flower seed = FlowerRegistry.createSeed(plantName);
            if (seed != null) {
                player.addToInventory(seed);
                System.out.println("   🌱 Seed Starting Tray produced 1 " + plantName + " seed.");
            }
        }

        Journal.addJournalEntry(player, "Trimmed (harvested) a withered " + plantName + ".");
    }
}
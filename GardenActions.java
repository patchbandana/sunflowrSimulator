/* GardenActions.java
 * Handles garden maintenance actions (watering and weeding)
 * Created to modularize sunflowerSimulator.java
 */

import java.util.ArrayList;
import java.util.List;

public class GardenActions {
    
    /**
     * Waters all plants in the garden that need watering
     * @param player The player performing the action
     * @return true if any plants were watered, false otherwise
     */
    public static boolean waterGarden(Player1 player) {
        if (player.getNRG() <= 0) {
            System.out.println("You're too tired to do that. You need to go to bed first!");
            return false;
        }
        
        // Check for plants that need watering
        List<gardenPlot> dryPlots = new ArrayList<>();
        for (gardenPlot plot : player.getGardenPlots()) {
            if (plot.isOccupied() && !plot.isWatered()) {
                dryPlots.add(plot);
            }
        }
        
        if (dryPlots.isEmpty()) {
            System.out.println("All of your plants are already watered for today!");
            return false;
        }
        
        System.out.println("You water your plants.");
        if (player.getPlacedFlowerPotCount() > 0) {
            System.out.println("(Remember: Flower pot plants take extra durability damage if not watered!)");
        }
        
        // Water all plots that need it
        int waterCount = 0;
        for (gardenPlot plot : dryPlots) {
            if (player.getNRG() <= 0) {
                break; // Stop if out of energy
            }
            
            if (plot.waterPlot()) {
                waterCount++;
                player.setNRG(player.getNRG() - 1);
            }
        }
        
        if (waterCount > 0) {
            System.out.println("You watered " + waterCount + " plants.");
            System.out.println("Remaining NRG: " + player.getNRG());
            Journal.addJournalEntry(player, "Watered " + waterCount + " plants in the garden.");
            return true;
        } else {
            System.out.println("You didn't find any plants that needed watering.");
            return false;
        }
    }
    
    /**
     * Weeds all garden plots that need weeding (excludes flower pots)
     * @param player The player performing the action
     * @return true if any plots were weeded, false otherwise
     */
    public static boolean weedGarden(Player1 player) {
        if (player.getNRG() <= 0) {
            System.out.println("You're too tired to do that. You need to go to bed first!");
            return false;
        }
        
        // Check if there are any plots that need weeding (excluding flower pots)
        List<gardenPlot> weedyPlots = new ArrayList<>();
        for (gardenPlot plot : player.getGardenPlots()) {
            if (!plot.isFlowerPot() && !plot.isWeeded()) {
                weedyPlots.add(plot);
            }
        }
        
        if (weedyPlots.isEmpty()) {
            System.out.println("Your garden is already free of weeds!");
            System.out.println("(Note: Flower pots don't need weeding)");
            return false;
        }
        
        System.out.println("You spend some time weeding your garden.");
        
        // Weed all plots that need it
        int weedCount = 0;
        for (gardenPlot plot : weedyPlots) {
            if (player.getNRG() <= 0) {
                break; // Stop if out of energy
            }
            
            if (plot.weedPlot()) {
                weedCount++;
                player.setNRG(player.getNRG() - 1);
            }
        }
        
        if (weedCount > 0) {
            System.out.println("You weeded " + weedCount + " garden plots.");
            System.out.println("Remaining NRG: " + player.getNRG());
            Journal.addJournalEntry(player, "Spent time weeding " + weedCount + " garden plots.");
            return true;
        } else {
            System.out.println("You didn't find any weeds to remove.");
            return false;
        }
    }
}
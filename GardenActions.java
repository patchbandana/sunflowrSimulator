/* GardenActions.java
 * Handles garden maintenance actions (watering and weeding)
 * Created to modularize sunflowerSimulator.java
 * 
 * UPDATED: 
 * - Sprinkler system integration (0 NRG for garden plots)
 * - Mulcher activation on weed garden
 */

import java.util.ArrayList;
import java.util.List;

public class GardenActions {
    
    /**
     * Waters all plants in the garden that need watering
     * UPDATED: Sprinkler system makes garden plots cost 0 NRG
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
        int dryRegularPlots = 0;
        int dryFlowerPots = 0;
        
        for (gardenPlot plot : player.getGardenPlots()) {
            if (plot.isOccupied() && !plot.isWatered()) {
                dryPlots.add(plot);
                if (plot.isFlowerPot()) {
                    dryFlowerPots++;
                } else {
                    dryRegularPlots++;
                }
            }
        }
        
        if (dryPlots.isEmpty()) {
            System.out.println("All of your plants are already watered for today!");
            return false;
        }
        
        // Calculate NRG cost
        int nrgCost = 0;
        if (player.hasSprinklerSystem()) {
            // Only flower pots cost NRG with sprinkler system
            nrgCost = dryFlowerPots;
            System.out.println("[Sprinkler] Watering garden with sprinkler system...");
            if (dryFlowerPots > 0) {
                System.out.println("(Flower pots still require manual watering: " + dryFlowerPots + " x 1 NRG)");
            }
        } else {
            // Without sprinkler, all plots cost NRG
            nrgCost = dryPlots.size();
            System.out.println("You water your plants.");
        }
        
        if (player.getPlacedFlowerPotCount() > 0) {
            System.out.println("(Remember: Flower pot plants take extra durability damage if not watered!)");
        }
        
        // Check if player has enough NRG
        if (player.getNRG() < nrgCost) {
            System.out.println("[X] You don't have enough energy to water everything!");
            System.out.println("Need " + nrgCost + " NRG, have " + player.getNRG());
            return false;
        }
        
        // Water all plots
        int waterCount = 0;
        for (gardenPlot plot : dryPlots) {
            if (plot.waterPlot()) {
                waterCount++;
            }
        }
        
        // Deduct NRG
        player.setNRG(player.getNRG() - nrgCost);
        
        if (waterCount > 0) {
            System.out.println("[OK] You watered " + waterCount + " plants.");
            if (player.hasSprinklerSystem() && dryRegularPlots > 0) {
                System.out.println("   " + dryRegularPlots + " garden plots watered automatically (0 NRG)");
            }
            if (dryFlowerPots > 0) {
                System.out.println("   " + dryFlowerPots + " flower pots watered manually (" + dryFlowerPots + " NRG)");
            }
            System.out.println("Remaining NRG: " + player.getNRG());
            
            String journalEntry = "Watered " + waterCount + " plants in the garden.";
            if (player.hasSprinklerSystem()) {
                journalEntry = "Used sprinkler system to water " + waterCount + " plants.";
            }
            Journal.addJournalEntry(player, journalEntry);
            return true;
        } else {
            System.out.println("You didn't find any plants that needed watering.");
            return false;
        }
    }
    
    /**
     * Weeds all garden plots that need weeding (excludes flower pots)
     * UPDATED: Activates mulcher effect for 7 days
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
            System.out.println("[OK] You weeded " + weedCount + " garden plots.");
            System.out.println("Remaining NRG: " + player.getNRG());
            
            // Activate mulcher effect if installed
            if (player.hasMulcher()) {
                player.activateMulcherEffect();
                System.out.println("[Mulcher] Mulcher activated! Weeds will grow at 0.25x speed for 7 days.");
            }
            
            Journal.addJournalEntry(player, "Spent time weeding " + weedCount + " garden plots.");
            return true;
        } else {
            System.out.println("You didn't find any weeds to remove.");
            return false;
        }
    }
}
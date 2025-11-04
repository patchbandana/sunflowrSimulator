/* Pat Eizenga
 * 2023-19-06
 * Description: Player Save File for manipulating the player's stats and details
 * 
 * UPDATES:
 * - Added tracking for flower pots crafted and placed
 * - Added methods to manage flower pot inventory and limits
 */

import java.util.ArrayList;
import java.util.List;

public class Player1 {

    private String name;
    private int nrg;
    private int credits;
    private int day;

    // Inventory to store anything - flowers, seeds, items
    private ArrayList<Object> inventory;
    
    // Journal entries list to store player's journal entries in memory
    private List<String> journalEntries;
    
    // Garden plots list to store player's garden plots
    private List<gardenPlot> gardenPlots;
    
    // NEW: Tracking for flower pots
    private int flowerPotsCrafted; // Total flower pots ever crafted
    private static final int MAX_FLOWER_POTS = 10; // Maximum flower pots that can exist
    
    // NEW: Track if player has built their first additional plot (for hints)
    private boolean hasBuiltExtraPlot;

    public Player1(String name) {
        this.name = name;
        this.nrg = 10;
        this.credits = 100;
        this.day = 1;
        this.inventory = new ArrayList<>();
        this.journalEntries = new ArrayList<>();
        this.flowerPotsCrafted = 0;
        this.hasBuiltExtraPlot = false;
        
        // Initialize with 3 garden plots
        this.gardenPlots = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            this.gardenPlots.add(new gardenPlot());
        }
    }

    // Inventory add method
    public void addToInventory(Object item) {
        inventory.add(item);
    }
    
    // Inventory remove method
    public boolean removeFromInventory(Object item) {
        return inventory.remove(item);
    }
    
    // Inventory remove at index method
    public Object removeFromInventory(int index) {
        if (index >= 0 && index < inventory.size()) {
            return inventory.remove(index);
        }
        return null;
    }

    // Inventory getter method
    public ArrayList<Object> getInventory() {
        return inventory;
    }
    
    // Garden plots getter method
    public List<gardenPlot> getGardenPlots() {
        return gardenPlots;
    }
    
    // Get a specific garden plot
    public gardenPlot getGardenPlot(int index) {
        if (index >= 0 && index < gardenPlots.size()) {
            return gardenPlots.get(index);
        }
        return null;
    }
    
    // Add a new garden plot
    public void addGardenPlot() {
        gardenPlots.add(new gardenPlot());
    }
    
    // NEW: Add a flower pot to the garden
    public void addFlowerPotToGarden(gardenPlot flowerPot) {
        gardenPlots.add(flowerPot);
    }
    
    // NEW: Count how many flower pots are currently placed in the garden
    public int getPlacedFlowerPotCount() {
        int count = 0;
        for (gardenPlot plot : gardenPlots) {
            if (plot.isFlowerPot()) {
                count++;
            }
        }
        return count;
    }
    
    // NEW: Count how many flower pots are in the inventory
    public int getInventoryFlowerPotCount() {
        int count = 0;
        for (Object item : inventory) {
            if (item instanceof gardenPlot && ((gardenPlot)item).isFlowerPot()) {
                count++;
            }
        }
        return count;
    }
    
    // NEW: Check if player can craft more flower pots
    public boolean canCraftFlowerPot() {
        int totalFlowerPots = getPlacedFlowerPotCount() + getInventoryFlowerPotCount();
        return totalFlowerPots < MAX_FLOWER_POTS;
    }
    
    // NEW: Get total flower pots (placed + in inventory)
    public int getTotalFlowerPots() {
        return getPlacedFlowerPotCount() + getInventoryFlowerPotCount();
    }
    
    // NEW: Craft a flower pot
    public void craftFlowerPot() {
        flowerPotsCrafted++;
    }
    
    // NEW: Get total flower pots ever crafted
    public int getFlowerPotsCrafted() {
        return flowerPotsCrafted;
    }
    
    // NEW: Set flower pots crafted (for save/load)
    public void setFlowerPotsCrafted(int count) {
        this.flowerPotsCrafted = count;
    }
    
    // NEW: Check if player has built extra plot
    public boolean hasBuiltExtraPlot() {
        return hasBuiltExtraPlot;
    }
    
    // NEW: Set that player has built extra plot
    public void setHasBuiltExtraPlot(boolean hasBuilt) {
        this.hasBuiltExtraPlot = hasBuilt;
    }
    
    // Journal entries methods
    public void addJournalEntry(String entry) {
        journalEntries.add(entry);
    }
    
    public List<String> getJournalEntries() {
        return journalEntries;
    }
    
    public void setJournalEntries(List<String> entries) {
        this.journalEntries = entries;
    }

    // Pretty print the backpack
    public void printInventory() {
        if (inventory.isEmpty()) {
            System.out.println("Your backpack is empty.");
        } else {
            System.out.println("📦 Inventory:");
            for (int i = 0; i < inventory.size(); i++) {
                System.out.println("- " + inventory.get(i));
            }
        }
    }
    
    // Pretty print the garden
    public void printGarden() {
        System.out.println("\n🌱 Your Garden 🌱");
        for (int i = 0; i < gardenPlots.size(); i++) {
            System.out.println("Plot #" + (i+1) + ":");
            System.out.println(gardenPlots.get(i));
        }
    }

    // Existing getters/setters and gameplay fields
    public String getName() {
        return name;
    }

    public int getNRG() {
        return nrg;
    }

    public void setNRG(int nrg) {
        this.nrg = nrg;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public int getDay() {
        return day;
    }

    public void advanceDay() {
        this.day++;
        this.nrg = 10; // Refresh energy each day
        
        // Advance all garden plots
        for (gardenPlot plot : gardenPlots) {
            boolean grew = plot.advanceDay();
            
            // Add journal entries about growth if a plant grew
            if (grew && plot.isOccupied()) {
                Flower flower = plot.getPlantedFlower();
                String stage = flower.getGrowthStage();
                
                if (stage.equals("Seedling")) {
                    addJournalEntry("Your " + flower.getName() + " sprouted into a seedling!");
                } else if (stage.equals("Bloomed")) {
                    addJournalEntry("Your " + flower.getName() + " has bloomed! It's beautiful!");
                } else if (stage.equals("Matured")) {
                    addJournalEntry("Your " + flower.getName() + " has fully matured.");
                } else if (stage.equals("Withered")) {
                    addJournalEntry("Your " + flower.getName() + " has withered. Consider harvesting it soon.");
                } else if (stage.equals("Mutated")) {
                    addJournalEntry("Something strange happened to your " + flower.getName() + "! It mutated!");
                }
            }
            
            // Add reminders about garden care
            if (plot.isOccupied() && !plot.isWatered()) {
                Flower flower = plot.getPlantedFlower();
                addJournalEntry("Remember to water your " + flower.getName() + "!");
            }
            
            // Only add weed reminders for regular plots (not flower pots)
            if (!plot.isFlowerPot() && !plot.isWeeded()) {
                addJournalEntry("Some weeds have appeared in your garden.");
            }
        }
    }
}
/* Pat Eizenga
 * 2023-19-06
 * Description: Player Save File for manipulating the player's stats and details
 * 
 * UPDATES:
 * - Added dream tracking system for unlockable dream journal
 * - Modified advanceDay to use summary journal entries instead of per-plant entries
 */

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

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
    
    // Tracking for flower pots
    private int flowerPotsCrafted;
    private static final int MAX_FLOWER_POTS = 10;
    
    // Track if player has built their first additional plot (for hints)
    private boolean hasBuiltExtraPlot;
    
    // NEW: Track unlocked dreams (excluding hints)
    private Set<String> unlockedDreams;

    public Player1(String name) {
        this.name = name;
        this.nrg = 10;
        this.credits = 100;
        this.day = 1;
        this.inventory = new ArrayList<>();
        this.journalEntries = new ArrayList<>();
        this.flowerPotsCrafted = 0;
        this.hasBuiltExtraPlot = false;
        this.unlockedDreams = new HashSet<>();
        
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
    
    // Add a flower pot to the garden
    public void addFlowerPotToGarden(gardenPlot flowerPot) {
        gardenPlots.add(flowerPot);
    }
    
    // Count how many flower pots are currently placed in the garden
    public int getPlacedFlowerPotCount() {
        int count = 0;
        for (gardenPlot plot : gardenPlots) {
            if (plot.isFlowerPot()) {
                count++;
            }
        }
        return count;
    }
    
    // Count how many flower pots are in the inventory
    public int getInventoryFlowerPotCount() {
        int count = 0;
        for (Object item : inventory) {
            if (item instanceof gardenPlot && ((gardenPlot)item).isFlowerPot()) {
                count++;
            }
        }
        return count;
    }
    
    // Check if player can craft more flower pots
    public boolean canCraftFlowerPot() {
        int totalFlowerPots = getPlacedFlowerPotCount() + getInventoryFlowerPotCount();
        return totalFlowerPots < MAX_FLOWER_POTS;
    }
    
    // Get total flower pots (placed + in inventory)
    public int getTotalFlowerPots() {
        return getPlacedFlowerPotCount() + getInventoryFlowerPotCount();
    }
    
    // Craft a flower pot
    public void craftFlowerPot() {
        flowerPotsCrafted++;
    }
    
    // Get total flower pots ever crafted
    public int getFlowerPotsCrafted() {
        return flowerPotsCrafted;
    }
    
    // Set flower pots crafted (for save/load)
    public void setFlowerPotsCrafted(int count) {
        this.flowerPotsCrafted = count;
    }
    
    // Check if player has built extra plot
    public boolean hasBuiltExtraPlot() {
        return hasBuiltExtraPlot;
    }
    
    // Set that player has built extra plot
    public void setHasBuiltExtraPlot(boolean hasBuilt) {
        this.hasBuiltExtraPlot = hasBuilt;
    }
    
    // NEW: Dream tracking methods
    public void unlockDream(String dreamFilename) {
        unlockedDreams.add(dreamFilename);
    }
    
    public boolean hasDreamUnlocked(String dreamFilename) {
        return unlockedDreams.contains(dreamFilename);
    }
    
    public Set<String> getUnlockedDreams() {
        return new HashSet<>(unlockedDreams); // Return copy for safety
    }
    
    public void setUnlockedDreams(Set<String> dreams) {
        this.unlockedDreams = new HashSet<>(dreams);
    }
    
    public int getUnlockedDreamCount() {
        return unlockedDreams.size();
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
        
        // UPDATED: Track growth/mutations/withering for summary
        int totalGrew = 0;
        int totalMutated = 0;
        int totalWithered = 0;
        int soilUpgrades = 0;
        
        // Advance all garden plots
        for (int i = 0; i < gardenPlots.size(); i++) {
            gardenPlot plot = gardenPlots.get(i);
            String previousSoil = plot.getSoilQuality();
            boolean grew = plot.advanceDay();
            String newSoil = plot.getSoilQuality();
            
            // Check if soil quality upgraded
            if (!previousSoil.equals(newSoil)) {
                soilUpgrades++;
                String plotType = plot.isFlowerPot() ? "flower pot" : "plot #" + (i + 1);
                addJournalEntry("✨ The soil in your " + plotType + " improved from " + 
                              previousSoil + " to " + newSoil + "!");
            }
            
            // Count growth events by stage
            if (grew && plot.isOccupied()) {
                Flower flower = plot.getPlantedFlower();
                String stage = flower.getGrowthStage();
                
                if (stage.equals("Mutated")) {
                    totalMutated++;
                } else if (stage.equals("Withered")) {
                    totalWithered++;
                } else {
                    totalGrew++;
                }
            }
        }
        
        // UPDATED: Add summary entries instead of per-plant entries
        if (totalGrew > 0) {
            if (totalGrew == 1) {
                addJournalEntry("🌱 1 plant grew overnight!");
            } else {
                addJournalEntry("🌱 " + totalGrew + " plants grew overnight!");
            }
        }
        
        if (totalMutated > 0) {
            if (totalMutated == 1) {
                addJournalEntry("✨ 1 plant mutated into something special!");
            } else {
                addJournalEntry("✨ " + totalMutated + " plants mutated into something special!");
            }
        }
        
        if (totalWithered > 0) {
            if (totalWithered == 1) {
                addJournalEntry("🥀 1 plant withered overnight.");
            } else {
                addJournalEntry("🥀 " + totalWithered + " plants withered overnight.");
            }
        }
        
        // Check for any plots that need watering/weeding
        boolean needsWater = false;
        boolean needsWeeding = false;
        
        for (gardenPlot plot : gardenPlots) {
            if (plot.isOccupied() && !plot.isWatered()) {
                needsWater = true;
            }
            if (!plot.isFlowerPot() && !plot.isWeeded()) {
                needsWeeding = true;
            }
        }
        
        if (needsWater) {
            addJournalEntry("💧 Your plants need watering!");
        }
        if (needsWeeding) {
            addJournalEntry("🌿 Some weeds appeared in the garden.");
        }
    }
}
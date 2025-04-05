/* Pat Eizenga
 * 2023-19-06
 * Description: Player Save File for manipulating the player's stats and details
 * 
 */

package sunflowrSimulator;

// Player1.java
import java.util.ArrayList;
import java.util.List;

public class Player1 {

    private String name;
    private int nrg;
    private int credits;
    private int day;

    // 🔹 New: inventory to store anything - flowers, seeds, items
    private ArrayList<Object> inventory;
    
    // 🔹 New: journal entries list to store player's journal entries in memory
    private List<String> journalEntries;
    
    // 🔹 New: garden plots list to store player's garden plots
    private List<gardenPlot> gardenPlots;

    public Player1(String name) {
        this.name = name;
        this.nrg = 10;
        this.credits = 100;
        this.day = 1;
        this.inventory = new ArrayList<>(); // 🔹 Start with empty backpack
        this.journalEntries = new ArrayList<>(); // 🔹 Start with empty journal
        
        // Initialize with 3 garden plots
        this.gardenPlots = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            this.gardenPlots.add(new gardenPlot());
        }
    }

    // 🔹 Inventory add method
    public void addToInventory(Object item) {
        inventory.add(item);
    }
    
    // 🔹 Inventory remove method
    public boolean removeFromInventory(Object item) {
        return inventory.remove(item);
    }
    
    // 🔹 Inventory remove at index method
    public Object removeFromInventory(int index) {
        if (index >= 0 && index < inventory.size()) {
            return inventory.remove(index);
        }
        return null;
    }

    // 🔹 Inventory getter method
    public ArrayList<Object> getInventory() {
        return inventory;
    }
    
    // 🔹 Garden plots getter method
    public List<gardenPlot> getGardenPlots() {
        return gardenPlots;
    }
    
    // 🔹 Get a specific garden plot
    public gardenPlot getGardenPlot(int index) {
        if (index >= 0 && index < gardenPlots.size()) {
            return gardenPlots.get(index);
        }
        return null;
    }
    
    // 🔹 Add a new garden plot
    public void addGardenPlot() {
        gardenPlots.add(new gardenPlot());
    }
    
    // 🔹 Journal entries methods
    public void addJournalEntry(String entry) {
        journalEntries.add(entry);
    }
    
    public List<String> getJournalEntries() {
        return journalEntries;
    }
    
    public void setJournalEntries(List<String> entries) {
        this.journalEntries = entries;
    }

    // 🔹 Optional: Pretty print the backpack
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
    
    // 🔹 Pretty print the garden
    public void printGarden() {
        System.out.println("\n🌱 Your Garden 🌱");
        for (int i = 0; i < gardenPlots.size(); i++) {
            System.out.println("Plot #" + (i+1) + ":");
            System.out.println(gardenPlots.get(i));
        }
    }

    // ✅ Existing getters/setters and gameplay fields below:
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
            
            if (!plot.isWeeded()) {
                addJournalEntry("Some weeds have appeared in your garden.");
            }
        }
    }
}
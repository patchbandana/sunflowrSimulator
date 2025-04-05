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

    public Player1(String name) {
        this.name = name;
        this.nrg = 10;
        this.credits = 100;
        this.day = 1;
        this.inventory = new ArrayList<>(); // 🔹 Start with empty backpack
        this.journalEntries = new ArrayList<>(); // 🔹 Start with empty journal
    }

    // 🔹 Inventory add method
    public void addToInventory(Object item) {
        inventory.add(item);
    }

    // 🔹 Inventory getter method
    public ArrayList<Object> getInventory() {
        return inventory;
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
    }
}
 /* Journal class for handling save/load functionality
 * Added to sunflowrSimulator package
 */
package sunflowrSimulator;

import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

public class Journal {
    private static final String SAVE_DIRECTORY = "saves/";
    // Define constants for pagination
    public static final int ENTRIES_PER_PAGE = 5;
    private static final int MAX_PAGES = 20;
    
    /**
     * Saves the player's current state to a journal file
     * @param player The player object to save
     * @return true if save was successful, false otherwise
     */
    public static boolean saveGame(Player1 player) {
        // Make sure the saves directory exists
        File directory = new File(SAVE_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        String filename = SAVE_DIRECTORY + player.getName() + ".txt";
        File saveFile = new File(filename);
        
        // Preserve existing journal entries if the file exists
        List<String> existingJournalEntries = new ArrayList<>();
        if (saveFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                String line;
                boolean inJournalSection = false;
                
                while ((line = reader.readLine()) != null) {
                    if (line.equals("[JOURNAL_ENTRIES]")) {
                        inJournalSection = true;
                        continue;
                    } else if (line.startsWith("[") && inJournalSection) {
                        break; // End of journal section
                    }
                    
                    if (inJournalSection && line.startsWith("Entry=")) {
                        existingJournalEntries.add(line);
                    }
                }
            } catch (IOException e) {
                System.out.println("Warning: Could not read existing journal entries: " + e.getMessage());
                // Continue with save operation even if reading existing entries fails
            }
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Write player basic info
            writer.write("[PLAYER]\n");
            writer.write("Name=" + player.getName() + "\n");
            writer.write("NRG=" + player.getNRG() + "\n");
            writer.write("Credits=" + player.getCredits() + "\n");
            writer.write("Day=" + player.getDay() + "\n");
            
            // Write save timestamp
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            writer.write("SaveDate=" + now.format(formatter) + "\n");
            
            // Write inventory items
            writer.write("[INVENTORY]\n");
            ArrayList<Object> inventory = player.getInventory();
            
            for (Object item : inventory) {
                if (item instanceof Flower) {
                    Flower flower = (Flower) item;
                    writer.write("Flower=" + flower.getName() + "," +
                                 flower.getGrowthStage() + "," +
                                 flower.getDaysPlanted() + "," +
                                 flower.getDurability() + "," +
                                 flower.getCost());
                    
                    // Add NRG restored if it's a MammothSunflower
                    if (item instanceof MammothSunflower) {
                        writer.write("," + ((MammothSunflower) item).getNRGRestored());
                    }
                    
                    writer.write("\n");
                }
                // Add other item types here as game expands
            }
            
            // Write garden plots
            writer.write("[GARDEN_PLOTS]\n");
            List<gardenPlot> gardenPlots = player.getGardenPlots();
            writer.write("PlotCount=" + gardenPlots.size() + "\n");
            
            for (int i = 0; i < gardenPlots.size(); i++) {
                gardenPlot plot = gardenPlots.get(i);
                writer.write("Plot=" + i + "," + 
                            plot.isWatered() + "," + 
                            plot.isWeeded() + "," + 
                            plot.isFertilized() + "," +
                            plot.getSoilQuality() + "\n");
                
                // If the plot has a flower, save that too
                if (plot.isOccupied()) {
                    Flower flower = plot.getPlantedFlower();
                    writer.write("PlotFlower=" + i + "," +
                                flower.getName() + "," +
                                flower.getGrowthStage() + "," +
                                flower.getDaysPlanted() + "," +
                                flower.getDurability() + "," +
                                flower.getCost());
                    
                    // Add NRG restored if it's a MammothSunflower
                    if (flower instanceof MammothSunflower) {
                        writer.write("," + ((MammothSunflower) flower).getNRGRestored());
                    }
                    
                    writer.write("\n");
                }
            }
            
            // Write journal entries section
            writer.write("[JOURNAL_ENTRIES]\n");
            
            // Add a new save entry
            writer.write("Entry=" + player.getDay() + "," + now.format(formatter) + 
                        ",Game saved on day " + player.getDay() + ".\n");
            
            // Write all existing journal entries to preserve history
            for (String entry : existingJournalEntries) {
                writer.write(entry + "\n");
            }
            
            return true;
        } catch (IOException e) {
            System.out.println("Error saving game: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Loads a player's saved game data
     * @param playerName The name of the player to load
     * @return The loaded Player1 object, or null if no save exists
     */
    public static Player1 loadGame(String playerName) {
        String filename = SAVE_DIRECTORY + playerName + ".txt";
        File saveFile = new File(filename);
        
        if (!saveFile.exists()) {
            return null; // No save file found
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            Player1 player = null;
            String section = "";
            String line;
            List<String> journalEntries = new ArrayList<>();
            
            while ((line = reader.readLine()) != null) {
                // Check for section headers
                if (line.equals("[PLAYER]")) {
                    section = "PLAYER";
                    continue;
                } else if (line.equals("[INVENTORY]")) {
                    section = "INVENTORY";
                    continue;
                } else if (line.equals("[JOURNAL_ENTRIES]")) {
                    section = "JOURNAL_ENTRIES";
                    continue;
                }
                
                // Process data based on current section
                if (section.equals("PLAYER")) {
                    if (line.startsWith("Name=")) {
                        String name = line.substring(5);
                        player = new Player1(name);
                    } else if (line.startsWith("NRG=")) {
                        player.setNRG(Integer.parseInt(line.substring(4)));
                    } else if (line.startsWith("Credits=")) {
                        player.setCredits(Integer.parseInt(line.substring(8)));
                    } else if (line.startsWith("Day=")) {
                        int day = Integer.parseInt(line.substring(4));
                        // Set the day (need to account for day starting at 1)
                        while (player.getDay() < day) {
                            player.advanceDay();
                        }
                    }
                } else if (section.equals("INVENTORY") && player != null) {
                    if (line.startsWith("Flower=")) {
                        String[] flowerData = line.substring(7).split(",");
                        if (flowerData.length >= 5) {
                            String name = flowerData[0];
                            String growthStage = flowerData[1];
                            int daysPlanted = Integer.parseInt(flowerData[2]);
                            double durability = Double.parseDouble(flowerData[3]);
                            double cost = Double.parseDouble(flowerData[4]);
                            
                            // Create appropriate flower type based on data
                            if (name.contains("Mammoth Sunflower") && flowerData.length >= 6) {
                                int nrgRestored = Integer.parseInt(flowerData[5]);
                                MammothSunflower flower = new MammothSunflower(
                                    name, growthStage, daysPlanted, durability, nrgRestored, cost);
                                player.addToInventory(flower);
                            } else {
                                // Handle other flower types as they're added
                            }
                        }
                    }
                    // Add other item loading logic here as game expands
                } else if (section.equals("JOURNAL_ENTRIES") && player != null) {
                    // Process journal entries and add to list
                    if (line.startsWith("Entry=")) {
                        String[] parts = line.substring(6).split(",", 3);
                        if (parts.length >= 3) {
                            String day = parts[0];
                            String date = parts[1];
                            String entryText = parts[2];
                            String formattedEntry = "Day " + day + " (" + date + "): " + entryText;
                            journalEntries.add(formattedEntry);
                        }
                    }
                }
            }
            
            // Add all journal entries to player's memory
            if (player != null && !journalEntries.isEmpty()) {
                for (String entry : journalEntries) {
                    player.addJournalEntry(entry);
                }
            }
            
            return player;
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error loading game: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Adds a journal entry to the player's save file and memory
     * @param player The player object
     * @param entry The journal entry text
     * @return true if successful, false otherwise
     */
    public static boolean addJournalEntry(Player1 player, String entry) {
        // Format the entry
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedEntry = "Day " + player.getDay() + " (" + now.format(formatter) + "): " + entry;
        
        // Add to player's in-memory journal entries
        player.addJournalEntry(formattedEntry);
        
        // Now save to file
        String filename = SAVE_DIRECTORY + player.getName() + ".txt";
        File saveFile = new File(filename);
        
        if (!saveFile.exists()) {
            // Need to save game first
            saveGame(player);
            return true; // saveGame already added the entry
        }
        
        try {
            // Read all existing content
            List<String> fileContent = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    fileContent.add(line);
                }
            }
            
            // Find or create JOURNAL_ENTRIES section
            int journalSectionIndex = -1;
            int journalEndIndex = -1;
            boolean inJournalSection = false;
            
            for (int i = 0; i < fileContent.size(); i++) {
                if (fileContent.get(i).equals("[JOURNAL_ENTRIES]")) {
                    journalSectionIndex = i;
                    inJournalSection = true;
                    continue;
                }
                
                // If we're in journal section and encounter another section, mark end
                if (inJournalSection && fileContent.get(i).startsWith("[") && !fileContent.get(i).equals("[JOURNAL_ENTRIES]")) {
                    journalEndIndex = i;
                    break;
                }
            }
            
            if (journalSectionIndex == -1) {
                // No journal section exists yet, add it at the end
                fileContent.add("[JOURNAL_ENTRIES]");
                journalSectionIndex = fileContent.size() - 1;
                journalEndIndex = fileContent.size();
            } else if (journalEndIndex == -1) {
                // Journal section is the last section
                journalEndIndex = fileContent.size();
            }
            
            // Create file entry format 
            String fileEntry = "Entry=" + player.getDay() + "," + now.format(formatter) + "," + entry;
            
            // Insert immediately after the [JOURNAL_ENTRIES] header
            fileContent.add(journalSectionIndex + 1, fileEntry);
            
            // Write updated content back to file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
                for (String line : fileContent) {
                    writer.write(line + "\n");
                }
            }
            
            return true;
        } catch (IOException e) {
            System.out.println("Error adding journal entry: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets all journal entries for a player
     * @param playerName The name of the player
     * @return List of journal entries as strings
     */
    public static List<String> getJournalEntries(String playerName) {
        return getJournalEntries(playerName, 0); // Default to first page
    }
    
    /**
     * Gets paginated journal entries for a player
     * @param playerName The name of the player
     * @param page The page number (0-based)
     * @return List of journal entries for the requested page
     */
    public static List<String> getJournalEntries(String playerName, int page) {
        // Try to load the player if they exist
        Player1 player = loadGame(playerName);
        
        // If player doesn't exist or has no entries, return empty list
        if (player == null) {
            return new ArrayList<>();
        }
        
        List<String> allEntries = player.getJournalEntries();
        
        // Implement pagination
        int totalEntries = allEntries.size();
        int startIndex = page * ENTRIES_PER_PAGE;
        int endIndex = Math.min(startIndex + ENTRIES_PER_PAGE, totalEntries);
        
        // Handle invalid page number
        if (startIndex >= totalEntries || page < 0) {
            return new ArrayList<>(); // Return empty list for invalid page
        }
        
        return allEntries.subList(startIndex, endIndex);
    }
    
    /**
     * Gets the total number of journal pages available
     * @param playerName The name of the player
     * @return The total number of pages based on ENTRIES_PER_PAGE
     */
    public static int getTotalJournalPages(String playerName) {
        // Try to load the player if they exist
        Player1 player = loadGame(playerName);
        
        // If player doesn't exist, return 0
        if (player == null) {
            return 0;
        }
        
        List<String> allEntries = player.getJournalEntries();
        return (int) Math.ceil((double) allEntries.size() / ENTRIES_PER_PAGE);
    }
    
    /**
     * Gets all journal entries without pagination
     * @param playerName The name of the player
     * @return Complete list of all journal entries
     */
    public static List<String> getAllJournalEntries(String playerName) {
        // Try to load the player if they exist
        Player1 player = loadGame(playerName);
        
        // If player doesn't exist, return empty list
        if (player == null) {
            return new ArrayList<>();
        }
        
        List<String> allEntries = player.getJournalEntries();
        
        // Limit to MAX_PAGES * ENTRIES_PER_PAGE if needed
        int maxEntries = MAX_PAGES * ENTRIES_PER_PAGE;
        if (allEntries.size() > maxEntries) {
            return allEntries.subList(0, maxEntries);
        }
        
        return allEntries;
    }
    
    /**
     * Checks if a save file exists for a player
     * @param playerName The name to check
     * @return true if save exists, false otherwise
     */
    public static boolean saveExists(String playerName) {
        String filename = SAVE_DIRECTORY + playerName + ".txt";
        File saveFile = new File(filename);
        return saveFile.exists();
    }
    
    /**
     * Resets a player's save file but keeps the player name
     * This completely wipes out the old save and replaces it with a fresh one
     * 
     * @param player The new Player1 object (should be freshly created with just the name preserved)
     * @return true if successful, false otherwise
     */
    public static boolean resetGame(Player1 player) {
        // Make sure the saves directory exists
        File directory = new File(SAVE_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        // Delete existing save file if it exists
        String filename = SAVE_DIRECTORY + player.getName() + ".txt";
        File saveFile = new File(filename);
        if (saveFile.exists()) {
            saveFile.delete();
        }
        
        // Create a fresh save with the new player
        return saveGame(player);
    }
}
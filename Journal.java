/* Journal class for handling save/load functionality
 * Added to sunflowrSimulator package
 * 
 * COMPLETE REWRITE with debugging output to track issues
 * This version prints detailed messages to help identify where save/load fails
 */

import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

public class Journal {
    private static final String SAVE_DIRECTORY = "saves/";
    public static final int ENTRIES_PER_PAGE = 5;
    private static final int MAX_PAGES = 20;
    
    /**
     * Helper class to store plot information during load process
     * This is a static nested class so it can be used with HashMap
     */
    private static class PlotData {
        boolean watered;
        boolean weeded;
        boolean fertilized;
        String soilQuality;
        String[] flowerData;
        
        PlotData() {
            this.watered = false;
            this.weeded = true;
            this.fertilized = false;
            this.soilQuality = "Average";
            this.flowerData = null;
        }
    }
    
    /**
     * Saves the player's current state to a journal file
     */
    public static boolean saveGame(Player1 player) {
        System.out.println("[SAVE] Starting save process for " + player.getName());
        
        // Make sure the saves directory exists
        File directory = new File(SAVE_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
            System.out.println("[SAVE] Created saves directory");
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
                        break;
                    }
                    
                    if (inJournalSection && line.startsWith("Entry=")) {
                        existingJournalEntries.add(line);
                    }
                }
                System.out.println("[SAVE] Preserved " + existingJournalEntries.size() + " existing journal entries");
            } catch (IOException e) {
                System.out.println("[SAVE] Warning: Could not read existing journal entries: " + e.getMessage());
            }
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Write player basic info
            writer.write("[PLAYER]\n");
            writer.write("Name=" + player.getName() + "\n");
            writer.write("NRG=" + player.getNRG() + "\n");
            writer.write("Credits=" + player.getCredits() + "\n");
            writer.write("Day=" + player.getDay() + "\n");
            
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            writer.write("SaveDate=" + now.format(formatter) + "\n");
            
            System.out.println("[SAVE] Saved player stats: Day " + player.getDay() + ", NRG " + player.getNRG() + ", Credits " + player.getCredits());
            
            // Write inventory items
            writer.write("[INVENTORY]\n");
            ArrayList<Object> inventory = player.getInventory();
            int inventoryCount = 0;
            
            for (Object item : inventory) {
                if (item instanceof Flower) {
                    Flower flower = (Flower) item;
                    writer.write("Flower=" + flower.getName() + "," +
                                 flower.getGrowthStage() + "," +
                                 flower.getDaysPlanted() + "," +
                                 flower.getDurability() + "," +
                                 flower.getCost());
                    
                    if (item instanceof MammothSunflower) {
                        writer.write("," + ((MammothSunflower) item).getNRGRestored());
                    }
                    
                    writer.write("\n");
                    inventoryCount++;
                    System.out.println("[SAVE] Saved inventory item: " + flower.getName() + " (" + flower.getGrowthStage() + ")");
                }
            }
            System.out.println("[SAVE] Saved " + inventoryCount + " inventory items");
            
            // Write garden plots
            writer.write("[GARDEN_PLOTS]\n");
            List<gardenPlot> gardenPlots = player.getGardenPlots();
            writer.write("PlotCount=" + gardenPlots.size() + "\n");
            System.out.println("[SAVE] Saving " + gardenPlots.size() + " garden plots");
            
            for (int i = 0; i < gardenPlots.size(); i++) {
                gardenPlot plot = gardenPlots.get(i);
                writer.write("Plot=" + i + "," + 
                            plot.isWatered() + "," + 
                            plot.isWeeded() + "," + 
                            plot.isFertilized() + "," +
                            plot.getSoilQuality() + "\n");
                
                System.out.println("[SAVE] Plot " + i + ": watered=" + plot.isWatered() + 
                                 ", weeded=" + plot.isWeeded() + ", fertilized=" + plot.isFertilized());
                
                if (plot.isOccupied()) {
                    Flower flower = plot.getPlantedFlower();
                    writer.write("PlotFlower=" + i + "," +
                                flower.getName() + "," +
                                flower.getGrowthStage() + "," +
                                flower.getDaysPlanted() + "," +
                                flower.getDurability() + "," +
                                flower.getCost());
                    
                    if (flower instanceof MammothSunflower) {
                        writer.write("," + ((MammothSunflower) flower).getNRGRestored());
                    }
                    
                    writer.write("\n");
                    System.out.println("[SAVE] Plot " + i + " has flower: " + flower.getName() + 
                                     " (Stage: " + flower.getGrowthStage() + ", Days: " + flower.getDaysPlanted() + ")");
                }
            }
            
            // Write journal entries section
            writer.write("[JOURNAL_ENTRIES]\n");
            writer.write("Entry=" + player.getDay() + "," + now.format(formatter) + 
                        ",Game saved on day " + player.getDay() + ".\n");
            
            for (String entry : existingJournalEntries) {
                writer.write(entry + "\n");
            }
            
            System.out.println("[SAVE] ✅ Save completed successfully!");
            return true;
        } catch (IOException e) {
            System.out.println("[SAVE] ❌ Error saving game: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Loads a player's saved game data with extensive debugging
     */
    public static Player1 loadGame(String playerName) {
        System.out.println("[LOAD] Starting load process for " + playerName);
        
        String filename = SAVE_DIRECTORY + playerName + ".txt";
        File saveFile = new File(filename);
        
        if (!saveFile.exists()) {
            System.out.println("[LOAD] No save file found at: " + filename);
            return null;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            Player1 player = null;
            String section = "";
            String line;
            List<String> journalEntries = new ArrayList<>();
            
            // Store garden plot data
            int expectedPlotCount = 0;
            Map<Integer, PlotData> plotDataMap = new HashMap<>();
            
            System.out.println("[LOAD] Reading save file...");
            
            while ((line = reader.readLine()) != null) {
                // Check for section headers
                if (line.equals("[PLAYER]")) {
                    section = "PLAYER";
                    System.out.println("[LOAD] Entering PLAYER section");
                    continue;
                } else if (line.equals("[INVENTORY]")) {
                    section = "INVENTORY";
                    System.out.println("[LOAD] Entering INVENTORY section");
                    continue;
                } else if (line.equals("[GARDEN_PLOTS]")) {
                    section = "GARDEN_PLOTS";
                    System.out.println("[LOAD] Entering GARDEN_PLOTS section");
                    continue;
                } else if (line.equals("[JOURNAL_ENTRIES]")) {
                    section = "JOURNAL_ENTRIES";
                    System.out.println("[LOAD] Entering JOURNAL_ENTRIES section");
                    continue;
                }
                
                // Process data based on current section
                if (section.equals("PLAYER")) {
                    if (line.startsWith("Name=")) {
                        String name = line.substring(5);
                        player = new Player1(name);
                        System.out.println("[LOAD] Created player: " + name);
                    } else if (line.startsWith("NRG=")) {
                        player.setNRG(Integer.parseInt(line.substring(4)));
                        System.out.println("[LOAD] Set NRG to: " + player.getNRG());
                    } else if (line.startsWith("Credits=")) {
                        player.setCredits(Integer.parseInt(line.substring(8)));
                        System.out.println("[LOAD] Set Credits to: " + player.getCredits());
                    } else if (line.startsWith("Day=")) {
                        int targetDay = Integer.parseInt(line.substring(4));
                        while (player.getDay() < targetDay) {
                            player.advanceDay();
                        }
                        System.out.println("[LOAD] Set Day to: " + player.getDay());
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
                            int nrgRestored = (flowerData.length >= 6) ? Integer.parseInt(flowerData[5]) : 1;
                            
                            MammothSunflower flower = new MammothSunflower(
                                name, growthStage, daysPlanted, durability, nrgRestored, cost);
                            player.addToInventory(flower);
                            System.out.println("[LOAD] Added to inventory: " + name + " (" + growthStage + ")");
                        }
                    }
                } else if (section.equals("GARDEN_PLOTS") && player != null) {
                    if (line.startsWith("PlotCount=")) {
                        expectedPlotCount = Integer.parseInt(line.substring(10));
                        System.out.println("[LOAD] Expecting " + expectedPlotCount + " plots");
                    } else if (line.startsWith("Plot=")) {
                        String[] plotData = line.substring(5).split(",");
                        if (plotData.length >= 5) {
                            int plotIndex = Integer.parseInt(plotData[0]);
                            Journal.PlotData pd = new Journal.PlotData();
                            pd.watered = Boolean.parseBoolean(plotData[1]);
                            pd.weeded = Boolean.parseBoolean(plotData[2]);
                            pd.fertilized = Boolean.parseBoolean(plotData[3]);
                            pd.soilQuality = plotData[4];
                            
                            plotDataMap.put(plotIndex, pd);
                            System.out.println("[LOAD] Read plot " + plotIndex + " data: watered=" + pd.watered + 
                                             ", weeded=" + pd.weeded + ", fertilized=" + pd.fertilized);
                        }
                    } else if (line.startsWith("PlotFlower=")) {
                        String[] flowerData = line.substring(11).split(",");
                        if (flowerData.length >= 6) {
                            int plotIndex = Integer.parseInt(flowerData[0]);
                            Journal.PlotData pd = plotDataMap.get(plotIndex);
                            if (pd != null) {
                                pd.flowerData = flowerData;
                                System.out.println("[LOAD] Plot " + plotIndex + " has flower: " + flowerData[1] + 
                                               " (Stage: " + flowerData[2] + ", Days: " + flowerData[3] + ")");
                            }
                        }
                    }
                } else if (section.equals("JOURNAL_ENTRIES") && player != null) {
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
            
            // Now restore garden plots
            if (player != null && !plotDataMap.isEmpty()) {
                System.out.println("[LOAD] Restoring " + plotDataMap.size() + " garden plots...");
                
                List<gardenPlot> playerPlots = player.getGardenPlots();
                playerPlots.clear();
                System.out.println("[LOAD] Cleared default plots");
                
                // Recreate plots in order
                for (int i = 0; i < expectedPlotCount; i++) {
                    Journal.PlotData pd = plotDataMap.get(i);
                    if (pd != null) {
                        gardenPlot plot = new gardenPlot();
                        plot.setSoilQuality(pd.soilQuality);
                        plot.setWatered(pd.watered);
                        plot.setWeeded(pd.weeded);
                        plot.setFertilized(pd.fertilized);
                        
                        playerPlots.add(plot);
                        System.out.println("[LOAD] Created plot " + i + " with states: watered=" + pd.watered + 
                                         ", weeded=" + pd.weeded + ", fertilized=" + pd.fertilized);
                        
                        // Restore flower if present
                        if (pd.flowerData != null) {
                            String name = pd.flowerData[1];
                            String growthStage = pd.flowerData[2];
                            int daysPlanted = Integer.parseInt(pd.flowerData[3]);
                            double durability = Double.parseDouble(pd.flowerData[4]);
                            double cost = Double.parseDouble(pd.flowerData[5]);
                            int nrgRestored = (pd.flowerData.length >= 7) ? Integer.parseInt(pd.flowerData[6]) : 1;
                            
                            MammothSunflower flower = new MammothSunflower(
                                name, growthStage, daysPlanted, durability, nrgRestored, cost);
                            
                            // Use forcePlantFlower to bypass the "Seed only" restriction
                            plot.forcePlantFlower(flower);
                            System.out.println("[LOAD] ✅ Restored flower in plot " + i + ": " + name + 
                                             " (Stage: " + growthStage + ", Days: " + daysPlanted + ")");
                        }
                    } else {
                        // Create empty plot if data missing
                        playerPlots.add(new gardenPlot());
                        System.out.println("[LOAD] Created empty plot " + i + " (no saved data)");
                    }
                }
            }
            
            // Add journal entries
            if (player != null && !journalEntries.isEmpty()) {
                for (String entry : journalEntries) {
                    player.addJournalEntry(entry);
                }
                System.out.println("[LOAD] Restored " + journalEntries.size() + " journal entries");
            }
            
            if (player != null) {
                System.out.println("[LOAD] ✅ Load completed successfully!");
                System.out.println("[LOAD] Final state - Day: " + player.getDay() + ", NRG: " + player.getNRG() + 
                                 ", Credits: " + player.getCredits() + ", Inventory: " + player.getInventory().size() + 
                                 " items, Plots: " + player.getGardenPlots().size());
            }
            
            return player;
        } catch (IOException | NumberFormatException e) {
            System.out.println("[LOAD] ❌ Error loading game: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Adds a journal entry to the player's save file and memory
     */
    public static boolean addJournalEntry(Player1 player, String entry) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedEntry = "Day " + player.getDay() + " (" + now.format(formatter) + "): " + entry;
        
        player.addJournalEntry(formattedEntry);
        
        String filename = SAVE_DIRECTORY + player.getName() + ".txt";
        File saveFile = new File(filename);
        
        if (!saveFile.exists()) {
            saveGame(player);
            return true;
        }
        
        try {
            List<String> fileContent = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    fileContent.add(line);
                }
            }
            
            int journalSectionIndex = -1;
            boolean inJournalSection = false;
            
            for (int i = 0; i < fileContent.size(); i++) {
                if (fileContent.get(i).equals("[JOURNAL_ENTRIES]")) {
                    journalSectionIndex = i;
                    inJournalSection = true;
                    continue;
                }
                
                if (inJournalSection && fileContent.get(i).startsWith("[") && !fileContent.get(i).equals("[JOURNAL_ENTRIES]")) {
                    break;
                }
            }
            
            if (journalSectionIndex == -1) {
                fileContent.add("[JOURNAL_ENTRIES]");
                journalSectionIndex = fileContent.size() - 1;
            }
            
            String fileEntry = "Entry=" + player.getDay() + "," + now.format(formatter) + "," + entry;
            fileContent.add(journalSectionIndex + 1, fileEntry);
            
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
    
    // All other methods remain the same
    public static List<String> getJournalEntries(String playerName) {
        return getJournalEntries(playerName, 0);
    }
    
    public static List<String> getJournalEntries(String playerName, int page) {
        Player1 player = loadGame(playerName);
        
        if (player == null) {
            return new ArrayList<>();
        }
        
        List<String> allEntries = player.getJournalEntries();
        int totalEntries = allEntries.size();
        int startIndex = page * ENTRIES_PER_PAGE;
        int endIndex = Math.min(startIndex + ENTRIES_PER_PAGE, totalEntries);
        
        if (startIndex >= totalEntries || page < 0) {
            return new ArrayList<>();
        }
        
        return allEntries.subList(startIndex, endIndex);
    }
    
    public static int getTotalJournalPages(String playerName) {
        Player1 player = loadGame(playerName);
        
        if (player == null) {
            return 0;
        }
        
        List<String> allEntries = player.getJournalEntries();
        return (int) Math.ceil((double) allEntries.size() / ENTRIES_PER_PAGE);
    }
    
    public static List<String> getAllJournalEntries(String playerName) {
        Player1 player = loadGame(playerName);
        
        if (player == null) {
            return new ArrayList<>();
        }
        
        List<String> allEntries = player.getJournalEntries();
        int maxEntries = MAX_PAGES * ENTRIES_PER_PAGE;
        if (allEntries.size() > maxEntries) {
            return allEntries.subList(0, maxEntries);
        }
        
        return allEntries;
    }
    
    public static boolean saveExists(String playerName) {
        String filename = SAVE_DIRECTORY + playerName + ".txt";
        File saveFile = new File(filename);
        return saveFile.exists();
    }
    
    public static boolean resetGame(Player1 player) {
        File directory = new File(SAVE_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        String filename = SAVE_DIRECTORY + player.getName() + ".txt";
        File saveFile = new File(filename);
        if (saveFile.exists()) {
            saveFile.delete();
        }
        
        return saveGame(player);
    }
}
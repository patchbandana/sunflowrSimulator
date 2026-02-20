/* FlowerRegistry.java
 * Loads and manages flower data from CSV file
 * Provides factory methods to create flower instances
 * * FIXES APPLIED:
 * - createSeed() now properly creates different flower types based on species
 * - Added detailed comments explaining the factory pattern
 * - ADDED: getRandomShopSelection for dynamic shop menu with difficulty weighting
 */

import java.io.*;
import java.util.*;

public class FlowerRegistry {
    private static final String FLOWER_DATA_FILE = "flowers.csv";
    private static Map<String, FlowerData> flowerDatabase = new HashMap<>();
    private static boolean isLoaded = false;
    
    /**
     * Internal class to store flower statistics from CSV
     */
    private static class FlowerData {
        String name;
        String species;
        int difficulty;
        double baseDurability;
        double seedCost;
        double seedlingValue;
        double bloomedValue;
        double maturedValue;
        double witheredValue;
        double mutatedValue;
        int nrgRestored;
        int daysToSeedling;
        int daysToBloomed;
        int daysToMatured;
        int daysToWithered;
        double mutationChance;
        
        FlowerData(String[] csvData) {
            this.name = csvData[0];
            this.species = csvData[1];
            this.difficulty = Integer.parseInt(csvData[2]);
            this.baseDurability = Double.parseDouble(csvData[3]);
            this.seedCost = Double.parseDouble(csvData[4]);
            this.seedlingValue = Double.parseDouble(csvData[5]);
            this.bloomedValue = Double.parseDouble(csvData[6]);
            this.maturedValue = Double.parseDouble(csvData[7]);
            this.witheredValue = Double.parseDouble(csvData[8]);
            this.mutatedValue = Double.parseDouble(csvData[9]);
            this.nrgRestored = Integer.parseInt(csvData[10]);
            this.daysToSeedling = Integer.parseInt(csvData[11]);
            this.daysToBloomed = Integer.parseInt(csvData[12]);
            this.daysToMatured = Integer.parseInt(csvData[13]);
            this.daysToWithered = Integer.parseInt(csvData[14]);
            this.mutationChance = Double.parseDouble(csvData[15]);
        }
    }
    
    /**
     * Loads flower data from CSV file into memory
     * Should be called once at game startup
     */
    public static void loadFlowerData() {
        if (isLoaded) {
            return; // Already loaded
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(FLOWER_DATA_FILE, java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            
            while ((line = reader.readLine()) != null) {
                // Skip header row
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                
                String[] data = line.split(",");
                if (data.length >= 16) {
                    FlowerData flowerData = new FlowerData(data);
                    flowerDatabase.put(flowerData.name, flowerData);
                }
            }
            
            isLoaded = true;
            System.out.println("✅ Loaded " + flowerDatabase.size() + " flower types from database.");
            
        } catch (IOException e) {
            System.err.println("❌ Error loading flower data: " + e.getMessage());
            System.err.println("Make sure " + FLOWER_DATA_FILE + " is in the same directory as the game.");
        }
    }
    
    /**
     * Creates a new flower seed instance from the database
     * * FACTORY PATTERN: This method acts as a factory, creating the appropriate
     * flower subclass based on the species data from the CSV. Currently all flowers
     * use MammothSunflower as the base class, but this can be extended.
     * * BUG FIX: The flower's name is now properly set to match the CSV name.
     * Previously, all flowers were created as "Mammoth Sunflower" regardless of
     * which flower type was purchased from the shop.
     * * @param flowerName The name of the flower (must match CSV exactly)
     * @return A new Flower object in "Seed" stage, or null if not found
     */
    public static Flower createSeed(String flowerName) {
        if (!isLoaded) {
            loadFlowerData();
        }
        
        FlowerData data = flowerDatabase.get(flowerName);
        if (data == null) {
            System.err.println("Warning: Flower '" + flowerName + "' not found in database.");
            return null;
        }
        
        // Create the flower with the CORRECT name from the database
        // This fixes the bug where all purchased seeds showed as "Mammoth Sunflower"
        return new FlowerInstance(
            data.name,              // Use the actual flower name from CSV
            "Seed",                 // Always start as a seed
            0,                      // Not planted yet (0 days)
            data.baseDurability,    // Base durability from CSV
            data.nrgRestored,       // Energy restored when eaten (as seed)
            data.seedCost           // Cost of the seed
        );
    }
    
    /**
     * Gets the cost of a flower seed
     * @param flowerName The name of the flower
     * @return The seed cost, or -1 if not found
     */
    public static double getSeedCost(String flowerName) {
        if (!isLoaded) {
            loadFlowerData();
        }
        
        FlowerData data = flowerDatabase.get(flowerName);
        return (data != null) ? data.seedCost : -1;
    }
    
    /**
     * Gets the value of a flower based on its growth stage
     * @param flowerName The name of the flower
     * @param growthStage The current growth stage
     * @return The value for that stage, or 0 if not found
     */
    public static double getFlowerValue(String flowerName, String growthStage) {
        if (!isLoaded) {
            loadFlowerData();
        }
        
        FlowerData data = flowerDatabase.get(flowerName);
        if (data == null) {
            return 0;
        }
        
        switch (growthStage) {
            case "Seed":
                return data.seedCost;
            case "Seedling":
                return data.seedlingValue;
            case "Bloomed":
                return data.bloomedValue;
            case "Matured":
                return data.maturedValue;
            case "Withered":
                return data.witheredValue;
            case "Mutated":
                return data.mutatedValue;
            default:
                return 0;
        }
    }
    
    /**
     * Gets all available flower names
     * @return List of all flower names in the database
     */
    public static List<String> getAllFlowerNames() {
        if (!isLoaded) {
            loadFlowerData();
        }
        
        return new ArrayList<>(flowerDatabase.keySet());
    }
    
    /**
     * Gets flowers by difficulty tier
     * @param minDifficulty Minimum difficulty (inclusive)
     * @param maxDifficulty Maximum difficulty (inclusive)
     * @return List of flower names in that difficulty range
     */
    public static List<String> getFlowersByDifficulty(int minDifficulty, int maxDifficulty) {
        if (!isLoaded) {
            loadFlowerData();
        }
        
        List<String> result = new ArrayList<>();
        for (FlowerData data : flowerDatabase.values()) {
            if (data.difficulty >= minDifficulty && data.difficulty <= maxDifficulty) {
                result.add(data.name);
            }
        }
        return result;
    }
    
    /**
     * Gets a random flower from a difficulty tier
     * @param minDifficulty Minimum difficulty (inclusive)
     * @param maxDifficulty Maximum difficulty (inclusive)
     * @return Random flower name from that tier, or null if none found
     */
    public static String getRandomFlowerByDifficulty(int minDifficulty, int maxDifficulty) {
        List<String> flowers = getFlowersByDifficulty(minDifficulty, maxDifficulty);
        if (flowers.isEmpty()) {
            return null;
        }
        
        Random rand = new Random();
        return flowers.get(rand.nextInt(flowers.size()));
    }
    
    /**
     * Gets flower difficulty
     * @param flowerName The name of the flower
     * @return The difficulty rating (1-5), or -1 if not found
     */
    public static int getFlowerDifficulty(String flowerName) {
        if (!isLoaded) {
            loadFlowerData();
        }
        
        FlowerData data = flowerDatabase.get(flowerName);
        return (data != null) ? data.difficulty : -1;
    }
    
    /**
     * Checks if a flower exists in the database
     * @param flowerName The name to check
     * @return true if flower exists
     */
    public static boolean flowerExists(String flowerName) {
        if (!isLoaded) {
            loadFlowerData();
        }
        
        return flowerDatabase.containsKey(flowerName);
    }
    
    /**
     * Gets detailed info about a flower for display purposes
     * @param flowerName The name of the flower
     * @return A formatted string with flower information
     */
    public static String getFlowerInfo(String flowerName) {
        if (!isLoaded) {
            loadFlowerData();
        }
        
        FlowerData data = flowerDatabase.get(flowerName);
        if (data == null) {
            return "Flower not found.";
        }
        
        StringBuilder info = new StringBuilder();
        info.append("🌸 ").append(data.name).append(" (").append(data.species).append(")\n");
        info.append("Difficulty: ");
        for (int i = 0; i < data.difficulty; i++) {
            info.append("★");
        }
        for (int i = data.difficulty; i < 5; i++) {
            info.append("☆");
        }
        info.append("\n");
        info.append("Seed Cost: ").append(data.seedCost).append(" credits\n");
        info.append("Matured Value: ").append(data.maturedValue).append(" credits\n");
        info.append("Growth Time: ").append(data.daysToMatured).append(" days\n");
        
        return info.toString();
    }
    
    /**
     * Generates a random, weighted selection of flowers for the shop.
     * Flowers with lower difficulty have a higher chance of being selected.
     * * @param count The number of unique flowers to select (e.g., 4 or 5).
     * @param maxDifficulty The maximum difficulty to include in the selection pool (e.g., 5 to include all).
     * @return A list of unique flower names for the shop.
     */
    public static List<String> getRandomShopSelection(int count, int maxDifficulty) {
        if (!isLoaded) {
            loadFlowerData();
        }
        
        // 1. Create a weighted pool of all flowers up to maxDifficulty
        List<String> weightedPool = new ArrayList<>();
        Random rand = new Random();

        // Check if the database is empty before proceeding
        if (flowerDatabase.isEmpty()) {
            return new ArrayList<>();
        }

        // Weighting: weight = maxDifficulty + 1 - currentDifficulty
        // e.g., if maxDiff=5: Diff 1 gets weight 5, Diff 5 gets weight 1.
        for (FlowerData data : flowerDatabase.values()) {
            if (data.difficulty > 0 && data.difficulty <= maxDifficulty) {
                int weight = maxDifficulty + 1 - data.difficulty;
                for (int i = 0; i < weight; i++) {
                    weightedPool.add(data.name);
                }
            }
        }
        
        // Safety check: If the weighting produced an empty pool (should not happen if difficulty 1 flowers exist)
        if (weightedPool.isEmpty()) {
            // Fallback: Use all available flowers without weighting
            for (FlowerData data : flowerDatabase.values()) {
                if (data.difficulty > 0) {
                    weightedPool.add(data.name);
                }
            }
        }
        
        // 2. Select 'count' unique flowers from the weighted pool
        List<String> shopSelection = new ArrayList<>();
        
        // Safety check to ensure we don't try to select more items than available unique flowers
        List<String> uniqueFlowers = new ArrayList<>(flowerDatabase.keySet());
        count = Math.min(count, uniqueFlowers.size());

        while (shopSelection.size() < count && !weightedPool.isEmpty()) {
            // Pick a random index from the weighted pool
            int randomIndex = rand.nextInt(weightedPool.size());
            String chosenFlower = weightedPool.get(randomIndex);
            
            // Only add if it's not already in the final selection
            if (!shopSelection.contains(chosenFlower)) {
                shopSelection.add(chosenFlower);
            }
            
            // Remove the specific instance chosen to reduce its weight in subsequent draws
            // This is a more subtle way of managing the weighted pool for uniqueness
            weightedPool.remove(randomIndex);
        }
        
        // Fallback: If the pool ran out of items, fill the remaining slots with non-weighted random picks.
        while (shopSelection.size() < count) {
             String fallbackFlower = uniqueFlowers.get(rand.nextInt(uniqueFlowers.size()));
             if (!shopSelection.contains(fallbackFlower)) {
                 shopSelection.add(fallbackFlower);
             }
        }
        
        return shopSelection;
    }
}
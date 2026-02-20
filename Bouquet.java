/* Bouquet.java
 * Represents a bouquet of flowers that can be auctioned
 * 
 * FEATURES:
 * - Holds 3-12 flowers (Bloomed, Matured, Mutated, or Withered only)
 * - Can be given custom name
 * - Tracks composition for multiplier calculations
 * - Stores creation date
 */

import java.util.ArrayList;
import java.util.List;

public class Bouquet {
    private List<Flower> flowers;
    private String customName;
    private int dayCreated;
    private double baseValue;
    
    /**
     * Creates a new bouquet
     * @param flowers List of flowers (must be 3-12, Bloomed+ stages only)
     * @param customName Optional custom name (null if unnamed)
     * @param dayCreated Day the bouquet was created
     */
    public Bouquet(List<Flower> flowers, String customName, int dayCreated) {
        this.flowers = new ArrayList<>(flowers);
        this.customName = customName;
        this.dayCreated = dayCreated;
        this.baseValue = calculateBaseValue();
    }
    
    /**
     * Calculates the base value (sum of all constituent flowers)
     */
    private double calculateBaseValue() {
        double total = 0;
        for (Flower flower : flowers) {
            String stage = flower.getGrowthStage();
            String name = flower.getName();
            
            // Get value from registry
            double value = FlowerRegistry.getFlowerValue(name, stage);
            total += value;
        }
        return total;
    }
    
    /**
     * Gets the composition signature for recognizing repeated bouquets
     * Format: "Rose-Bloomed,Rose-Bloomed,Tulip-Matured" (sorted alphabetically)
     */
    public String getCompositionSignature() {
        List<String> components = new ArrayList<>();
        for (Flower flower : flowers) {
            components.add(flower.getName() + "-" + flower.getGrowthStage());
        }
        components.sort(String::compareTo);
        return String.join(",", components);
    }
    
    /**
     * Checks if this bouquet matches a known composition
     */
    public boolean matchesComposition(String signature) {
        return getCompositionSignature().equals(signature);
    }
    
    /**
     * Validates that a list of flowers can form a bouquet
     */
    public static boolean isValidBouquet(List<Flower> flowers) {
        if (flowers == null || flowers.size() < 3 || flowers.size() > 12) {
            return false;
        }
        
        // Check that all flowers are Bloomed or higher
        for (Flower flower : flowers) {
            String stage = flower.getGrowthStage();
            if (stage.equals("Seed") || stage.equals("Seedling")) {
                return false;
            }
        }
        
        return true;
    }
    
    // Getters
    public List<Flower> getFlowers() {
        return new ArrayList<>(flowers); // Return copy for safety
    }
    
    public String getCustomName() {
        return customName;
    }
    
    public boolean hasCustomName() {
        return customName != null && !customName.isEmpty();
    }
    
    public int getDayCreated() {
        return dayCreated;
    }
    
    public double getBaseValue() {
        return baseValue;
    }
    
    public int getFlowerCount() {
        return flowers.size();
    }
    
    /**
     * Gets a display string for the bouquet
     */
    public String getDisplayName() {
        if (hasCustomName()) {
            return customName + " (" + flowers.size() + " flowers)";
        } else {
            return "Unnamed Bouquet (" + flowers.size() + " flowers)";
        }
    }
    
    /**
     * Gets a detailed description of the bouquet contents
     */
    public String getDetailedDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("💐 ").append(getDisplayName()).append("\n");
        sb.append("Base Value: ").append((int)baseValue).append(" credits\n");
        sb.append("Created: Day ").append(dayCreated).append("\n");
        sb.append("\nContents:\n");
        
        // Count each flower type and stage
        java.util.Map<String, Integer> flowerCounts = new java.util.HashMap<>();
        for (Flower flower : flowers) {
            String key = flower.getName() + " (" + flower.getGrowthStage() + ")";
            flowerCounts.put(key, flowerCounts.getOrDefault(key, 0) + 1);
        }
        
        // Display sorted
        flowerCounts.entrySet().stream()
            .sorted(java.util.Map.Entry.comparingByKey())
            .forEach(entry -> sb.append("  • ").append(entry.getValue())
                .append("x ").append(entry.getKey()).append("\n"));
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
}
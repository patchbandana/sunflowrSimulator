/* 
 * Description: Garden Plot class for the sunflowrSimulator
 * Represents a single plot of land where flowers can be planted
 * 
 * UPDATES:
 * - Added flower pot functionality with isFlowerPot flag
 * - Flower pots have special rules: no weeding, double durability penalty, restrictions on plant types
 * - Added methods to check if a flower can be planted in a flower pot
 */

public class gardenPlot {
    // Current plant in this garden plot
    private Flower plantedFlower;
    
    // Garden plot state
    private boolean isWatered;
    private boolean isWeeded;
    private boolean isFertilized;
    private String soilQuality; // "Poor", "Average", "Good", "Excellent"
    
    // NEW: Flower pot functionality
    private boolean isFlowerPot; // Is this plot actually a flower pot?
    
    /**
     * Creates a new garden plot with default values
     */
    public gardenPlot() {
        this.plantedFlower = null;
        this.isWatered = false;
        this.isWeeded = true; // Start with a weeded plot
        this.isFertilized = false;
        this.soilQuality = "Average";
        this.isFlowerPot = false; // Default to regular plot
    }
    
    /**
     * Creates a new flower pot (special type of garden plot)
     * @param isFlowerPot true to create a flower pot
     */
    public gardenPlot(boolean isFlowerPot) {
        this();
        this.isFlowerPot = isFlowerPot;
        if (isFlowerPot) {
            this.isWeeded = true; // Flower pots don't get weeds
            this.soilQuality = "Good"; // Flower pots have controlled soil
        }
    }
    
    /**
     * Checks if this is a flower pot
     * @return true if this is a flower pot
     */
    public boolean isFlowerPot() {
        return isFlowerPot;
    }
    
    /**
     * Sets whether this is a flower pot
     * @param isFlowerPot true to make this a flower pot
     */
    public void setFlowerPot(boolean isFlowerPot) {
        this.isFlowerPot = isFlowerPot;
        if (isFlowerPot) {
            this.isWeeded = true; // Flower pots don't get weeds
        }
    }
    
    /**
     * Checks if a flower can be planted in this flower pot
     * Flower pots cannot contain:
     * - Bush or Tree subspecies
     * - Flowers with difficulty >= 4
     * 
     * @param flower The flower to check
     * @return true if the flower can be planted in this flower pot
     */
    public boolean canPlantInFlowerPot(Flower flower) {
        if (!isFlowerPot) {
            return true; // Regular plots can plant anything
        }
        
        // Check difficulty rating
        int difficulty = FlowerRegistry.getFlowerDifficulty(flower.getName());
        if (difficulty >= 4) {
            return false; // Too difficult for flower pot
        }
        
        // Check if it's a bush or tree by getting the species from FlowerRegistry
        // We need to check the CSV data for the species field
        String flowerName = flower.getName();
        
        // Get all flower names and check if this flower exists
        if (!FlowerRegistry.flowerExists(flowerName)) {
            return true; // If we can't find it, allow it (shouldn't happen)
        }
        
        // Check the species field - we'll need to expose this in FlowerRegistry
        // For now, check if the name contains "Bush" or "Tree"
        String nameLower = flowerName.toLowerCase();
        if (nameLower.contains("bush") || nameLower.contains("tree")) {
            return false; // Bush or tree species not allowed
        }
        
        return true; // All checks passed
    }
    
    /**
     * Plants a flower in this garden plot
     * @param flower The flower to plant
     * @return true if planting was successful, false if plot is already occupied
     */
    public boolean plantFlower(Flower flower) {
        // Check if plot is already occupied
        if (isOccupied()) {
            return false;
        }
        
        // Can only plant seeds
        if (!flower.getGrowthStage().equals("Seed")) {
            return false;
        }
        
        // Check flower pot restrictions
        if (isFlowerPot && !canPlantInFlowerPot(flower)) {
            return false;
        }
        
        // Plant the flower - set it to planted and set days planted to 1
        flower.setDaysPlanted(1);
        this.plantedFlower = flower;
        return true;
    }
    
    /**
     * Force plants a flower regardless of growth stage
     * This is used by the save/load system to restore planted flowers
     * @param flower The flower to force plant
     */
    public void forcePlantFlower(Flower flower) {
        this.plantedFlower = flower;
    }
    
    /**
     * Harvests the flower from this garden plot
     * @return The harvested flower, or null if there's nothing to harvest
     */
    public Flower harvestFlower() {
        if (!isOccupied()) {
            return null;
        }
        
        Flower harvestedFlower = this.plantedFlower;
        this.plantedFlower = null;
        
        // Reset plot state after harvesting
        this.isWatered = false;
        
        return harvestedFlower;
    }
    
    /**
     * Waters the garden plot
     * @return true if watering was successful
     */
    public boolean waterPlot() {
        if (!isOccupied()) {
            return false;
        }
        
        if (isWatered) {
            return false; // Already watered
        }
        
        this.isWatered = true;
        return true;
    }
    
    /**
     * Weeds the garden plot
     * Flower pots cannot be weeded (they don't get weeds)
     * @return true if weeding was successful
     */
    public boolean weedPlot() {
        if (isFlowerPot) {
            return false; // Can't weed a flower pot
        }
        
        if (isWeeded) {
            return false; // Already weeded
        }
        
        this.isWeeded = true;
        return true;
    }
    
    /**
     * Fertilizes the garden plot
     * @return true if fertilizing was successful
     */
    public boolean fertilizePlot() {
        if (isFertilized) {
            return false; // Already fertilized
        }
        
        this.isFertilized = true;
        return true;
    }
    
    /**
     * Directly sets the watered state (for save/load system)
     * @param watered The watered state to set
     */
    public void setWatered(boolean watered) {
        this.isWatered = watered;
    }
    
    /**
     * Directly sets the weeded state (for save/load system)
     * @param weeded The weeded state to set
     */
    public void setWeeded(boolean weeded) {
        this.isWeeded = weeded;
    }
    
    /**
     * Directly sets the fertilized state (for save/load system)
     * @param fertilized The fertilized state to set
     */
    public void setFertilized(boolean fertilized) {
        this.isFertilized = fertilized;
    }
    
    /**
     * Advances the day for this garden plot, grows the plant if conditions are met
     * Growth is now influenced by care (watering/weeding) and flower difficulty
     * 
     * FLOWER POT CHANGES:
     * - Flower pots don't get weeds, so weeding is always true
     * - Double durability penalty if not watered
     * 
     * @return true if the plant grew, false otherwise
     */
    public boolean advanceDay() {
        if (!isOccupied()) {
            // Reset daily states
            this.isWatered = false;
            if (!isFlowerPot) {
                this.isWeeded = Math.random() > 0.7; // 30% chance of weeds appearing (not for flower pots)
            }
            return false;
        }
        
        // Increment days planted
        plantedFlower.setDaysPlanted(plantedFlower.getDaysPlanted() + 1);
        
        // Get flower difficulty to adjust growth chances
        int difficulty = FlowerRegistry.getFlowerDifficulty(plantedFlower.getName());
        if (difficulty == -1) {
            difficulty = 3; // Default to medium if not found
        }
        
        // Calculate base growth chance (harder flowers = lower base chance)
        // Difficulty 1: 85% base, Difficulty 5: 45% base
        double baseGrowthChance = 0.95 - (difficulty * 0.10);
        
        // Modify growth chance based on care
        double growthChance = baseGrowthChance;
        
        if (isWatered) {
            growthChance += 0.30; // +30% if watered
        } else {
            growthChance -= 0.25; // -25% if not watered
            
            // FLOWER POT: Double durability penalty if not watered
            if (isFlowerPot) {
                double currentDurability = plantedFlower.getDurability();
                plantedFlower.setDurability(currentDurability - 1); // Extra -1 durability
            }
        }
        
        // Weeding only matters for regular plots
        if (!isFlowerPot) {
            if (isWeeded) {
                growthChance += 0.15; // +15% if weeded
            } else {
                growthChance -= 0.10; // -10% if weedy
            }
        }
        
        if (isFertilized) {
            growthChance += 0.20; // +20% if fertilized
        }
        
        // Clamp between 10% and 99%
        growthChance = Math.max(0.10, Math.min(0.99, growthChance));
        
        // Roll for growth
        boolean shouldGrow = Math.random() < growthChance;
        
        // Growth logic based on current stage
        String currentStage = plantedFlower.getGrowthStage();
        int daysPlanted = plantedFlower.getDaysPlanted();
        boolean didGrow = false;
        
        if (shouldGrow) {
            // Progress to next stage if enough days have passed
            if (currentStage.equals("Seed") && daysPlanted >= 3) {
                plantedFlower.setGrowthStage("Seedling");
                didGrow = true;
            } else if (currentStage.equals("Seedling") && daysPlanted >= 7) {
                plantedFlower.setGrowthStage("Bloomed");
                didGrow = true;
            } else if (currentStage.equals("Bloomed") && daysPlanted >= 12) {
                plantedFlower.setGrowthStage("Matured");
                didGrow = true;
            } else if (currentStage.equals("Matured") && daysPlanted >= 20) {
                // Check if flower mutates or withers
                // Higher difficulty flowers have better mutation chance
                double mutationChance = 0.05 + (difficulty * 0.02);
                
                if (isFertilized) {
                    mutationChance *= 2; // Double chance if fertilized
                }
                
                if (Math.random() < mutationChance) {
                    plantedFlower.setGrowthStage("Mutated");
                } else {
                    plantedFlower.setGrowthStage("Withered");
                }
                didGrow = true;
            }
        } else if (!isWatered) {
            // Plant might take damage if not cared for
            // Harder flowers are more resilient
            double damageChance = 0.30 - (difficulty * 0.04);
            
            if (Math.random() < damageChance) {
                double currentDurability = plantedFlower.getDurability();
                plantedFlower.setDurability(currentDurability - 1);
                
                // If durability reaches 0, plant withers prematurely
                if (plantedFlower.getDurability() <= 0) {
                    plantedFlower.setGrowthStage("Withered");
                    didGrow = true; // Changed state, so return true
                }
            }
        }
        
        // Reset daily states
        this.isWatered = false;
        if (!isFlowerPot) {
            this.isWeeded = Math.random() > 0.7; // 30% chance of weeds appearing (not for flower pots)
        } else {
            this.isWeeded = true; // Flower pots stay weed-free
        }
        
        // Fertilizer has chance to fade each day
        if (this.isFertilized && Math.random() > 0.5) {
            this.isFertilized = false;
        }
        
        return didGrow;
    }
    
    /**
     * Checks if the plot is occupied
     * @return true if there is a plant in the plot
     */
    public boolean isOccupied() {
        return plantedFlower != null;
    }
    
    /**
     * Gets the plant in this plot
     * @return The flower in this plot, or null if empty
     */
    public Flower getPlantedFlower() {
        return plantedFlower;
    }
    
    /**
     * Checks if the plot is watered
     * @return true if watered
     */
    public boolean isWatered() {
        return isWatered;
    }
    
    /**
     * Checks if the plot is weeded
     * @return true if weeded
     */
    public boolean isWeeded() {
        return isWeeded;
    }
    
    /**
     * Checks if the plot is fertilized
     * @return true if fertilized
     */
    public boolean isFertilized() {
        return isFertilized;
    }
    
    /**
     * Gets the soil quality
     * @return The soil quality
     */
    public String getSoilQuality() {
        return soilQuality;
    }
    
    /**
     * Sets the soil quality
     * @param soilQuality The new soil quality
     */
    public void setSoilQuality(String soilQuality) {
        this.soilQuality = soilQuality;
    }
    
    /**
     * Gets a description of the garden plot
     * @return A string describing the garden plot
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        // Different icon for flower pot vs regular plot
        if (isFlowerPot) {
            sb.append("🪴 Flower Pot [Soil: ").append(soilQuality).append("]").append("\n");
        } else {
            sb.append("📦 Garden Plot [Soil: ").append(soilQuality).append("]").append("\n");
        }
        
        if (isOccupied()) {
            sb.append("  Plant: ").append(plantedFlower.getName()).append(" (").append(plantedFlower.getGrowthStage()).append(")\n");
            sb.append("  Days Planted: ").append(plantedFlower.getDaysPlanted()).append("\n");
            sb.append("  Durability: ").append(plantedFlower.getDurability()).append("\n");
        } else {
            sb.append("  [Empty]\n");
        }
        
        sb.append("  Watered: ").append(isWatered ? "✅" : "❌").append("\n");
        
        // Only show weeding status for regular plots
        if (!isFlowerPot) {
            sb.append("  Weeded: ").append(isWeeded ? "✅" : "❌").append("\n");
        }
        
        sb.append("  Fertilized: ").append(isFertilized ? "✅" : "❌").append("\n");
        
        return sb.toString();
    }
}
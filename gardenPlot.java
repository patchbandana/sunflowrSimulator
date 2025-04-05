/* 
 * Description: Garden Plot class for the sunflowrSimulator
 * Represents a single plot of land where flowers can be planted
 */
package sunflowrSimulator;

public class gardenPlot {
    // Current plant in this garden plot
    private Flower plantedFlower;
    
    // Garden plot state
    private boolean isWatered;
    private boolean isWeeded;
    private boolean isFertilized;
    private String soilQuality; // "Poor", "Average", "Good", "Excellent"
    
    /**
     * Creates a new garden plot with default values
     */
    public gardenPlot() {
        this.plantedFlower = null;
        this.isWatered = false;
        this.isWeeded = true; // Start with a weeded plot
        this.isFertilized = false;
        this.soilQuality = "Average";
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
        
        // Plant the flower - set it to planted and set days planted to 1
        flower.setDaysPlanted(1);
        this.plantedFlower = flower;
        return true;
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
     * @return true if weeding was successful
     */
    public boolean weedPlot() {
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
     * Advances the day for this garden plot, grows the plant if conditions are met
     * @return true if the plant grew, false otherwise
     */
    public boolean advanceDay() {
        if (!isOccupied()) {
            // Reset daily states
            this.isWatered = false;
            this.isWeeded = Math.random() > 0.7; // 30% chance of weeds appearing
            return false;
        }
        
        // Increment days planted
        plantedFlower.setDaysPlanted(plantedFlower.getDaysPlanted() + 1);
        
        // Check if conditions are right for growth
        boolean canGrow = isWatered && isWeeded;
        
        // Growth logic based on current stage and conditions
        String currentStage = plantedFlower.getGrowthStage();
        int daysPlanted = plantedFlower.getDaysPlanted();
        
        if (canGrow) {
            // Growth based on current stage and days planted
            if (currentStage.equals("Seed") && daysPlanted >= 3) {
                plantedFlower.setGrowthStage("Seedling");
                return true;
            } else if (currentStage.equals("Seedling") && daysPlanted >= 7) {
                plantedFlower.setGrowthStage("Bloomed");
                return true;
            } else if (currentStage.equals("Bloomed") && daysPlanted >= 12) {
                plantedFlower.setGrowthStage("Matured");
                return true;
            } else if (currentStage.equals("Matured") && daysPlanted >= 20) {
                // Check if flower withers or becomes special
                if (Math.random() > 0.9 && isFertilized) {
                    plantedFlower.setGrowthStage("Mutated");
                } else {
                    plantedFlower.setGrowthStage("Withered");
                }
                return true;
            }
        } else {
            // Plant might wither if not cared for
            if (!isWatered && Math.random() > 0.7) {
                plantedFlower.setDurability(plantedFlower.getDurability() - 1);
                
                // If durability reaches 0, plant withers
                if (plantedFlower.getDurability() <= 0) {
                    plantedFlower.setGrowthStage("Withered");
                    return true;
                }
            }
        }
        
        // Reset daily states
        this.isWatered = false;
        this.isWeeded = Math.random() > 0.7; // 30% chance of weeds appearing
        this.isFertilized = isFertilized && Math.random() > 0.5; // 50% chance of fertilizer fading
        
        return false;
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
        sb.append("📦 Garden Plot [Soil: ").append(soilQuality).append("]").append("\n");
        
        if (isOccupied()) {
            sb.append("  Plant: ").append(plantedFlower.getName()).append(" (").append(plantedFlower.getGrowthStage()).append(")\n");
            sb.append("  Days Planted: ").append(plantedFlower.getDaysPlanted()).append("\n");
            sb.append("  Durability: ").append(plantedFlower.getDurability()).append("\n");
        } else {
            sb.append("  [Empty Plot]\n");
        }
        
        sb.append("  Watered: ").append(isWatered ? "✅" : "❌").append("\n");
        sb.append("  Weeded: ").append(isWeeded ? "✅" : "❌").append("\n");
        sb.append("  Fertilized: ").append(isFertilized ? "✅" : "❌").append("\n");
        
        return sb.toString();
    }
}
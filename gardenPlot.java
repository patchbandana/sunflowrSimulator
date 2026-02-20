/* 
 * Description: Garden Plot class for the sunflowrSimulator
 * Represents a single plot of land where flowers can be planted
 * 
 * UPDATES (Pre-Release):
 * - Added consecutive days without water tracking
 * - Enhanced watering penalties (escalating damage, day 7 instant wither)
 * - Added durability penalty for not weeding
 * - Soil quality now affects mutation probability (Bad: 0.5x, Magic: 2x)
 * - Soil quality now affects withering probability (Magic soil extends lifecycle)
 * - UPDATED: Mulcher effect reduces weed growth to 0.25x speed
 */

public class gardenPlot {
    // Current plant in this garden plot
    private Flower plantedFlower;
    
    // Garden plot state
    private boolean isWatered;
    private boolean isWeeded;
    private boolean isFertilized;
    private String soilQuality; // "Bad", "Average", "Good", "Great", "Magic"
    
    // Flower pot functionality
    private boolean isFlowerPot;
    
    // NEW: Track consecutive days without water for escalating penalties
    private int consecutiveDaysWithoutWater;
    
    /**
     * Creates a new garden plot with default values
     */
    public gardenPlot() {
        this.plantedFlower = null;
        this.isWatered = false;
        this.isWeeded = true;
        this.isFertilized = false;
        this.soilQuality = "Average";
        this.isFlowerPot = false;
        this.consecutiveDaysWithoutWater = 0;
    }
    
    /**
     * Creates a new flower pot (special type of garden plot)
     * @param isFlowerPot true to create a flower pot
     */
    public gardenPlot(boolean isFlowerPot) {
        this();
        this.isFlowerPot = isFlowerPot;
        if (isFlowerPot) {
            this.isWeeded = true;
            this.soilQuality = "Good";
        }
    }
    
    /**
     * Gets consecutive days without water
     * @return Number of consecutive days without water
     */
    public int getConsecutiveDaysWithoutWater() {
        return consecutiveDaysWithoutWater;
    }
    
    /**
     * Sets consecutive days without water (for save/load)
     * @param days Number of days
     */
    public void setConsecutiveDaysWithoutWater(int days) {
        this.consecutiveDaysWithoutWater = days;
    }
    
    public boolean isFlowerPot() {
        return isFlowerPot;
    }
    
    public void setFlowerPot(boolean isFlowerPot) {
        this.isFlowerPot = isFlowerPot;
        if (isFlowerPot) {
            this.isWeeded = true;
        }
    }
    
    /**
     * Checks if a flower can be planted in this plot based on soil quality
     */
    public boolean hasSufficientSoilQuality(Flower flower) {
        int difficulty = FlowerRegistry.getFlowerDifficulty(flower.getName());
        
        switch (difficulty) {
            case 5:
                return soilQuality.equals("Great") || soilQuality.equals("Magic");
            case 4:
                return soilQuality.equals("Good") || soilQuality.equals("Great") || 
                       soilQuality.equals("Magic");
            case 3:
                return !soilQuality.equals("Bad");
            default:
                return true;
        }
    }
    
    /**
     * Checks if a flower can be planted in this flower pot
     */
    public boolean canPlantInFlowerPot(Flower flower) {
        if (!isFlowerPot) {
            return true;
        }
        
        int difficulty = FlowerRegistry.getFlowerDifficulty(flower.getName());
        if (difficulty >= 4) {
            return false;
        }
        
        String flowerName = flower.getName();
        if (!FlowerRegistry.flowerExists(flowerName)) {
            return true;
        }
        
        String nameLower = flowerName.toLowerCase();
        if (nameLower.contains("bush") || nameLower.contains("tree")) {
            return false;
        }
        
        return true;
    }
    
    public boolean plantFlower(Flower flower) {
        if (isOccupied()) {
            return false;
        }
        
        if (!flower.getGrowthStage().equals("Seed")) {
            return false;
        }
        
        if (!hasSufficientSoilQuality(flower)) {
            return false;
        }
        
        if (isFlowerPot && !canPlantInFlowerPot(flower)) {
            return false;
        }
        
        flower.setDaysPlanted(1);
        this.plantedFlower = flower;
        this.consecutiveDaysWithoutWater = 0; // Reset counter for new plant
        return true;
    }
    
    public void forcePlantFlower(Flower flower) {
        this.plantedFlower = flower;
    }
    
    public Flower harvestFlower() {
        if (!isOccupied()) {
            return null;
        }
        
        Flower harvestedFlower = this.plantedFlower;
        this.plantedFlower = null;
        this.isWatered = false;
        this.consecutiveDaysWithoutWater = 0; // Reset counter
        return harvestedFlower;
    }
    
    public boolean waterPlot() {
        if (!isOccupied()) {
            return false;
        }
        
        if (isWatered) {
            return false;
        }
        
        this.isWatered = true;
        return true;
    }
    
    public boolean weedPlot() {
        if (isFlowerPot) {
            return false;
        }
        
        if (isWeeded) {
            return false;
        }
        
        this.isWeeded = true;
        return true;
    }
    
    public boolean fertilizePlot() {
        if (isFertilized) {
            return false;
        }
        
        this.isFertilized = true;
        return true;
    }
    
    public void setWatered(boolean watered) {
        this.isWatered = watered;
    }
    
    public void setWeeded(boolean weeded) {
        this.isWeeded = weeded;
    }
    
    public void setFertilized(boolean fertilized) {
        this.isFertilized = fertilized;
    }
    
    /**
     * Gets the soil quality multiplier for mutation chance
     * @return Multiplier (0.5 for Bad, 2.0 for Magic)
     */
    private double getSoilMutationMultiplier() {
        switch (soilQuality) {
            case "Bad": return 0.5;
            case "Average": return 1.0;
            case "Good": return 1.3;
            case "Great": return 1.6;
            case "Magic": return 2.0;
            default: return 1.0;
        }
    }
    
    /**
     * Gets the probability of withering (instead of staying matured)
     * Better soil = lower wither chance = longer lifecycle
     * @return Probability 0.0-1.0
     */
    private double getSoilWitherProbability() {
        switch (soilQuality) {
            case "Bad": return 0.80;      // 80% chance to wither
            case "Average": return 0.60;  // 60% chance
            case "Good": return 0.40;     // 40% chance
            case "Great": return 0.25;    // 25% chance
            case "Magic": return 0.15;    // 15% chance (nearly double lifecycle)
            default: return 0.60;
        }
    }
    
    /**
     * Advances the day for this garden plot, grows the plant if conditions are met
     * ENHANCED: Soil quality affects mutation/withering, escalating water penalties
     * UPDATED: Mulcher reduces weed growth to 0.25x speed
     * @param player Reference to player (for mulcher check)
     */
    public boolean advanceDay(Player1 player) {
        if (!isOccupied()) {
            this.isWatered = false;
            if (!isFlowerPot) {
                // MODIFIED: Apply mulcher effect to weed growth
                double weedChance = 0.3; // Base 30% chance of weeds (inverse of 70% clean)
                
                if (player != null && player.isMulcherActive()) {
                    weedChance *= 0.25; // Reduce to 7.5% chance with mulcher
                }
                
                this.isWeeded = Math.random() > weedChance;
            }
            return false;
        }
        
        // Increment days planted
        plantedFlower.setDaysPlanted(plantedFlower.getDaysPlanted() + 1);
        
        // Get flower difficulty
        int difficulty = FlowerRegistry.getFlowerDifficulty(plantedFlower.getName());
        if (difficulty == -1) {
            difficulty = 3;
        }
        
        // ENHANCED WATERING PENALTY SYSTEM
        if (isWatered) {
            consecutiveDaysWithoutWater = 0; // Reset counter
        } else {
            consecutiveDaysWithoutWater++; // Increment counter
            
            // Day 7 without water: Instant wither
            if (consecutiveDaysWithoutWater >= 7) {
                plantedFlower.setDurability(0); // Triggers auto-wither in Flower.java
                // Plant is now withered, reset counter
                consecutiveDaysWithoutWater = 0;
            } else {
                // Escalating durability penalties for consecutive days without water
                // Day 1-3: Standard penalty
                // Day 4: 2x penalty
                // Day 5: 3x penalty  
                // Day 6: 4x penalty
                double waterPenalty = 1.0;
                if (consecutiveDaysWithoutWater >= 4) {
                    waterPenalty = consecutiveDaysWithoutWater - 2; // Day 4 = 2x, Day 5 = 3x, etc.
                }
                
                // Base damage for not watering
                double baseDamage = 1.0;
                if (isFlowerPot) {
                    baseDamage = 2.0; // Flower pots get double base penalty
                }
                
                double totalDamage = baseDamage * waterPenalty;
                plantedFlower.setDurability(plantedFlower.getDurability() - totalDamage);
            }
        }
        
        // ENHANCED WEEDING PENALTY SYSTEM (only for regular plots)
        if (!isFlowerPot && !isWeeded) {
            // Not weeding causes durability damage (less severe than not watering)
            plantedFlower.setDurability(plantedFlower.getDurability() - 0.3);
        }
        
        // Check if plant withered from neglect before trying growth
        if (plantedFlower.getGrowthStage().equals("Withered")) {
            this.isWatered = false;
            if (!isFlowerPot) {
                // MODIFIED: Apply mulcher effect to weed growth
                double weedChance = 0.3;
                if (player != null && player.isMulcherActive()) {
                    weedChance *= 0.25;
                }
                this.isWeeded = Math.random() > weedChance;
            } else {
                this.isWeeded = true;
            }
            if (this.isFertilized && Math.random() > 0.5) {
                this.isFertilized = false;
            }
            if (this.isFertilized && Math.random() < 0.0075) {
                upgradeSoilQuality();
            }
            return true; // State changed (withered)
        }
        
        // Calculate growth chance
        double baseGrowthChance = 0.95 - (difficulty * 0.10);
        double growthChance = baseGrowthChance;
        
        // Water is REQUIRED for growth
        if (isWatered) {
            growthChance += 0.30;
        } else {
            growthChance = 0; // NO GROWTH without water
        }
        
        // Weeding bonus (only for regular plots)
        if (!isFlowerPot) {
            if (isWeeded) {
                growthChance += 0.15;
            } else {
                growthChance -= 0.10;
            }
        }
        
        if (isFertilized) {
            growthChance += 0.20;
        }
        
        // Clamp between 0% and 99%
        growthChance = Math.max(0.0, Math.min(0.99, growthChance));
        
        // Roll for growth
        boolean shouldGrow = Math.random() < growthChance;
        
        // Growth logic
        String currentStage = plantedFlower.getGrowthStage();
        int daysPlanted = plantedFlower.getDaysPlanted();
        boolean didGrow = false;
        
        if (shouldGrow) {
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
                // ENHANCED: Soil quality affects both mutation and withering
                
                // Get base mutation chance from CSV
                double baseMutationChance = 0.05 + (difficulty * 0.02);
                if (isFertilized) {
                    baseMutationChance *= 2;
                }
                
                // Apply soil quality multiplier
                double soilMultiplier = getSoilMutationMultiplier();
                double finalMutationChance = baseMutationChance * soilMultiplier;
                
                if (Math.random() < finalMutationChance) {
                    plantedFlower.setGrowthStage("Mutated");
                    didGrow = true;
                } else {
                    // ENHANCED: Probability-based withering instead of automatic
                    double witherProbability = getSoilWitherProbability();
                    
                    if (Math.random() < witherProbability) {
                        plantedFlower.setGrowthStage("Withered");
                        didGrow = true;
                    }
                    // If doesn't wither, stays Matured for another day
                }
            }
        }
        
        // Reset daily states
        this.isWatered = false;
        if (!isFlowerPot) {
            // MODIFIED: Apply mulcher effect to weed growth
            double weedChance = 0.3; // Base 30% chance of weeds
            
            if (player != null && player.isMulcherActive()) {
                weedChance *= 0.25; // Reduce to 7.5% chance with mulcher
            }
            
            this.isWeeded = Math.random() > weedChance;
        } else {
            this.isWeeded = true;
        }
        
        // Fertilizer fade
        if (this.isFertilized && Math.random() > 0.5) {
            this.isFertilized = false;
        }
        
        // Soil quality upgrade
        if (this.isFertilized && Math.random() < 0.0075) {
            upgradeSoilQuality();
        }
        
        return didGrow;
    }
    
    private void upgradeSoilQuality() {
        switch (soilQuality) {
            case "Bad":
                soilQuality = "Average";
                break;
            case "Average":
                soilQuality = "Good";
                break;
            case "Good":
                soilQuality = "Great";
                break;
            case "Great":
                soilQuality = "Magic";
                break;
            case "Magic":
                return;
        }
    }
    
    public boolean isOccupied() {
        return plantedFlower != null;
    }
    
    public Flower getPlantedFlower() {
        return plantedFlower;
    }
    
    public boolean isWatered() {
        return isWatered;
    }
    
    public boolean isWeeded() {
        return isWeeded;
    }
    
    public boolean isFertilized() {
        return isFertilized;
    }
    
    public String getSoilQuality() {
        return soilQuality;
    }
    
    public void setSoilQuality(String soilQuality) {
        this.soilQuality = soilQuality;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        if (isFlowerPot) {
            sb.append("🪴 Flower Pot [Soil: ").append(getSoilQualityEmoji()).append(" ").append(soilQuality).append("]").append("\n");
        } else {
            sb.append("ðŸ“¦ Garden Plot [Soil: ").append(getSoilQualityEmoji()).append(" ").append(soilQuality).append("]").append("\n");
        }
        
        if (isOccupied()) {
            sb.append("  Plant: ").append(plantedFlower.getName()).append(" (").append(plantedFlower.getGrowthStage()).append(")\n");
            sb.append("  Days Planted: ").append(plantedFlower.getDaysPlanted()).append("\n");
            sb.append("  Durability: ").append(plantedFlower.getDurability()).append("\n");
            if (consecutiveDaysWithoutWater > 0) {
                sb.append("  ⚠️ Days without water: ").append(consecutiveDaysWithoutWater).append("/7\n");
            }
        } else {
            sb.append("  [Empty]\n");
        }
        
        sb.append("  Watered: ").append(isWatered ? "âœ…" : "âŒ").append("\n");
        
        if (!isFlowerPot) {
            sb.append("  Weeded: ").append(isWeeded ? "âœ…" : "âŒ").append("\n");
        }
        
        sb.append("  Fertilized: ").append(isFertilized ? "âœ…" : "âŒ").append("\n");
        
        return sb.toString();
    }
    
    private String getSoilQualityEmoji() {
        switch (soilQuality) {
            case "Bad": return "ðŸ’€";
            case "Average": return "ðŸŒ±";
            case "Good": return "ðŸŒ¿";
            case "Great": return "✨";
            case "Magic": return "ðŸ”®";
            default: return "â“";
        }
    }
}
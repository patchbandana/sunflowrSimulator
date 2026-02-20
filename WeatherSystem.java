/* WeatherSystem.java
 * Manages weather events and their effects on the garden
 * Updated: November 25, 2025 - Enhanced with diverse weather types
 * 
 * WEATHER MECHANICS:
 * - Triggers 25% of nights (independent of dreams/hints)
 * - Rain (40%): All garden plants start watered at 0 NRG cost
 * - Clear (35%): No weather event
 * - Snow (10%): Prevents weed growth, -50% durability to unprotected plants
 * - Thunderstorm (10%): All plants watered, -10 durability to all plants
 * - Earthquake (2%): -90% durability to ALL plants
 * - Hurricane (2%): All plants watered, -50 durability to all plants
 * - Mole Infestation (0.5%): One random unprotected plant unearthed
 * - Fairy Visit (0.5%): One random plant mutated OR one soil upgraded
 * 
 * PROTECTED LOCATIONS:
 * - Flower pots (protected from snow and moles)
 * - Greenhouse structures (future - full protection)
 * - Items in backpack (not affected by weather)
 */

import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class WeatherSystem {
    
    // Weather types
    public enum Weather {
        CLEAR,          // 35% - No weather event
        RAIN,           // 40% - All plants watered
        SNOW,           // 10% - No weeds, durability penalty to unprotected
        THUNDERSTORM,   // 10% - Watered but durability loss
        EARTHQUAKE,     // 2% - Massive durability loss to all
        HURRICANE,      // 2% - Watered with heavy damage
        MOLE_INFESTATION, // 0.5% - One plant unearthed
        FAIRY_VISIT     // 0.5% - Beneficial event
    }
    
    private static final Random random = new Random();
    private static Weather currentWeather = Weather.CLEAR;
    private static boolean weatherOccurredTonight = false;
    
    // Track fairy visit details for display
    private static String fairyVisitDetails = "";
    
    /**
     * Determines if weather should occur
     * Weather has a 25% chance each night, independent of dreams/hints
     * 
     * @return true if weather should occur tonight
     */
    public static boolean shouldWeatherOccur() {
        weatherOccurredTonight = false;
        currentWeather = Weather.CLEAR;
        fairyVisitDetails = "";
        
        // 25% chance of weather check
        if (Math.random() >= 0.25) {
            return false;
        }
        
        // We're in the 25% - select weather type
        weatherOccurredTonight = true;
        selectWeatherType();
        return true;
    }
    
    /**
     * Selects a random weather type with proper distribution
     * Total = 100%
     * - Clear: 35%
     * - Rain: 40%
     * - Snow: 10%
     * - Thunderstorm: 10%
     * - Earthquake: 2%
     * - Hurricane: 2%
     * - Mole Infestation: 0.5%
     * - Fairy Visit: 0.5%
     */
    private static void selectWeatherType() {
        double roll = Math.random() * 100;
        
        if (roll < 40) {
            currentWeather = Weather.RAIN;
        } else if (roll < 75) {
            currentWeather = Weather.CLEAR;
        } else if (roll < 85) {
            currentWeather = Weather.SNOW;
        } else if (roll < 95) {
            currentWeather = Weather.THUNDERSTORM;
        } else if (roll < 97) {
            currentWeather = Weather.EARTHQUAKE;
        } else if (roll < 99) {
            currentWeather = Weather.HURRICANE;
        } else if (roll < 99.5) {
            currentWeather = Weather.MOLE_INFESTATION;
        } else {
            currentWeather = Weather.FAIRY_VISIT;
        }
    }
    
    /**
     * Gets the current weather
     */
    public static Weather getCurrentWeather() {
        return currentWeather;
    }
    
    /**
     * Checks if weather occurred tonight
     */
    public static boolean didWeatherOccur() {
        return weatherOccurredTonight;
    }
    
    /**
     * Applies weather effects to all garden plots
     * Called during advanceDay AFTER plants have grown
     * 
     * @param player The player whose garden is affected
     */
    public static void applyWeatherEffects(Player1 player) {
        if (!weatherOccurredTonight || currentWeather == Weather.CLEAR) {
            return;
        }
        
        switch (currentWeather) {
            case RAIN:
                applyRainEffects(player);
                break;
            case SNOW:
                applySnowEffects(player);
                break;
            case THUNDERSTORM:
                applyThunderstormEffects(player);
                break;
            case EARTHQUAKE:
                applyEarthquakeEffects(player);
                break;
            case HURRICANE:
                applyHurricaneEffects(player);
                break;
            case MOLE_INFESTATION:
                applyMoleInfestationEffects(player);
                break;
            case FAIRY_VISIT:
                applyFairyVisitEffects(player);
                break;
            default:
                break;
        }
    }
    
    /**
     * Rain: All garden plants start watered (0 NRG cost)
     */
    private static void applyRainEffects(Player1 player) {
        int wateredCount = 0;
        
        for (gardenPlot plot : player.getGardenPlots()) {
            if (plot.isOccupied() && !plot.isWatered()) {
                plot.setWatered(true);
                plot.setConsecutiveDaysWithoutWater(0);
                wateredCount++;
            }
        }
        
        if (wateredCount > 0) {
            player.addJournalEntry("[Rain] Rain watered " + wateredCount + (wateredCount == 1 ? " plant" : " plants") + " overnight!");
        }
    }
    
    /**
     * Snow: Prevents weed growth, -50% durability to unprotected plants
     * Protected: Flower pots, greenhouses (future)
     */
    private static void applySnowEffects(Player1 player) {
        int affectedPlants = 0;
        int protectedPlants = 0;
        
        for (gardenPlot plot : player.getGardenPlots()) {
            if (plot.isOccupied()) {
                Flower plant = plot.getPlantedFlower();
                
                if (!plot.isFlowerPot()) {
                    double currentDurability = plant.getDurability();
                    double durabilityLoss = currentDurability * 0.5;
                    plant.setDurability(currentDurability - durabilityLoss);
                    affectedPlants++;
                } else {
                    protectedPlants++;
                }
            }
            
            if (!plot.isFlowerPot()) {
                plot.setWeeded(true);
            }
        }
        
        if (affectedPlants > 0 && protectedPlants > 0) {
            player.addJournalEntry("[Snow] Snow damaged " + affectedPlants + 
                    (affectedPlants == 1 ? " plant" : " plants") + " but " + protectedPlants + " in pots were protected. Weeds prevented.");
        } else if (affectedPlants > 0) {
            player.addJournalEntry("[Snow] Snow damaged " + affectedPlants + 
                    (affectedPlants == 1 ? " plant" : " plants") + " and prevented weed growth.");
        } else if (protectedPlants > 0) {
            player.addJournalEntry("[Snow] Snow fell, but all plants were protected in pots. Weeds prevented.");
        }
    }
    
    /**
     * Thunderstorm: All plants watered, -10 durability to all plants
     */
    private static void applyThunderstormEffects(Player1 player) {
        int wateredCount = 0;
        int damagedCount = 0;
        
        for (gardenPlot plot : player.getGardenPlots()) {
            if (plot.isOccupied()) {
                Flower plant = plot.getPlantedFlower();
                
                if (!plot.isWatered()) {
                    plot.setWatered(true);
                    plot.setConsecutiveDaysWithoutWater(0);
                    wateredCount++;
                }
                
                double currentDurability = plant.getDurability();
                plant.setDurability(currentDurability - 10);
                damagedCount++;
            }
        }
        
        if (wateredCount > 0) {
            player.addJournalEntry("[Storm] A thunderstorm watered " + wateredCount + 
                    (wateredCount == 1 ? " plant" : " plants") + " but damaged " + damagedCount + (damagedCount == 1 ? " plant" : " plants") + "!");
        }
    }
    
    /**
     * Earthquake: All plants lose 90% durability
     */
    private static void applyEarthquakeEffects(Player1 player) {
        int affectedPlants = 0;
        
        for (gardenPlot plot : player.getGardenPlots()) {
            if (plot.isOccupied()) {
                Flower plant = plot.getPlantedFlower();
                double currentDurability = plant.getDurability();
                double durabilityLoss = currentDurability * 0.9;
                plant.setDurability(currentDurability - durabilityLoss);
                affectedPlants++;
            }
        }
        
        if (affectedPlants > 0) {
            player.addJournalEntry("[Earthquake] An earthquake struck! All " + affectedPlants + 
                    (affectedPlants == 1 ? " plant" : " plants") + " severely damaged (90% durability lost)!");
        }
    }
    
    /**
     * Hurricane: All plants watered, -50 durability to all plants
     */
    private static void applyHurricaneEffects(Player1 player) {
        int wateredCount = 0;
        int damagedCount = 0;
        
        for (gardenPlot plot : player.getGardenPlots()) {
            if (plot.isOccupied()) {
                Flower plant = plot.getPlantedFlower();
                
                if (!plot.isWatered()) {
                    plot.setWatered(true);
                    plot.setConsecutiveDaysWithoutWater(0);
                    wateredCount++;
                }
                
                double currentDurability = plant.getDurability();
                plant.setDurability(currentDurability - 50);
                damagedCount++;
            }
        }
        
        if (wateredCount > 0) {
            player.addJournalEntry("[Hurricane] A hurricane watered " + wateredCount + 
                    (wateredCount == 1 ? " plant" : " plants") + " but heavily damaged " + damagedCount + (damagedCount == 1 ? " plant" : " plants") + " (50 durability)!");
        }
    }
    
    /**
     * Mole Infestation: One random unprotected plant is unearthed
     * - If in flower pot: Protected, moles can't reach
     * - If matured/mutated: Harvested to inventory
     * - If seed/seedling/bloomed: Destroyed (plot emptied)
     */
    private static void applyMoleInfestationEffects(Player1 player) {
        List<gardenPlot> vulnerablePlots = new ArrayList<>();
        
        // Find all occupied regular plots (not flower pots)
        for (gardenPlot plot : player.getGardenPlots()) {
            if (plot.isOccupied() && !plot.isFlowerPot()) {
                vulnerablePlots.add(plot);
            }
        }
        
        if (vulnerablePlots.isEmpty()) {
            player.addJournalEntry("[Moles] Moles visited but found no vulnerable plants to disturb.");
            return;
        }
        
        // Select random plot
        gardenPlot targetPlot = vulnerablePlots.get(random.nextInt(vulnerablePlots.size()));
        Flower targetPlant = targetPlot.getPlantedFlower();
        String plantName = targetPlant.getName();
        String stage = targetPlant.getGrowthStage();
        
        // Determine outcome based on stage
        if (stage.equals("Matured") || stage.equals("Mutated")) {
            // Harvest to inventory
            Flower harvestedPlant = targetPlot.harvestFlower();
            player.addToInventory(harvestedPlant);
            player.addJournalEntry("[Moles] Moles unearthed your " + plantName + " (" + stage + 
                    ") and it was harvested to your inventory!");
        } else {
            // Destroy the plant
            targetPlot.harvestFlower(); // Remove from plot
            player.addJournalEntry("[Moles] Moles destroyed your " + plantName + " (" + stage + 
                    ") by digging it up!");
        }
    }
    
    /**
     * Fairy Visit: Beneficial event
     * - 50% chance: One random plant advanced to Mutated
     * - 50% chance: One random plot's soil upgraded
     */
    private static void applyFairyVisitEffects(Player1 player) {
        boolean giftPlantMutation = random.nextBoolean();
        
        if (giftPlantMutation) {
            // Try to mutate a plant
            List<gardenPlot> mutablePlots = new ArrayList<>();
            
            for (gardenPlot plot : player.getGardenPlots()) {
                if (plot.isOccupied()) {
                    String stage = plot.getPlantedFlower().getGrowthStage();
                    // Can mutate anything except already mutated or withered
                    if (!stage.equals("Mutated") && !stage.equals("Withered")) {
                        mutablePlots.add(plot);
                    }
                }
            }
            
            if (!mutablePlots.isEmpty()) {
                gardenPlot targetPlot = mutablePlots.get(random.nextInt(mutablePlots.size()));
                Flower targetPlant = targetPlot.getPlantedFlower();
                String plantName = targetPlant.getName();
                String oldStage = targetPlant.getGrowthStage();
                
                targetPlant.setGrowthStage("Mutated");
                
                fairyVisitDetails = "mutated " + plantName + " (" + oldStage + " -> Mutated)";
                player.addJournalEntry("[Fairy] The fairies blessed your " + plantName + 
                        ", transforming it into a mutated beauty!");
            } else {
                // No plants to mutate, upgrade soil instead
                upgradeSoilInstead(player);
            }
        } else {
            // Upgrade soil
            upgradeSoilInstead(player);
        }
    }
    
    /**
     * Helper method to upgrade soil quality
     */
    private static void upgradeSoilInstead(Player1 player) {
        List<gardenPlot> upgradeablePlots = new ArrayList<>();
        
        for (gardenPlot plot : player.getGardenPlots()) {
            // Can upgrade any plot that's not already Magic soil
            if (!plot.getSoilQuality().equals("Magic")) {
                upgradeablePlots.add(plot);
            }
        }
        
        if (!upgradeablePlots.isEmpty()) {
            gardenPlot targetPlot = upgradeablePlots.get(random.nextInt(upgradeablePlots.size()));
            String oldSoil = targetPlot.getSoilQuality();
            
            // Upgrade soil by one tier
            switch (oldSoil) {
                case "Bad":
                    targetPlot.setSoilQuality("Average");
                    break;
                case "Average":
                    targetPlot.setSoilQuality("Good");
                    break;
                case "Good":
                    targetPlot.setSoilQuality("Great");
                    break;
                case "Great":
                    targetPlot.setSoilQuality("Magic");
                    break;
            }
            
            String newSoil = targetPlot.getSoilQuality();
            String plotType = targetPlot.isFlowerPot() ? "flower pot" : "garden plot";
            
            fairyVisitDetails = "upgraded " + plotType + " soil (" + oldSoil + " -> " + newSoil + ")";
            player.addJournalEntry("[Fairy] The fairies blessed your " + plotType + 
                    ", upgrading the soil from " + oldSoil + " to " + newSoil + "!");
        } else {
            fairyVisitDetails = "visited but all was already perfect";
            player.addJournalEntry("[Fairy] The fairies visited and admired your perfect garden!");
        }
    }
    
    /**
     * Gets a description of the weather event for display
     */
    public static String getWeatherDescription() {
        if (!weatherOccurredTonight) {
            return null;
        }
        
        switch (currentWeather) {
            case CLEAR:
                return null; // No message for clear weather
                
            case RAIN:
                return "[Rain] Gentle rain fell through the night, watering your garden.\n" +
                       "All plants in the garden are now watered at no cost!";
                
            case SNOW:
                return "[Snow] Snow blanketed the garden overnight.\n" +
                       "Weeds were prevented from growing, but exposed plants lost durability.\n" +
                       "(Flower pots were protected from the cold)";
                
            case THUNDERSTORM:
                return "[Storm] A fierce thunderstorm rolled through!\n" +
                       "The rain watered all plants, but lightning damaged them.\n" +
                       "All plants lost 10 durability.";
                
            case EARTHQUAKE:
                return "[Earthquake] A massive earthquake shook the land!\n" +
                       "The violent tremors severely damaged all plants in your garden.\n" +
                       "All plants lost 90% of their durability!";
                
            case HURRICANE:
                return "[Hurricane] A devastating hurricane struck!\n" +
                       "The torrential rains watered all plants, but the fierce winds caused heavy damage.\n" +
                       "All plants lost 50 durability!";
                
            case MOLE_INFESTATION:
                return "[Moles] A mole has been busy in your garden!\n" +
                       "One of your plants has been unearthed.\n" +
                       "(Flower pots are safe from moles)";
                
            case FAIRY_VISIT:
                return "[Fairy] The fairies visited your garden during the night!\n" +
                       "They left a magical gift: " + fairyVisitDetails + ".\n" +
                       "How fortunate!";
                
            default:
                return null;
        }
    }
    
    /**
     * Gets a short weather status for garden summary
     */
    public static String getWeatherSummary() {
        if (!weatherOccurredTonight || currentWeather == Weather.CLEAR) {
            return null;
        }
        
        switch (currentWeather) {
            case RAIN:
                return "[Rain] Rain watered your garden";
            case SNOW:
                return "[Snow] Snow prevented weeds, damaged plants";
            case THUNDERSTORM:
                return "[Storm] Thunderstorm watered and damaged plants";
            case EARTHQUAKE:
                return "[Earthquake] Earthquake severely damaged all plants";
            case HURRICANE:
                return "[Hurricane] Hurricane watered plants but caused heavy damage";
            case MOLE_INFESTATION:
                return "[Moles] Moles unearthed a plant";
            case FAIRY_VISIT:
                return "[Fairy] Fairies blessed your garden";
            default:
                return null;
        }
    }
    
    /**
     * Checks if a specific plot is protected from weather
     * Currently only flower pots, future: greenhouses
     */
    public static boolean isPlotProtected(gardenPlot plot) {
        return plot.isFlowerPot();
    }
    
    /**
     * Resets weather state (for save/load compatibility)
     */
    public static void reset() {
        currentWeather = Weather.CLEAR;
        weatherOccurredTonight = false;
        fairyVisitDetails = "";
    }
}
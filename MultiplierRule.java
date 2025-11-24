/* MultiplierRule.java
 * Defines multiplier rules for bouquet auctions
 * 
 * MULTIPLIER TYPES:
 * - Composition-based (all withered, all sunflowers, etc.)
 * - Size-based (dozen roses)
 * - Named bouquet bonus
 * 
 * TODO: Eventually load from CSV file
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiplierRule {
    public final String name;
    public final String description;
    public final double multiplier;
    private final RuleChecker checker;
    
    /**
     * Interface for rule checking logic
     */
    @FunctionalInterface
    private interface RuleChecker {
        boolean test(Bouquet bouquet);
    }
    
    private MultiplierRule(String name, String description, double multiplier, RuleChecker checker) {
        this.name = name;
        this.description = description;
        this.multiplier = multiplier;
        this.checker = checker;
    }
    
    /**
     * Checks if this rule applies to the given bouquet
     */
    public boolean appliesTo(Bouquet bouquet) {
        return checker.test(bouquet);
    }
    
    // ========================================
    // RULE DEFINITIONS
    // ========================================
    
    /**
     * Gets all available multiplier rules
     * TODO: Load from CSV file instead
     */
    public static List<MultiplierRule> getAllRules() {
        List<MultiplierRule> rules = new ArrayList<>();
        
        // All Withered = 50x
        rules.add(new MultiplierRule(
            "all_withered",
            "All flowers are withered! (Beauty in decay)",
            50.0,
            bouquet -> {
                for (Flower flower : bouquet.getFlowers()) {
                    if (!flower.getGrowthStage().equals("Withered")) {
                        return false;
                    }
                }
                return true;
            }
        ));
        
        // All Sunflowers = 10x
        rules.add(new MultiplierRule(
            "all_sunflowers",
            "All sunflowers! (Summer glory)",
            10.0,
            bouquet -> {
                for (Flower flower : bouquet.getFlowers()) {
                    if (!flower.getName().toLowerCase().contains("sunflower")) {
                        return false;
                    }
                }
                return true;
            }
        ));
        
        // Dozen Roses = 20x
        rules.add(new MultiplierRule(
            "dozen_roses",
            "A perfect dozen roses! (Classic romance)",
            20.0,
            bouquet -> {
                if (bouquet.getFlowerCount() != 12) {
                    return false;
                }
                for (Flower flower : bouquet.getFlowers()) {
                    if (!flower.getName().toLowerCase().contains("rose")) {
                        return false;
                    }
                }
                return true;
            }
        ));
        
        // All Mutated = 30x
        rules.add(new MultiplierRule(
            "all_mutated",
            "All flowers are mutated! (Magical transformation)",
            30.0,
            bouquet -> {
                for (Flower flower : bouquet.getFlowers()) {
                    if (!flower.getGrowthStage().equals("Mutated")) {
                        return false;
                    }
                }
                return true;
            }
        ));
        
        // All Same Species = 5x
        rules.add(new MultiplierRule(
            "monoculture",
            "All the same flower type! (Perfect uniformity)",
            5.0,
            bouquet -> {
                if (bouquet.getFlowers().isEmpty()) {
                    return false;
                }
                String firstFlower = bouquet.getFlowers().get(0).getName();
                for (Flower flower : bouquet.getFlowers()) {
                    if (!flower.getName().equals(firstFlower)) {
                        return false;
                    }
                }
                return true;
            }
        ));
        
        // All Different Species = 3x
        rules.add(new MultiplierRule(
            "diversity",
            "Every flower is different! (Beautiful variety)",
            3.0,
            bouquet -> {
                java.util.Set<String> uniqueFlowers = new java.util.HashSet<>();
                for (Flower flower : bouquet.getFlowers()) {
                    uniqueFlowers.add(flower.getName());
                }
                return uniqueFlowers.size() == bouquet.getFlowerCount();
            }
        ));
        
        // All Matured = 8x
        rules.add(new MultiplierRule(
            "all_matured",
            "All flowers are fully matured! (Perfect timing)",
            8.0,
            bouquet -> {
                for (Flower flower : bouquet.getFlowers()) {
                    if (!flower.getGrowthStage().equals("Matured")) {
                        return false;
                    }
                }
                return true;
            }
        ));
        
        // All Bloomed = 4x
        rules.add(new MultiplierRule(
            "all_bloomed",
            "All flowers are freshly bloomed! (Spring beauty)",
            4.0,
            bouquet -> {
                for (Flower flower : bouquet.getFlowers()) {
                    if (!flower.getGrowthStage().equals("Bloomed")) {
                        return false;
                    }
                }
                return true;
            }
        ));
        
        // Large Bouquet (10-12 flowers) = 2x
        rules.add(new MultiplierRule(
            "large_bouquet",
            "A grand bouquet! (Impressive size)",
            2.0,
            bouquet -> bouquet.getFlowerCount() >= 10
        ));
        
        // Small Bouquet (3 flowers) = 1.5x
        rules.add(new MultiplierRule(
            "minimalist",
            "A simple trio! (Elegant restraint)",
            1.5,
            bouquet -> bouquet.getFlowerCount() == 3
        ));
        
        // Contains Orchid = 6x
        rules.add(new MultiplierRule(
            "has_orchid",
            "Contains a rare orchid! (Exotic treasure)",
            6.0,
            bouquet -> {
                for (Flower flower : bouquet.getFlowers()) {
                    if (flower.getName().toLowerCase().contains("orchid")) {
                        return true;
                    }
                }
                return false;
            }
        ));
        
        // Contains Lotus = 7x
        rules.add(new MultiplierRule(
            "has_lotus",
            "Contains a sacred lotus! (Divine beauty)",
            7.0,
            bouquet -> {
                for (Flower flower : bouquet.getFlowers()) {
                    if (flower.getName().toLowerCase().contains("lotus")) {
                        return true;
                    }
                }
                return false;
            }
        ));
        
        return rules;
    }
}
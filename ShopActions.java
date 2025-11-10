/* ShopActions.java
 * Handles all shop functionality including buying and selling
 * Created to modularize sunflowerSimulator.java
 * * UPDATED: Added comprehensive selling system
 * - Seeds: 50% of shop value
 * - Empty flower pots: 5 credits
 * - Flower pots with seed/seedling: Full pot value (10 credits) + plant value
 * - Bloomed flowers: 110% of seed value (+10 if in pot)
 * - Matured flowers: 125% of seed value (+10 if in pot)
 * - Mutated flowers: 500% of seed value (+10 if in pot)
 * - Bouquet auctions: Coming soon!
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class ShopActions {
    
    private static final Random random = new Random();
    
    /**
     * Main shop interface - handles both buying and selling
     * @param player The player
     * @param scanner Scanner for user input
     */
    public static void handleShop(Player1 player, Scanner scanner) {
        boolean inShop = true;

        while (inShop) {
            System.out.println("\n🏪 Welcome to the Garden Shop! 🏪");
            System.out.println("Current resources: " + player.getNRG() + " NRG | " + player.getCredits() + " credits");
            System.out.println();
            System.out.println("What would you like to do?");
            System.out.println("1: Buy Seeds");
            System.out.println("2: Sell Items");
            System.out.println("3: Auction Bouquets");
            System.out.println("4: Return to Main Menu");
            
            // REMOVED: System.out.print("\nChoice: ");
            System.out.print("\nChoice: "); // Changed to be consistent with main menu's "\nEnter your choice: " equivalent
            String shopChoice = scanner.nextLine();

            switch (shopChoice) {
                case "1":
                    handleBuying(player, scanner);
                    break;
                    
                case "2":
                    handleSelling(player, scanner);
                    break;
                    
                case "3":
                    handleBouquetAuction(player, scanner);
                    break;
                    
                case "4":
                    inShop = false;
                    break;
                    
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    // ========================================
    // BUYING SYSTEM
    // ========================================
    
    /**
     * Handles the seed buying workflow
     */
    private static void handleBuying(Player1 player, Scanner scanner) {
        // Generate shop inventory based on player progression
        List<String> shopInventory = generateShopInventory(player);

        if (shopInventory.isEmpty()) {
            System.out.println("The shop is currently out of stock! Come back later.");
            return;
        }

        // Display shop menu
        displayShopMenu(shopInventory);

        // Get player choice (Robust input loop added)
        int seedChoice = -1;
        boolean validChoice = false;

        while (!validChoice) {
            System.out.print("\nWhich seed would you like to buy? (1-" + shopInventory.size() + 
                    ", or 0 to cancel): ");
            try {
                seedChoice = Integer.parseInt(scanner.nextLine());
                if (seedChoice >= 0 && seedChoice <= shopInventory.size()) {
                    validChoice = true;
                } else {
                    System.out.println("Invalid choice. Please select a valid number.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }

        if (seedChoice == 0) {
            System.out.println("Purchase cancelled.");
            return;
        }

        // Process the purchase
        String selectedFlower = shopInventory.get(seedChoice - 1);
        processPurchase(player, selectedFlower);
    }
    
    /**
     * Generates shop inventory based on player progression
     */
    private static List<String> generateShopInventory(Player1 player) {
        List<String> inventory = new ArrayList<>();
        int playerDay = player.getDay();

        // Determine difficulty tiers available based on player day
        int maxDifficulty;
        if (playerDay < 5) {
            maxDifficulty = 1; // Only 1★ flowers
        } else if (playerDay < 15) {
            maxDifficulty = 2; // Up to 2★ flowers
        } else if (playerDay < 30) {
            maxDifficulty = 3; // Up to 3★ flowers
        } else if (playerDay < 50) {
            maxDifficulty = 4; // Up to 4★ flowers
        } else {
            maxDifficulty = 5; // All flowers available
        }

        // Generate inventory with variety
        Set<String> selectedFlowers = new HashSet<>();

        // Always include at least one 1★ flower
        String easyFlower = FlowerRegistry.getRandomFlowerByDifficulty(1, 1);
        if (easyFlower != null) {
            selectedFlowers.add(easyFlower);
        }

        // Add 3-5 more random flowers from available tiers
        int inventorySize = 4 + random.nextInt(2); // 4-5 flowers total
        while (selectedFlowers.size() < inventorySize) {
            String randomFlower = FlowerRegistry.getRandomFlowerByDifficulty(1, maxDifficulty);
            if (randomFlower != null) {
                selectedFlowers.add(randomFlower);
            }
        }

        inventory.addAll(selectedFlowers);
        return inventory;
    }
    
    /**
     * Displays the shop menu with available seeds
     */
    private static void displayShopMenu(List<String> shopInventory) {
        System.out.println("\n🌸 Available Seeds 🌸");
        for (int i = 0; i < shopInventory.size(); i++) {
            String flowerName = shopInventory.get(i);
            double seedCost = FlowerRegistry.getSeedCost(flowerName);
            int difficulty = FlowerRegistry.getFlowerDifficulty(flowerName);

            // Create difficulty stars
            StringBuilder stars = new StringBuilder();
            for (int j = 0; j < difficulty; j++) {
                stars.append("★");
            }
            for (int j = difficulty; j < 5; j++) {
                stars.append("☆");
            }

            System.out.println((i + 1) + ": " + flowerName + " " + stars + " - " + 
                    (int)seedCost + " credits");
        }
    }
    
    /**
     * Processes a seed purchase
     */
    private static void processPurchase(Player1 player, String flowerName) {
        double seedCost = FlowerRegistry.getSeedCost(flowerName);

        if (player.getCredits() < seedCost) {
            System.out.println("\nYou don't have enough credits! Need " + (int)seedCost + 
                    ", have " + player.getCredits());
            return;
        }

        // Create the seed and add to inventory
        Flower newSeed = FlowerRegistry.createSeed(flowerName);
        if (newSeed != null) {
            player.addToInventory(newSeed);
            player.setCredits(player.getCredits() - (int)seedCost);

            System.out.println("\n✅ You purchased a " + flowerName + " seed for " + 
                    (int)seedCost + " credits!");
            System.out.println("Remaining credits: " + player.getCredits());

            Journal.addJournalEntry(player, "Purchased a " + flowerName + " seed from the shop.");
        } else {
            System.out.println("\nError: Could not create seed. Please try again.");
        }
    }
    
    // ========================================
    // SELLING SYSTEM
    // ========================================
    
    /**
     * Handles the selling workflow
     */
    private static void handleSelling(Player1 player, Scanner scanner) {
        System.out.println("\n💰 Sell Items 💰");
        System.out.println("What would you like to sell?");
        
        // Get sellable items from inventory
        List<SellableItem> sellableItems = getSellableItems(player);
        
        if (sellableItems.isEmpty()) {
            System.out.println("\nYou don't have anything to sell!");
            System.out.println("Harvest some flowers or craft items to sell them here.");
            return;
        }
        
        // Display sellable items
        displaySellableItems(sellableItems);
        
        // Get player choice (Robust input loop added)
        int sellChoice = -1;
        boolean validChoice = false;

        while (!validChoice) {
            System.out.print("\nWhich item would you like to sell? (1-" + sellableItems.size() + 
                    ", or 0 to cancel): ");
            try {
                sellChoice = Integer.parseInt(scanner.nextLine());
                if (sellChoice >= 0 && sellChoice <= sellableItems.size()) {
                    validChoice = true;
                } else {
                    System.out.println("Invalid choice. Please select a valid item number.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        
        if (sellChoice == 0) {
            System.out.println("Sale cancelled.");
            return;
        }
        
        // Process the sale
        SellableItem selectedItem = sellableItems.get(sellChoice - 1);
        processSale(player, selectedItem, scanner);
    }
    
    /**
     * Helper class to store sellable item information
     */
    private static class SellableItem {
        Object item;
        int inventoryIndex;
        String displayName;
        int sellValue;
        String description;
        
        SellableItem(Object item, int inventoryIndex, String displayName, int sellValue, String description) {
            this.item = item;
            this.inventoryIndex = inventoryIndex;
            this.displayName = displayName;
            this.sellValue = sellValue;
            this.description = description;
        }
    }
    
    /**
     * Gets all sellable items from player's inventory
     */
    private static List<SellableItem> getSellableItems(Player1 player) {
        List<SellableItem> sellableItems = new ArrayList<>();
        ArrayList<Object> inventory = player.getInventory();
        
        for (int i = 0; i < inventory.size(); i++) {
            Object item = inventory.get(i);
            
            if (item instanceof Flower) {
                Flower flower = (Flower) item;
                SellableItem sellable = calculateFlowerValue(flower, i);
                if (sellable != null) {
                    sellableItems.add(sellable);
                }
            } else if (item instanceof gardenPlot) {
                gardenPlot plot = (gardenPlot) item;
                if (plot.isFlowerPot()) {
                    SellableItem sellable = calculateFlowerPotValue(plot, i);
                    if (sellable != null) {
                        sellableItems.add(sellable);
                    }
                }
            }
        }
        
        return sellableItems;
    }
    
    /**
     * Calculates the sell value of a flower based on its stage
     */
    private static SellableItem calculateFlowerValue(Flower flower, int inventoryIndex) {
        String flowerName = flower.getName();
        String growthStage = flower.getGrowthStage();
        double seedCost = FlowerRegistry.getSeedCost(flowerName);
        
        if (seedCost < 0) {
            return null; // Unknown flower
        }
        
        int sellValue;
        String description;
        
        switch (growthStage) {
            case "Seed":
                // Seeds sell for 50% of shop value
                sellValue = (int)(seedCost * 0.5);
                description = "Unused seed (50% of shop value)";
                break;
                
            case "Seedling":
                // Seedlings sell for 90% of seed value
                sellValue = (int)(seedCost * 0.9);
                description = "Young plant (90% of seed value)";
                break;
                
            case "Bloomed":
                // Bloomed flowers sell for 110% of seed value
                sellValue = (int)(seedCost * 1.1);
                description = "Beautiful bloomed flower (110% of seed value)";
                break;
                
            case "Matured":
                // Matured flowers sell for 125% of seed value
                sellValue = (int)(seedCost * 1.25);
                description = "Fully matured flower (125% of seed value)";
                break;
                
            case "Withered":
                // Withered flowers sell for minimal value
                sellValue = (int)(seedCost * 0.1);
                description = "Withered plant (10% of seed value)";
                break;
                
            case "Mutated":
                // Mutated flowers sell for 500% of seed value!
                sellValue = (int)(seedCost * 5.0);
                description = "✨ RARE MUTATED FLOWER! ✨ (500% of seed value!)";
                break;
                
            default:
                return null;
        }
        
        String displayName = flowerName + " (" + growthStage + ")";
        return new SellableItem(flower, inventoryIndex, displayName, sellValue, description);
    }
    
    /**
     * Calculates the sell value of a flower pot
     */
    private static SellableItem calculateFlowerPotValue(gardenPlot plot, int inventoryIndex) {
        if (!plot.isOccupied()) {
            // Empty flower pot: 5 credits
            return new SellableItem(plot, inventoryIndex, 
                    "Empty Flower Pot 🪴", 5, 
                    "Unused flower pot (25% of craft cost)");
        }
        
        // Flower pot with plant
        Flower flower = plot.getPlantedFlower();
        String flowerName = flower.getName();
        String growthStage = flower.getGrowthStage();
        double seedCost = FlowerRegistry.getSeedCost(flowerName);
        
        if (seedCost < 0) {
            return null;
        }
        
        int plantValue;
        String stageDescription;
        
        if (growthStage.equals("Seed")) {
            // Seed in pot: Full pot value (10) + 50% seed value
            plantValue = (int)(seedCost * 0.5);
            stageDescription = "seed";
        } else if (growthStage.equals("Seedling")) {
            // Seedling in pot: Full pot value (10) + 90% seed value
            plantValue = (int)(seedCost * 0.9);
            stageDescription = "seedling";
        } else if (growthStage.equals("Bloomed")) {
            // Bloomed in pot: 110% seed value + 10 bonus
            plantValue = (int)(seedCost * 1.1);
            stageDescription = "bloomed flower";
        } else if (growthStage.equals("Matured")) {
            // Matured in pot: 125% seed value + 10 bonus
            plantValue = (int)(seedCost * 1.25);
            stageDescription = "matured flower";
        } else if (growthStage.equals("Mutated")) {
            // Mutated in pot: 500% seed value + 10 bonus
            plantValue = (int)(seedCost * 5.0);
            stageDescription = "✨ MUTATED FLOWER ✨";
        } else {
            // Withered
            plantValue = (int)(seedCost * 0.1);
            stageDescription = "withered plant";
        }
        
        // Add pot bonus
        int totalValue = plantValue + 10;
        
        String displayName = "🪴 Flower Pot with " + flowerName + " (" + growthStage + ")";
        String description = "Pot with " + stageDescription + " (+10 bonus for pot)";
        
        return new SellableItem(plot, inventoryIndex, displayName, totalValue, description);
    }
    
    /**
     * Displays all sellable items with their values
     */
    private static void displaySellableItems(List<SellableItem> sellableItems) {
        System.out.println("\n📦 Your Sellable Items:");
        for (int i = 0; i < sellableItems.size(); i++) {
            SellableItem item = sellableItems.get(i);
            System.out.println((i + 1) + ": " + item.displayName + " - " + item.sellValue + " credits");
            System.out.println("   " + item.description);
        }
    }
    
    /**
     * Processes the sale of an item
     */
    private static void processSale(Player1 player, SellableItem selectedItem, Scanner scanner) {
        System.out.println("\n💰 Sale Confirmation");
        System.out.println("Item: " + selectedItem.displayName);
        System.out.println("Sale price: " + selectedItem.sellValue + " credits");
        System.out.println(selectedItem.description);
        
        System.out.print("\nAre you sure you want to sell this? (yes/no): ");
        String confirm = scanner.nextLine().toLowerCase();
        
        if (!confirm.equals("yes")) {
            System.out.println("Sale cancelled.");
            return;
        }
        
        // Remove item from inventory
        player.removeFromInventory(selectedItem.item);
        
        // Add credits
        player.setCredits(player.getCredits() + selectedItem.sellValue);
        
        System.out.println("\n✅ Sold " + selectedItem.displayName + " for " + 
                selectedItem.sellValue + " credits!");
        System.out.println("New balance: " + player.getCredits() + " credits");
        
        // Add journal entry
        String journalEntry = "Sold " + selectedItem.displayName + " for " + 
                selectedItem.sellValue + " credits.";
        Journal.addJournalEntry(player, journalEntry);
        Journal.saveGame(player);
    }
    
    // ========================================
    // BOUQUET AUCTION SYSTEM (Coming Soon)
    // ========================================
    
    /**
     * Handles bouquet auction workflow
     */
    private static void handleBouquetAuction(Player1 player, Scanner scanner) {
        System.out.println("\n🎨 Bouquet Auction 🎨");
        System.out.println();
        System.out.println("You don't have any bouquets!");
        System.out.println("Consult the journal and backpack to get those creative juices flowing! ^-^");
        System.out.println();
        System.out.println("💡 Hint: Bouquets are special arrangements you can create");
        System.out.println("from your harvested flowers. They sell for much more than");
        System.out.println("individual blooms!");
        System.out.println();
        System.out.println("Press Enter to return...");
        scanner.nextLine();
    }
}
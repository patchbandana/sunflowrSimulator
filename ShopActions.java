/* ShopActions.java
 * Handles all shop-related actions including seed purchasing
 * Created to modularize sunflowerSimulator.java
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class ShopActions {
    
    /**
     * Handles the complete shop interface workflow
     * @param player The player shopping
     * @param scanner Scanner for user input
     */
    public static void handleShop(Player1 player, Scanner scanner) {
        boolean inShop = true;

        // Determine difficulty tier based on player progress
        int shopDifficultyMin = 1;
        int shopDifficultyMax = 2;

        if (player.getDay() > 30) {
            shopDifficultyMin = 3;
            shopDifficultyMax = 5;
        } else if (player.getDay() > 15) {
            shopDifficultyMin = 2;
            shopDifficultyMax = 4;
        } else if (player.getDay() > 7) {
            shopDifficultyMin = 1;
            shopDifficultyMax = 3;
        }

        // Generate random shop inventory (4 different flowers)
        List<String> shopInventory = generateShopInventory(shopDifficultyMin, shopDifficultyMax);

        while (inShop) {
            displayShopMenu(player, shopInventory);

            System.out.print("\nPick a seed to buy (1-5): ");
            String shopChoice = scanner.next();
            scanner.nextLine(); // Clear buffer

            // Handle seed buying or exit
            if (shopChoice.equals("5")) {
                inShop = false;
                System.out.println("Thank you for visiting the shop!");
            } else {
                processPurchase(player, shopInventory, shopChoice);
            }
        }
    }
    
    /**
     * Generates random shop inventory based on difficulty tier
     * @param minDifficulty Minimum flower difficulty
     * @param maxDifficulty Maximum flower difficulty
     * @return List of 4 unique flower names
     */
    private static List<String> generateShopInventory(int minDifficulty, int maxDifficulty) {
        List<String> shopInventory = new ArrayList<>();
        Set<String> usedFlowers = new HashSet<>();
        Random shopRand = new Random();

        while (shopInventory.size() < 4) {
            String randomFlower = FlowerRegistry.getRandomFlowerByDifficulty(minDifficulty, maxDifficulty);
            if (randomFlower != null && !usedFlowers.contains(randomFlower)) {
                shopInventory.add(randomFlower);
                usedFlowers.add(randomFlower);
            }
        }
        
        return shopInventory;
    }
    
    /**
     * Displays the shop menu with available seeds
     * @param player The player shopping
     * @param shopInventory List of available flowers
     */
    private static void displayShopMenu(Player1 player, List<String> shopInventory) {
        System.out.println("\n🌼 Welcome to the Flower Shop! 🌼");
        System.out.println("You have " + player.getCredits() + " credits.");
        System.out.println("Here are today's seeds for sale:");
        System.out.println();

        // Display shop inventory
        for (int i = 0; i < shopInventory.size(); i++) {
            String flowerName = shopInventory.get(i);
            double cost = FlowerRegistry.getSeedCost(flowerName);
            int difficulty = FlowerRegistry.getFlowerDifficulty(flowerName);

            // Build difficulty stars
            StringBuilder stars = new StringBuilder();
            for (int j = 0; j < difficulty; j++) {
                stars.append("★");
            }
            for (int j = difficulty; j < 5; j++) {
                stars.append("☆");
            }

            System.out.println((i + 1) + ". " + flowerName + " Seed - " + 
                    (int)cost + " credits " + stars);
        }
        System.out.println("5. Leave Shop");
    }
    
    /**
     * Processes a seed purchase
     * @param player The player making the purchase
     * @param shopInventory List of available flowers
     * @param shopChoice The player's choice (as string)
     */
    private static void processPurchase(Player1 player, List<String> shopInventory, String shopChoice) {
        try {
            int choice = Integer.parseInt(shopChoice);
            
            if (choice >= 1 && choice <= 4) {
                String selectedFlower = shopInventory.get(choice - 1);
                double cost = FlowerRegistry.getSeedCost(selectedFlower);

                if (player.getCredits() >= cost) {
                    // Player can afford the seed
                    Flower seed = FlowerRegistry.createSeed(selectedFlower);
                    
                    if (seed != null) {
                        player.addToInventory(seed);
                        player.setCredits((int)(player.getCredits() - cost));
                        
                        System.out.println("✅ You bought a " + selectedFlower + " seed!");
                        Journal.addJournalEntry(player, "Purchased a " + selectedFlower + " seed from the shop.");
                        Journal.saveGame(player);
                    } else {
                        System.out.println("❌ Error creating seed. Please try again.");
                    }
                } else {
                    // Not enough credits
                    System.out.println("❌ You don't have enough credits!");
                    System.out.println("You need " + (int)cost + " credits but only have " + 
                            player.getCredits() + " credits.");
                }
            } else {
                System.out.println("Please enter a valid choice (1-5).");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number (1-5).");
        }
    }
}
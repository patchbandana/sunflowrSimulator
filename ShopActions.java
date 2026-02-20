/* ShopActions.java
 * Handles shop, selling, and auction house interactions
 * Updated: November 24, 2025 - SIMPLIFIED seed buying (removed cart system)
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class ShopActions {

	private static final Random random = new Random();
    
    private static List<String> currentSeedSelection = new ArrayList<>();
    private static boolean isInventoryStale = true;

	/**
	 * Main shop interface
	 */
	public static void handleShop(Player1 player, Scanner scanner) {
		boolean inShop = true;

		while (inShop) {
			System.out.println("\n🪴 Welcome to the Garden Shop! 🪴");
			System.out.println("Current resources: " + player.getNRG() + " NRG | " + player.getCredits() + " credits");
			System.out.println();
			System.out.println("What would you like to do?");
			System.out.println("1: Buy Seeds");
			System.out.println("2: Sell Items");
			System.out.println("3: Auction House");
			System.out.println("4: Return to Main Menu");

			System.out.print("\nChoice: ");
			String shopChoice = scanner.nextLine();

			switch (shopChoice) {
			case "1":
				handleBuySeeds(player, scanner);
				break;

			case "2":
				handleSelling(player, scanner);
				break;

			case "3":
				AuctionActions.handleAuctionHouse(player, scanner);
				break;

			case "4":
				inShop = false;
				break;

			default:
				System.out.println("Invalid choice. Please try again.");
			}
		}
	}
    
    /**
     * Resets the shop's inventory for the new day
     */
    public static void resetShopInventory() {
        isInventoryStale = true;
    }
    
    /**
     * Generates a new random inventory list if needed
     */
    private static void ensureShopInventoryIsGenerated(Player1 player) {
        if (!isInventoryStale && !currentSeedSelection.isEmpty()) {
            return;
        }
        
        int numSeeds = 4 + random.nextInt(2);
        currentSeedSelection = FlowerRegistry.getRandomShopSelection(numSeeds, 5);
        isInventoryStale = false;
    }

    /**
     * Handles seed purchasing - simplified one-at-a-time system
     * UPDATED: Removed cart system, purchases happen immediately like selling
     */
	private static void handleBuySeeds(Player1 player, Scanner scanner) {
		boolean buyingSeeds = true;
        
        ensureShopInventoryIsGenerated(player);
        
        if (currentSeedSelection.isEmpty()) {
            System.out.println("The shop is currently out of stock! Come back later.");
            return;
        }
		
		while (buyingSeeds) {
			System.out.println("\n--- SEED SHOP ---");
			System.out.println("Credits: " + (int)player.getCredits());
			System.out.println();
			
			// Display available seeds
			for (int i = 0; i < currentSeedSelection.size(); i++) {
				String flowerName = currentSeedSelection.get(i);
				double cost = FlowerRegistry.getSeedCost(flowerName);
                int difficulty = FlowerRegistry.getFlowerDifficulty(flowerName);
                
                // Build difficulty stars
                StringBuilder stars = new StringBuilder();
                for (int j = 0; j < difficulty; j++) {
                    stars.append("â˜…");
                }
                for (int j = difficulty; j < 5; j++) {
                    stars.append("â˜†");
                }

				System.out.printf("%d: %s Seed %s - %d credits\n", 
					i + 1, flowerName, stars, (int)cost);
			}
			
			System.out.println("0: Back to Shop Menu");

			System.out.print("\nWhich seed would you like to buy? (1-" + currentSeedSelection.size() + ", 0 to exit): ");
			
			int choice;
			try {
				String input = scanner.nextLine().trim();

				if (input.isEmpty()) {
					System.out.println("Please enter a number or '0' to return.");
					continue;
				}

				choice = Integer.parseInt(input);
			} catch (NumberFormatException e) {
				System.out.println("Invalid input. Please enter a number.");
				continue;
			}

			if (choice == 0) {
				buyingSeeds = false;
				System.out.println("Returning to shop menu.");
			
			} else if (choice >= 1 && choice <= currentSeedSelection.size()) {
				String flowerName = currentSeedSelection.get(choice - 1);
				double seedCost = FlowerRegistry.getSeedCost(flowerName);
                
                // Ask how many
                System.out.print("\nHow many " + flowerName + " seeds would you like to buy? (Cost: " + (int)seedCost + " each, or 0 to cancel): ");
				
                int quantity;
				try {
					String quantityInput = scanner.nextLine().trim();
                    if (quantityInput.isEmpty()) {
                        System.out.println("Purchase cancelled.");
                        continue;
                    }
					quantity = Integer.parseInt(quantityInput);
				} catch (NumberFormatException e) {
					System.out.println("Invalid quantity input. Purchase cancelled.");
					continue;
				}
				
				if (quantity <= 0) {
					System.out.println("Purchase cancelled.");
					continue;
				}
				
				double totalCost = seedCost * quantity;
				
				// Check if player can afford it
				if (player.getCredits() < totalCost) {
					System.out.println("\nâŒ You don't have enough credits! Need " + (int)totalCost + ", have " + (int)player.getCredits());
					continue;
				}
				
				// Confirm purchase
				System.out.print("\nBuy " + quantity + "x " + flowerName + " for " + (int)totalCost + " credits? (yes/no): ");
				String confirm = scanner.nextLine().toLowerCase();
				
				if (!confirm.equals("yes")) {
					System.out.println("Purchase cancelled.");
					continue;
				}
				
				// Execute purchase
				player.setCredits(player.getCredits() - (int)totalCost);
				
				for (int i = 0; i < quantity; i++) {
					Flower seed = FlowerRegistry.createSeed(flowerName);
					if (seed != null) {
						player.addToInventory(seed);
					}
				}
				
				System.out.println("\nâœ… Purchase successful! Bought " + quantity + "x " + flowerName + " seed(s).");
				System.out.println("Credits remaining: " + (int)player.getCredits());
				
				// Journal entry
				String journalEntry = "Purchased " + quantity + "x " + flowerName + " seed(s) for " + (int)totalCost + " credits.";
				Journal.addJournalEntry(player, journalEntry);
				Journal.saveGame(player);
				
			} else {
				System.out.println("Invalid selection! Please enter a number between 1 and " + currentSeedSelection.size() + ".");
			}
		}
	}

	/**
	 * Handles selling items from the player's inventory
	 */
	private static void handleSelling(Player1 player, Scanner scanner) {
		ArrayList<Object> inventory = player.getInventory();
		
		if (inventory.isEmpty()) {
			System.out.println("\nYour backpack is empty! Nothing to sell.");
			System.out.println("Press Enter to return to shop menu...");
			scanner.nextLine();
			return;
		}
		
		boolean selling = true;
		
		while (selling) {
			System.out.println("\n--- SELLING ITEMS ---");
			System.out.println("Current credits: " + (int)player.getCredits());
			System.out.println("\nYour Items:");
			
			List<Integer> sellableIndices = new ArrayList<>();
			int displayIndex = 1;
			
			for (int i = 0; i < inventory.size(); i++) {
				Object item = inventory.get(i);
				int sellPrice = calculateSellPrice(item);
				
				if (sellPrice > 0) {
					sellableIndices.add(i);
					System.out.println(displayIndex + ": " + getItemDescription(item) + 
							" - Sell for " + sellPrice + " credits");
					displayIndex++;
				}
			}
			
			if (sellableIndices.isEmpty()) {
				System.out.println("You don't have any sellable items!");
				System.out.println("Press Enter to return to shop menu...");
				scanner.nextLine();
				return;
			}
			
			System.out.println("0: Back to Shop Menu");
			System.out.print("\nWhich item would you like to sell? (1-" + sellableIndices.size() + ", 0 to exit): ");
			
			int choice;
			try {
				String input = scanner.nextLine().trim();
				if (input.isEmpty()) {
					System.out.println("Please enter a number.");
					continue;
				}
				choice = Integer.parseInt(input);
			} catch (NumberFormatException e) {
				System.out.println("Invalid input. Please enter a number.");
				continue;
			}
			
			if (choice == 0) {
				selling = false;
				System.out.println("Returning to shop menu.");
			} else if (choice >= 1 && choice <= sellableIndices.size()) {
				int inventoryIndex = sellableIndices.get(choice - 1);
				Object itemToSell = inventory.get(inventoryIndex);
				int sellPrice = calculateSellPrice(itemToSell);
				
				System.out.print("\nSell " + getItemDescription(itemToSell) + " for " + 
						sellPrice + " credits? (yes/no): ");
				String confirm = scanner.nextLine().toLowerCase();
				
				if (confirm.equals("yes")) {
					inventory.remove(inventoryIndex);
					player.setCredits(player.getCredits() + sellPrice);
					
					System.out.println("âœ… Sold! You now have " + (int)player.getCredits() + " credits.");
					
					Journal.addJournalEntry(player, "Sold " + getItemDescription(itemToSell) + 
							" for " + sellPrice + " credits.");
					Journal.saveGame(player);
				} else {
					System.out.println("Sale cancelled.");
				}
			} else {
				System.out.println("Invalid choice. Please select a valid item number.");
			}
		}
	}
	
	/**
	 * Calculates the sell price for an item
	 */
	private static int calculateSellPrice(Object item) {
		if (item instanceof Bouquet) {
			return 0; // Bouquets cannot be sold directly, only auctioned
		}
		
		if (item instanceof gardenPlot) {
			gardenPlot plot = (gardenPlot) item;
			
			if (plot.isFlowerPot() && !plot.isOccupied()) {
				return 10;
			}
			
			if (plot.isFlowerPot() && plot.isOccupied()) {
				Flower plant = plot.getPlantedFlower();
				int basePrice = calculateFlowerPrice(plant);
				
				if (plant.getGrowthStage().equals("Withered")) {
					return 1 + 10;
				}
				
				return basePrice + 20;
			}
		}
		
		if (item instanceof Flower) {
			Flower flower = (Flower) item;
			return calculateFlowerPrice(flower);
		}
		
		return 0;
	}
	
	/**
	 * Calculates the sell price for a flower based on growth stage
	 */
	private static int calculateFlowerPrice(Flower flower) {
		String stage = flower.getGrowthStage();
		String name = flower.getName();
		
		switch (stage) {
			case "Seed":
				double seedCost = FlowerRegistry.getSeedCost(name);
				return (int)(seedCost * 0.5);
				
			case "Seedling":
				double seedlingValue = FlowerRegistry.getFlowerValue(name, "Seedling");
				return (int)(seedlingValue * 0.9);
				
			case "Bloomed":
				double bloomedValue = FlowerRegistry.getFlowerValue(name, "Bloomed");
				return (int)(bloomedValue * 1.1);
				
			case "Matured":
				double maturedValue = FlowerRegistry.getFlowerValue(name, "Matured");
				return (int)(maturedValue * 1.5);
				
			case "Withered":
				return 1;
				
			case "Mutated":
				double mutatedValue = FlowerRegistry.getFlowerValue(name, "Mutated");
				return (int)(mutatedValue * 5.0);
				
			default:
				return 0;
		}
	}
	
	/**
	 * Gets a descriptive string for an inventory item
	 */
	private static String getItemDescription(Object item) {
		if (item instanceof Bouquet) {
			Bouquet bouquet = (Bouquet) item;
			return "ðŸ’ " + bouquet.getDisplayName();
		}
		
		if (item instanceof gardenPlot) {
			gardenPlot plot = (gardenPlot) item;
			if (plot.isFlowerPot() && !plot.isOccupied()) {
				return "🪴 Empty Flower Pot";
			}
			if (plot.isFlowerPot() && plot.isOccupied()) {
				Flower plant = plot.getPlantedFlower();
				return "🪴 Flower Pot with " + plant.getName() + " (" + plant.getGrowthStage() + ")";
			}
		}
		
		if (item instanceof Flower) {
			Flower flower = (Flower) item;
			String emoji = getFlowerEmoji(flower.getGrowthStage());
			return emoji + " " + flower.getName() + " (" + flower.getGrowthStage() + ")";
		}
		
		return item.toString();
	}
	
	/**
	 * Gets an appropriate emoji for a flower's growth stage
	 */
	private static String getFlowerEmoji(String stage) {
		switch (stage) {
			case "Seed": return "ðŸŒ±";
			case "Seedling": return "ðŸŒ¿";
			case "Bloomed": return "ðŸŒ¸";
			case "Matured": return "ðŸŒ»";
			case "Withered": return "ðŸ¥€";
			case "Mutated": return "✨";
			default: return "ðŸŒ¼";
		}
	}
}
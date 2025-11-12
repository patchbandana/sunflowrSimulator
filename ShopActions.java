import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class ShopActions {

	private static final Random random = new Random();
    
    // Static inventory list that persists across shop visits
    private static List<String> currentSeedSelection = new ArrayList<>();
    
    // Flag to track if the inventory needs to be regenerated (should be reset daily)
    private static boolean isInventoryStale = true;

    /**
     * Internal class to manage purchase data temporarily in the cart
     */
    private static class PurchaseItem {
        String name;
        int quantity;
        double totalCost;

        PurchaseItem(String name, int quantity, double totalCost) {
            this.name = name;
            this.quantity = quantity;
            this.totalCost = totalCost;
        }
    }

	/**
	 * Main shop interface - handles both buying and selling
	 * @param player The player
	 * @param scanner Scanner for user input
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
			System.out.println("3: Auction Bouquets");
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
    
    /**
     * External method that must be called from sunflowerSimulator.advanceDay()
     * to reset the shop's inventory for the new day.
     */
    public static void resetShopInventory() {
        isInventoryStale = true;
    }
    
    /**
     * Generates a new random inventory list if needed.
     */
    private static void ensureShopInventoryIsGenerated(Player1 player) {
        if (!isInventoryStale && !currentSeedSelection.isEmpty()) {
            return; // Inventory is fresh, no need to regenerate
        }
        
        // Target size is 4 or 5 options
        int numSeeds = 4 + random.nextInt(2); 

        // Always use max difficulty 5 so all seeds can be pulled,
        // letting the weighting inside FlowerRegistry favor lower difficulties.
        currentSeedSelection = FlowerRegistry.getRandomShopSelection(numSeeds, 5);
        isInventoryStale = false;
    }

    /**
     * Handles the dynamic, cart-based seed purchase menu.
     */
	private static void handleBuySeeds(Player1 player, Scanner scanner) {
		boolean buyingSeeds = true;
        
        ensureShopInventoryIsGenerated(player);
        
        if (currentSeedSelection.isEmpty()) {
            System.out.println("The shop is currently out of stock! Come back later.");
            return;
        }
        
		// Temporary list to hold the items the user selects for purchase
		List<PurchaseItem> cart = new ArrayList<>();
		
		while (buyingSeeds) {
			System.out.println("\n--- SEED SHOP ---");
			System.out.println("Credits: " + (int)player.getCredits());
            
            // Calculate total cart cost
            double currentTotalCost = cart.stream().mapToDouble(item -> item.totalCost).sum();
			
			// 1. Display the current selection (simplified menu)
			for (int i = 0; i < currentSeedSelection.size(); i++) {
				String flowerName = currentSeedSelection.get(i);
				double cost = FlowerRegistry.getSeedCost(flowerName);
                int difficulty = FlowerRegistry.getFlowerDifficulty(flowerName);
                
                StringBuilder stars = new StringBuilder();
                for (int j = 0; j < difficulty; j++) {
                    stars.append("★");
                }
                for (int j = difficulty; j < 5; j++) {
                    stars.append("☆");
                }

                // Simplified Display: Name, Growth Stage, Cost, Difficulty (stars)
				System.out.printf("%d: Buy %s Seed %s - %d credits\n", 
					i + 1, flowerName, stars, (int)cost);
			}
			
			// 2. Display Cart contents (subtle, only if cart has items)
            if (!cart.isEmpty()) {
                System.out.print("   Cart: ");
                for (PurchaseItem item : cart) {
                    System.out.printf("%dx %s (%.0f), ", item.quantity, item.name, item.totalCost);
                }
                System.out.println();
            }
            
            // 3. Add the final menu option for purchasing (N+1 option)
			System.out.printf("%d: Purchase Cart (%.0f total credits)\n", 
				currentSeedSelection.size() + 1, currentTotalCost);
			System.out.println("0: Back to Shop Menu");

			System.out.print("\nChoice (1-" + (currentSeedSelection.size() + 1) + ", 0 to exit): ");
			
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
                System.out.println("Leaving the seed shop. Cart contents discarded.");
			
            // 4. Handle Purchase Cart option (N+1)
			} else if (choice == currentSeedSelection.size() + 1) {
				if (cart.isEmpty()) {
					System.out.println("Your cart is empty. Select seeds to add them first.");
				} else {
					// Finalize Purchase: deduct credits, add to inventory, log to journal, save game.
					finalizePurchase(player, cart);
					cart.clear(); // Reset the cart after purchase
				}
			
            // 5. Handle adding a seed to the cart (1 to N options)
			} else if (choice >= 1 && choice <= currentSeedSelection.size()) {
				String flowerName = currentSeedSelection.get(choice - 1);
				double seedCost = FlowerRegistry.getSeedCost(flowerName);
                
                System.out.print("How many " + flowerName + " seeds would you like to add to the cart (Cost: " + (int)seedCost + " each)? ");
				
                int quantity;
				try {
					String quantityInput = scanner.nextLine().trim();
                    if (quantityInput.isEmpty()) {
                        System.out.println("Invalid quantity. Aborting purchase.");
                        continue;
                    }
					quantity = Integer.parseInt(quantityInput);
				} catch (NumberFormatException e) {
					System.out.println("Invalid quantity input. Aborting purchase.");
					continue;
				}
				
				if (quantity <= 0) {
					System.out.println("Addition to cart cancelled.");
					continue;
				}
				
				double totalCost = seedCost * quantity;
				
				// Check for existing item in cart and update, or add new item
				boolean found = false;
				for (PurchaseItem item : cart) {
					if (item.name.equals(flowerName)) {
						item.quantity += quantity;
						item.totalCost += totalCost;
						found = true;
						break;
					}
				}
				if (!found) {
					cart.add(new PurchaseItem(flowerName, quantity, totalCost));
				}
				
				System.out.printf("%d x %s added to cart (Current cart total: %.0f).\n", quantity, flowerName, cart.stream().mapToDouble(item -> item.totalCost).sum());
				
			} else {
				System.out.println("Invalid selection! Please enter a number between 1 and " + (currentSeedSelection.size() + 1) + ".");
			}
		}
	}
    
	/**
	 * Finalizes the purchase of items in the cart.
	 * Updates player's credits, inventory, journal, and saves the game.
	 * @param player The player instance.
	 * @param cart The list of items to purchase.
	 */
	private static void finalizePurchase(Player1 player, List<PurchaseItem> cart) {
		double grandTotal = cart.stream().mapToDouble(item -> item.totalCost).sum();

		if (player.getCredits() < grandTotal) {
			System.out.println("\n❌ You don't have enough credits! Need " + (int)grandTotal + ", have " + (int)player.getCredits());
			return;
		}

		// Process transaction
		player.setCredits(player.getCredits() - (int)grandTotal);
		StringBuilder journalEntry = new StringBuilder("Purchased seeds: ");
		
		System.out.println("\n✅ Purchase successful! Credits remaining: " + (int)player.getCredits());
		
		for (PurchaseItem item : cart) {
			// Add seeds to inventory
			for (int i = 0; i < item.quantity; i++) {
				Flower seed = FlowerRegistry.createSeed(item.name);
				if (seed != null) {
					player.addToInventory(seed);
				}
			}
			
			// Log for journal
			journalEntry.append(item.quantity).append("x ").append(item.name).append(", ");
		}

		// Save to journal and game
		String finalEntry = journalEntry.substring(0, journalEntry.length() - 2) + ". Total cost: " + (int)grandTotal + " credits.";
		Journal.addJournalEntry(player, finalEntry);
		Journal.saveGame(player); 
	}

	/**
	 * Handles selling items from the player's inventory
	 * 
	 * SELLING PRICES:
	 * - Seeds (not in pot): 50% of seed cost
	 * - Empty flower pots: 50% of craft cost (10 credits)
	 * - Seeds in flower pot: 100% pot value (20) + 50% seed cost
	 * - Seedlings in pot: 100% pot value (20) + 90% flower value
	 * - Bloomed flowers: 110% of value (+20 if in pot)
	 * - Matured flowers: 150% of value (+20 if in pot)
	 * - Mutated flowers: 500% of value (+20 if in pot)
	 * - Withered flowers: 1 credit (+10 if in pot) - FIX #4
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
			
			// Display all sellable items with calculated prices
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
					// Remove item from inventory
					inventory.remove(inventoryIndex);
					
					// Add credits to player
					player.setCredits(player.getCredits() + sellPrice);
					
					System.out.println("✅ Sold! You now have " + (int)player.getCredits() + " credits.");
					
					// Journal entry
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
	 * @param item The item to price
	 * @return The sell price in credits
	 */
	private static int calculateSellPrice(Object item) {
		if (item instanceof gardenPlot) {
			gardenPlot plot = (gardenPlot) item;
			
			// Empty flower pot
			if (plot.isFlowerPot() && !plot.isOccupied()) {
				return 10; // 50% of 20 credit craft cost
			}
			
			// Flower pot with plant
			if (plot.isFlowerPot() && plot.isOccupied()) {
				Flower plant = plot.getPlantedFlower();
				int basePrice = calculateFlowerPrice(plant);
				
				// For withered in pot: 1 credit + 10 (half pot value) = 11 (fix #4)
				if (plant.getGrowthStage().equals("Withered")) {
					return 1 + 10;
				}
				
				return basePrice + 20; // Add full pot value for non-withered
			}
		}
		
		if (item instanceof Flower) {
			Flower flower = (Flower) item;
			return calculateFlowerPrice(flower);
		}
		
		return 0; // Not sellable
	}
	
	/**
	 * Calculates the sell price for a flower based on growth stage
	 * @param flower The flower to price
	 * @return The sell price in credits
	 */
	private static int calculateFlowerPrice(Flower flower) {
		String stage = flower.getGrowthStage();
		String name = flower.getName();
		
		switch (stage) {
			case "Seed":
				// 50% of seed cost
				double seedCost = FlowerRegistry.getSeedCost(name);
				return (int)(seedCost * 0.5);
				
			case "Seedling":
				// 90% of seedling value
				double seedlingValue = FlowerRegistry.getFlowerValue(name, "Seedling");
				return (int)(seedlingValue * 0.9);
				
			case "Bloomed":
				// 110% of bloomed value
				double bloomedValue = FlowerRegistry.getFlowerValue(name, "Bloomed");
				return (int)(bloomedValue * 1.1);
				
			case "Matured":
				// 150% of matured value
				double maturedValue = FlowerRegistry.getFlowerValue(name, "Matured");
				return (int)(maturedValue * 1.5);
				
			case "Withered":
				// Always 1 credit
				return 1;
				
			case "Mutated":
				// 500% of mutated value
				double mutatedValue = FlowerRegistry.getFlowerValue(name, "Mutated");
				return (int)(mutatedValue * 5.0);
				
			default:
				return 0;
		}
	}
	
	/**
	 * Gets a descriptive string for an inventory item
	 * @param item The item to describe
	 * @return Description string
	 */
	private static String getItemDescription(Object item) {
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
	 * @param stage The growth stage
	 * @return Emoji string
	 */
	private static String getFlowerEmoji(String stage) {
		switch (stage) {
			case "Seed": return "🌱";
			case "Seedling": return "🌿";
			case "Bloomed": return "🌸";
			case "Matured": return "🌻";
			case "Withered": return "🥀";
			case "Mutated": return "✨";
			default: return "🌼";
		}
	}

	private static void handleBouquetAuction(Player1 player, Scanner scanner) {
		System.out.println("\n🎨 Bouquet Auction 🎨");
		System.out.println("Coming soon!");
		System.out.println("Press Enter to return to shop menu...");
		scanner.nextLine();
	}
}
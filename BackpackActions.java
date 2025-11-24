/* BackpackActions.java
 * Handles inventory display and item management
 * Updated: November 21, 2025
 */

import java.util.ArrayList;
import java.util.Scanner;

public class BackpackActions {
    
    /**
     * Main backpack interface with interactive options
     */
    public static void displayBackpack(Player1 player, Scanner scanner) {
        boolean inBackpack = true;
        
        while (inBackpack) {
            System.out.println("\n🎒 Your Backpack 🎒");
            System.out.println("Current resources: " + player.getNRG() + " NRG | " + player.getCredits() + " credits");
            System.out.println();
            
            ArrayList<Object> inventory = player.getInventory();
            
            if (inventory.isEmpty()) {
                System.out.println("Your backpack is empty.");
            } else {
                System.out.println("📦 Inventory Contents:");
                displayInventoryItems(inventory);
            }
            
            System.out.println("\nWhat would you like to do?");
            System.out.println("1: Use an item");
            System.out.println("2: Rearrange items");
            System.out.println("3: Dispose of an item");
            System.out.println("4: Create Bouquet");
            System.out.println("5: Disassemble Bouquet");
            System.out.println("6: Return to main menu");
            
            System.out.print("\nChoice: ");
            String backpackChoice = scanner.nextLine();
            
            switch (backpackChoice) {
                case "1":
                    handleUseItem(player, scanner);
                    break;
                    
                case "2":
                    handleRearrangeItems(player, scanner);
                    break;
                    
                case "3":
                    handleDisposeItem(player, scanner);
                    break;
                    
                case "4":
                    BouquetActions.handleCreateBouquet(player, scanner);
                    break;
                    
                case "5":
                    BouquetActions.handleDisassembleBouquet(player, scanner);
                    break;
                    
                case "6":
                    inBackpack = false;
                    break;
                    
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    /**
     * Displays all inventory items with formatting
     */
    private static void displayInventoryItems(ArrayList<Object> inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            Object item = inventory.get(i);
            System.out.print((i + 1) + ": ");
            
            if (item instanceof Bouquet) {
                Bouquet bouquet = (Bouquet) item;
                System.out.println(bouquet.getDisplayName());
            } else if (item instanceof Flower) {
                Flower flower = (Flower) item;
                System.out.println(flower.toString());
            } else if (item instanceof gardenPlot) {
                gardenPlot plot = (gardenPlot) item;
                if (plot.isFlowerPot()) {
                    if (plot.isOccupied()) {
                        Flower flower = plot.getPlantedFlower();
                        System.out.println("🪴 Flower Pot with " + flower.getName() + 
                                " (" + flower.getGrowthStage() + ")");
                    } else {
                        System.out.println("🪴 Empty Flower Pot");
                    }
                }
            } else {
                System.out.println(item.toString());
            }
        }
    }
    
    /**
     * Handles using items from inventory
     */
    private static void handleUseItem(Player1 player, Scanner scanner) {
        ArrayList<Object> inventory = player.getInventory();
        
        if (inventory.isEmpty()) {
            System.out.println("\nYour backpack is empty!");
            return;
        }
        
        System.out.println("\n🔧 Use Item");
        System.out.println("Select an item to use (or 0 to cancel):");
        displayInventoryItems(inventory);
        
        System.out.print("\nWhich item? (1-" + inventory.size() + " or 0): ");
        int itemChoice = getValidItemChoice(scanner, inventory.size());
        
        if (itemChoice == 0) {
            System.out.println("Use cancelled.");
            return;
        }
        
        Object selectedItem = inventory.get(itemChoice - 1);
        
        if (selectedItem instanceof Flower) {
            useFlower(player, (Flower) selectedItem, scanner);
        } else if (selectedItem instanceof gardenPlot) {
            gardenPlot plot = (gardenPlot) selectedItem;
            if (plot.isFlowerPot()) {
                useFlowerPot(player, plot, scanner);
            }
        } else {
            System.out.println("You can't use this item right now.");
        }
    }
    
    /**
     * Handles using a flower (eating seeds or planting in flower pot)
     */
    private static void useFlower(Player1 player, Flower flower, Scanner scanner) {
        String stage = flower.getGrowthStage();
        
        if (stage.equals("Seed")) {
            System.out.println("\n🌱 " + flower.getName() + " Seed");
            System.out.println("What would you like to do?");
            System.out.println("1: Eat the seed (restore NRG)");
            System.out.println("2: Plant in an empty flower pot");
            System.out.println("0: Cancel");
            
            System.out.print("\nChoice: ");
            String useChoice = scanner.nextLine();
            
            switch (useChoice) {
                case "1":
                    eatSeed(player, flower);
                    break;
                case "2":
                    plantSeedInFlowerPot(player, flower, scanner);
                    break;
                case "0":
                    System.out.println("Use cancelled.");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        } else {
            System.out.println("\nYou can't use this " + flower.getGrowthStage() + " " + 
                    flower.getName() + " directly.");
            System.out.println("Consider selling it at the shop or creating a bouquet!");
        }
    }
    
    /**
     * Eats a seed to restore NRG
     */
    private static void eatSeed(Player1 player, Flower seed) {
        if (seed instanceof FlowerInstance) {
            FlowerInstance flowerInstance = (FlowerInstance) seed;
            int nrgRestored = flowerInstance.getNRGRestored();
            
            player.setNRG(player.getNRG() + nrgRestored);
            player.removeFromInventory(seed);
            
            System.out.println("\n✅ You ate the " + seed.getName() + " seed.");
            System.out.println("Restored " + nrgRestored + " NRG!");
            System.out.println("Current NRG: " + player.getNRG());
            
            Journal.addJournalEntry(player, "Ate a " + seed.getName() + " seed for energy.");
        }
    }
    
    /**
     * Plants a seed in an empty flower pot from inventory
     */
    private static void plantSeedInFlowerPot(Player1 player, Flower seed, Scanner scanner) {
        ArrayList<Object> inventory = player.getInventory();
        
        ArrayList<Integer> emptyPotIndices = new ArrayList<>();
        for (int i = 0; i < inventory.size(); i++) {
            Object item = inventory.get(i);
            if (item instanceof gardenPlot) {
                gardenPlot plot = (gardenPlot) item;
                if (plot.isFlowerPot() && !plot.isOccupied()) {
                    emptyPotIndices.add(i);
                }
            }
        }
        
        if (emptyPotIndices.isEmpty()) {
            System.out.println("\nYou don't have any empty flower pots in your backpack!");
            System.out.println("You can craft flower pots in the Build menu.");
            return;
        }
        
        int difficulty = FlowerRegistry.getFlowerDifficulty(seed.getName());
        if (difficulty >= 4) {
            System.out.println("\n❌ This flower is too difficult (4★+) to plant in a flower pot!");
            System.out.println("You'll need to plant it in a regular garden plot.");
            return;
        }
        
        String nameLower = seed.getName().toLowerCase();
        if (nameLower.contains("bush") || nameLower.contains("tree")) {
            System.out.println("\n❌ Bushes and trees can't be planted in flower pots!");
            System.out.println("You'll need to plant it in a regular garden plot.");
            return;
        }
        
        System.out.println("\nAvailable empty flower pots:");
        for (int i = 0; i < emptyPotIndices.size(); i++) {
            System.out.println((i + 1) + ": Empty Flower Pot (slot " + 
                    (emptyPotIndices.get(i) + 1) + " in backpack)");
        }
        
        System.out.print("\nWhich pot? (1-" + emptyPotIndices.size() + " or 0 to cancel): ");
        int potChoice = getValidChoice(scanner, emptyPotIndices.size());
        
        if (potChoice == 0) {
            System.out.println("Planting cancelled.");
            return;
        }
        
        int potIndex = emptyPotIndices.get(potChoice - 1);
        gardenPlot selectedPot = (gardenPlot) inventory.get(potIndex);
        
        if (selectedPot.plantFlower(seed)) {
            System.out.println("\n✅ You planted the " + seed.getName() + " seed in the flower pot!");
            System.out.println("The potted plant is still in your backpack.");
            System.out.println("You can place it in your garden when you're ready.");
            
            player.removeFromInventory(seed);
            
            Journal.addJournalEntry(player, "Planted a " + seed.getName() + 
                    " seed in a flower pot.");
        } else {
            System.out.println("\n❌ Failed to plant the seed. This shouldn't happen!");
        }
    }
    
    /**
     * Handles using a flower pot
     */
    private static void useFlowerPot(Player1 player, gardenPlot pot, Scanner scanner) {
        if (pot.isOccupied()) {
            Flower flower = pot.getPlantedFlower();
            System.out.println("\n🪴 This flower pot already contains a " + flower.getName() + 
                    " (" + flower.getGrowthStage() + ").");
            System.out.println("You can place it in your garden when planting (option 3 in main menu).");
        } else {
            System.out.println("\n🪴 Empty Flower Pot");
            System.out.println("You can plant a seed in this pot (use a seed and select this option),");
            System.out.println("or place it in your garden when planting.");
        }
    }
    
    /**
     * Handles rearranging items in inventory
     */
    private static void handleRearrangeItems(Player1 player, Scanner scanner) {
        ArrayList<Object> inventory = player.getInventory();
        
        if (inventory.isEmpty()) {
            System.out.println("\nYour backpack is empty!");
            return;
        }
        
        if (inventory.size() == 1) {
            System.out.println("\nYou only have one item, nothing to rearrange!");
            return;
        }
        
        System.out.println("\n🔄 Rearrange Items");
        System.out.println("Current order:");
        displayInventoryItems(inventory);
        
        System.out.print("\nWhich item do you want to move? (1-" + inventory.size() + " or 0 to cancel): ");
        int itemToMove = getValidItemChoice(scanner, inventory.size());
        
        if (itemToMove == 0) {
            System.out.println("Rearrange cancelled.");
            return;
        }
        
        System.out.print("Move to which position? (1-" + inventory.size() + " or 0 to cancel): ");
        int newPosition = getValidItemChoice(scanner, inventory.size());
        
        if (newPosition == 0) {
            System.out.println("Rearrange cancelled.");
            return;
        }
        
        if (itemToMove == newPosition) {
            System.out.println("Item is already in that position!");
            return;
        }
        
        Object itemToSwap = inventory.remove(itemToMove - 1);
        inventory.add(newPosition - 1, itemToSwap);
        
        System.out.println("\n✅ Items rearranged!");
        System.out.println("\nNew order:");
        displayInventoryItems(inventory);
        
        Journal.saveGame(player);
    }
    
    /**
     * Handles permanently disposing of an item
     */
    private static void handleDisposeItem(Player1 player, Scanner scanner) {
        ArrayList<Object> inventory = player.getInventory();
        
        if (inventory.isEmpty()) {
            System.out.println("\nYour backpack is empty!");
            return;
        }
        
        System.out.println("\n🗑️ Dispose of Item");
        System.out.println("⚠️ WARNING: This will permanently delete the item!");
        System.out.println();
        displayInventoryItems(inventory);
        
        System.out.print("\nWhich item do you want to throw away? (1-" + inventory.size() + " or 0 to cancel): ");
        int itemChoice = getValidItemChoice(scanner, inventory.size());
        
        if (itemChoice == 0) {
            System.out.println("Dispose cancelled.");
            return;
        }
        
        Object selectedItem = inventory.get(itemChoice - 1);
        
        System.out.println("\nYou selected:");
        if (selectedItem instanceof Bouquet) {
            Bouquet bouquet = (Bouquet) selectedItem;
            System.out.println("  " + bouquet.getDisplayName());
        } else if (selectedItem instanceof Flower) {
            Flower flower = (Flower) selectedItem;
            System.out.println("  " + flower.toString());
        } else if (selectedItem instanceof gardenPlot) {
            gardenPlot plot = (gardenPlot) selectedItem;
            if (plot.isFlowerPot()) {
                if (plot.isOccupied()) {
                    System.out.println("  🪴 Flower Pot with " + 
                            plot.getPlantedFlower().getName() + 
                            " (" + plot.getPlantedFlower().getGrowthStage() + ")");
                } else {
                    System.out.println("  🪴 Empty Flower Pot");
                }
            }
        }
        
        System.out.println("\n⚠️ Are you ABSOLUTELY SURE you want to throw this away forever?");
        System.out.print("Type 'DELETE' to confirm, or anything else to cancel: ");
        String confirmation = scanner.nextLine();
        
        if (confirmation.equals("DELETE")) {
            String itemName;
            if (selectedItem instanceof Bouquet) {
                Bouquet bouquet = (Bouquet) selectedItem;
                itemName = bouquet.getDisplayName();
            } else if (selectedItem instanceof Flower) {
                Flower flower = (Flower) selectedItem;
                itemName = flower.getName() + " (" + flower.getGrowthStage() + ")";
            } else if (selectedItem instanceof gardenPlot) {
                gardenPlot plot = (gardenPlot) selectedItem;
                if (plot.isOccupied()) {
                    itemName = "Flower Pot with " + plot.getPlantedFlower().getName();
                } else {
                    itemName = "Empty Flower Pot";
                }
            } else {
                itemName = "item";
            }
            
            inventory.remove(itemChoice - 1);
            
            System.out.println("\n✅ " + itemName + " has been thrown away.");
            System.out.println("It's gone forever. 💀");
            
            Journal.addJournalEntry(player, "Disposed of " + itemName + ".");
            Journal.saveGame(player);
        } else {
            System.out.println("\nDispose cancelled. Item kept.");
        }
    }
    
    /**
     * Gets a valid item choice from user input
     */
    private static int getValidItemChoice(Scanner scanner, int maxChoice) {
        while (true) {
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice >= 0 && choice <= maxChoice) {
                    return choice;
                } else {
                    System.out.print("Invalid choice. Please enter 1-" + maxChoice + " or 0: ");
                }
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }
    
    /**
     * Gets a valid choice from user input
     */
    private static int getValidChoice(Scanner scanner, int maxChoice) {
        return getValidItemChoice(scanner, maxChoice);
    }
}
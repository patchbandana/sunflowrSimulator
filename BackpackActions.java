/* BackpackActions.java
 * Handles backpack/inventory display functionality
 * Created to modularize sunflowerSimulator.java
 */

import java.util.Scanner;

public class BackpackActions {
    
    /**
     * Displays the player's backpack with all inventory items
     * @param player The player whose backpack to display
     * @param scanner Scanner for user input (to pause before returning)
     */
    public static void displayBackpack(Player1 player, Scanner scanner) {
        System.out.println("\n📦 Checking your backpack...");
        
        if (player.getInventory().isEmpty()) {
            System.out.println("Your backpack is empty.");
        } else {
            System.out.println("Items in your backpack:");
            
            for (int i = 0; i < player.getInventory().size(); i++) {
                Object item = player.getInventory().get(i);
                
                if (item instanceof gardenPlot) {
                    // Special display for flower pots
                    gardenPlot pot = (gardenPlot) item;
                    if (pot.isFlowerPot()) {
                        System.out.println((i+1) + ". 🪴 Empty Flower Pot (place when planting)");
                    }
                } else {
                    // Display other items normally
                    System.out.println((i+1) + ". " + item);
                }
            }
        }

        System.out.println("\nPress Enter to return to the main menu...");
        scanner.nextLine();
    }
}
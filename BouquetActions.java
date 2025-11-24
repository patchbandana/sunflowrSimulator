/* BouquetActions.java
 * Handles bouquet creation and disassembly
 * UPDATED: Removed multiplier hints - players discover through experimentation
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BouquetActions {
    
    /**
     * Handles the bouquet creation workflow
     * @param player The player
     * @param scanner Scanner for user input
     * @return true if bouquet was created
     */
    public static boolean handleCreateBouquet(Player1 player, Scanner scanner) {
        System.out.println("\n💐 Create a Bouquet 💐");
        System.out.println("Bouquets must contain 3-12 flowers.");
        System.out.println("Only Bloomed, Matured, Mutated, or Withered flowers can be used.");
        
        // Get eligible flowers from inventory
        List<Flower> eligibleFlowers = getEligibleFlowers(player);
        
        if (eligibleFlowers.isEmpty()) {
            System.out.println("\n❌ You don't have any flowers that can be used in a bouquet!");
            System.out.println("You need at least 3 Bloomed, Matured, Mutated, or Withered flowers.");
            System.out.println("Press Enter to continue...");
            scanner.nextLine();
            return false;
        }
        
        if (eligibleFlowers.size() < 3) {
            System.out.println("\n❌ You need at least 3 eligible flowers to make a bouquet!");
            System.out.println("You have " + eligibleFlowers.size() + " eligible flower(s).");
            System.out.println("Press Enter to continue...");
            scanner.nextLine();
            return false;
        }
        
        // Display eligible flowers
        System.out.println("\n🌸 Eligible Flowers:");
        for (int i = 0; i < eligibleFlowers.size(); i++) {
            Flower flower = eligibleFlowers.get(i);
            System.out.println((i + 1) + ": " + flower.getName() + " (" + 
                    flower.getGrowthStage() + ")");
        }
        
        // Select flowers for bouquet
        List<Flower> selectedFlowers = selectFlowersForBouquet(eligibleFlowers, scanner);
        
        if (selectedFlowers == null || selectedFlowers.isEmpty()) {
            System.out.println("Bouquet creation cancelled.");
            return false;
        }
        
        // Optional: Give custom name
        System.out.print("\nWould you like to give this bouquet a custom name? (yes/no): ");
        String nameChoice = scanner.nextLine().toLowerCase();
        
        String customName = null;
        if (nameChoice.equals("yes")) {
            System.out.print("Enter bouquet name: ");
            customName = scanner.nextLine().trim();
            if (customName.isEmpty()) {
                customName = null;
            }
        }
        
        // Create the bouquet
        Bouquet bouquet = new Bouquet(selectedFlowers, customName, player.getDay());
        
        // Remove flowers from inventory
        for (Flower flower : selectedFlowers) {
            player.removeFromInventory(flower);
        }
        
        // Add bouquet to inventory
        player.addToInventory(bouquet);
        
        // Display result
        System.out.println("\n✅ Bouquet created successfully!");
        System.out.println(bouquet.getDetailedDescription());
        
        // Journal entry
        String journalEntry = "Created a bouquet with " + selectedFlowers.size() + " flowers";
        if (bouquet.hasCustomName()) {
            journalEntry += ": \"" + bouquet.getCustomName() + "\"";
        }
        journalEntry += ".";
        Journal.addJournalEntry(player, journalEntry);
        
        // Store this composition if it has a custom name
        if (bouquet.hasCustomName()) {
            String signature = bouquet.getCompositionSignature();
            if (player.hasKnownBouquetComposition(signature)) {
                System.out.println("\n✨ You've made this bouquet recipe before!");
                System.out.println("The fairies may recognize it...");
            } else {
                player.addKnownBouquetComposition(signature, bouquet.getCustomName());
                System.out.println("\n📝 Recipe saved: \"" + bouquet.getCustomName() + "\"");
                System.out.println("You can recreate this exact bouquet in the future.");
            }
        }
        
        Journal.saveGame(player);
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
        return true;
    }
    
    /**
     * Gets all eligible flowers from player's inventory
     */
    private static List<Flower> getEligibleFlowers(Player1 player) {
        List<Flower> eligible = new ArrayList<>();
        
        for (Object item : player.getInventory()) {
            if (item instanceof Flower) {
                Flower flower = (Flower) item;
                String stage = flower.getGrowthStage();
                
                // Must be Bloomed or higher (not Seed or Seedling)
                if (!stage.equals("Seed") && !stage.equals("Seedling")) {
                    eligible.add(flower);
                }
            }
            // Flower pots are NOT eligible (handled by instanceof check)
        }
        
        return eligible;
    }
    
    /**
     * Allows player to select flowers for the bouquet
     */
    private static List<Flower> selectFlowersForBouquet(List<Flower> eligibleFlowers, Scanner scanner) {
        List<Flower> selected = new ArrayList<>();
        
        System.out.println("\nSelect flowers for your bouquet (3-12 flowers).");
        System.out.println("Enter flower numbers separated by spaces (e.g., '1 3 5 7')");
        System.out.println("Or enter '0' to cancel.");
        System.out.print("\nYour selection: ");
        
        String input = scanner.nextLine().trim();
        
        if (input.equals("0")) {
            return null;
        }
        
        // Parse input
        String[] parts = input.split("\\s+");
        for (String part : parts) {
            try {
                int index = Integer.parseInt(part) - 1;
                if (index >= 0 && index < eligibleFlowers.size()) {
                    selected.add(eligibleFlowers.get(index));
                } else {
                    System.out.println("Warning: Invalid number '" + part + "' ignored.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Warning: Invalid input '" + part + "' ignored.");
            }
        }
        
        // Validate selection
        if (selected.size() < 3) {
            System.out.println("\n❌ You must select at least 3 flowers!");
            return null;
        }
        
        if (selected.size() > 12) {
            System.out.println("\n❌ You can only select up to 12 flowers!");
            return null;
        }
        
        // Show selection
        System.out.println("\nSelected flowers:");
        for (Flower flower : selected) {
            System.out.println("  • " + flower.getName() + " (" + flower.getGrowthStage() + ")");
        }
        
        System.out.print("\nConfirm selection? (yes/no): ");
        String confirm = scanner.nextLine().toLowerCase();
        
        if (!confirm.equals("yes")) {
            return null;
        }
        
        return selected;
    }
    
    /**
     * Handles bouquet disassembly
     */
    public static boolean handleDisassembleBouquet(Player1 player, Scanner scanner) {
        System.out.println("\n🌸 Disassemble a Bouquet 🌸");
        
        // Get bouquets from inventory
        List<Bouquet> bouquets = getBouquetsFromInventory(player);
        
        if (bouquets.isEmpty()) {
            System.out.println("\nYou don't have any bouquets to disassemble!");
            System.out.println("Press Enter to continue...");
            scanner.nextLine();
            return false;
        }
        
        // Display bouquets
        System.out.println("\nYour Bouquets:");
        for (int i = 0; i < bouquets.size(); i++) {
            System.out.println((i + 1) + ": " + bouquets.get(i).getDisplayName());
        }
        
        System.out.print("\nWhich bouquet would you like to disassemble? (1-" + 
                bouquets.size() + ", or 0 to cancel): ");
        
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return false;
        }
        
        if (choice == 0) {
            System.out.println("Disassembly cancelled.");
            return false;
        }
        
        if (choice < 1 || choice > bouquets.size()) {
            System.out.println("Invalid choice.");
            return false;
        }
        
        Bouquet selectedBouquet = bouquets.get(choice - 1);
        
        // Show details and confirm
        System.out.println("\n" + selectedBouquet.getDetailedDescription());
        System.out.print("\nDisassemble this bouquet? All flowers will return to your inventory. (yes/no): ");
        
        String confirm = scanner.nextLine().toLowerCase();
        
        if (!confirm.equals("yes")) {
            System.out.println("Disassembly cancelled.");
            return false;
        }
        
        // Disassemble: remove bouquet, add flowers back
        player.removeFromInventory(selectedBouquet);
        
        for (Flower flower : selectedBouquet.getFlowers()) {
            player.addToInventory(flower);
        }
        
        System.out.println("\n✅ Bouquet disassembled!");
        System.out.println(selectedBouquet.getFlowerCount() + " flowers returned to your inventory.");
        
        Journal.addJournalEntry(player, "Disassembled a bouquet, returning " + 
                selectedBouquet.getFlowerCount() + " flowers to inventory.");
        Journal.saveGame(player);
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
        return true;
    }
    
    /**
     * Gets all bouquets from player's inventory
     */
    private static List<Bouquet> getBouquetsFromInventory(Player1 player) {
        List<Bouquet> bouquets = new ArrayList<>();
        
        for (Object item : player.getInventory()) {
            if (item instanceof Bouquet) {
                bouquets.add((Bouquet) item);
            }
        }
        
        return bouquets;
    }
}
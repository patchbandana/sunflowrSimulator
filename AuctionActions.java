/* AuctionActions.java
 * Handles auction house UI and interactions
 * UPDATED: Removed all multiplier information - players discover through experimentation
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AuctionActions {
    
    /**
     * Handles the auction house menu
     */
    public static void handleAuctionHouse(Player1 player, Scanner scanner) {
        AuctionHouse auctionHouse = player.getAuctionHouse();
        
        boolean inAuctionMenu = true;
        
        while (inAuctionMenu) {
            displayAuctionMenu(player, auctionHouse);
            
            System.out.print("\nChoice: ");
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1": // Post Bouquet
                    handlePostBouquet(player, auctionHouse, scanner);
                    break;
                    
                case "2": // View Status
                    handleViewStatus(player, auctionHouse, scanner);
                    break;
                    
                case "3": // Accept Current Bid
                    handleAcceptBid(player, auctionHouse, scanner);
                    break;
                    
                case "4": // Collect Earnings
                    handleCollectEarnings(player, auctionHouse, scanner);
                    break;
                    
                case "5": // Return
                    inAuctionMenu = false;
                    break;
                    
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    /**
     * Displays the auction house menu
     */
    private static void displayAuctionMenu(Player1 player, AuctionHouse auctionHouse) {
        System.out.println("\n🏛️ Fairy Auction House 🏛️");
        System.out.println("Current resources: " + player.getNRG() + " NRG | " + 
                          player.getCredits() + " credits");
        System.out.println();
        
        // Show status
        if (auctionHouse.hasUncollectedEarnings()) {
            System.out.println("💰 You have earnings waiting to be collected!");
            System.out.println("   Amount: " + (int)auctionHouse.getEarningsWaiting() + " credits");
        } else if (auctionHouse.hasActiveAuction()) {
            System.out.println("📊 Current Auction Status:");
            int auctionDay = auctionHouse.getAuctionDay(player.getDay());
            System.out.println("   Bouquet: " + auctionHouse.getCurrentBouquet().getDisplayName());
            System.out.println("   Day: " + auctionDay + "/7");
            System.out.println("   Current Bid: " + (int)auctionHouse.getCurrentBid() + " credits");
        } else {
            System.out.println("🔭 No active auction.");
        }
        
        System.out.println("\nWhat would you like to do?");
        System.out.println("1: Post Bouquet for Auction");
        System.out.println("2: View Auction Details");
        System.out.println("3: Accept Current Bid");
        System.out.println("4: Collect Earnings");
        System.out.println("5: Return to Shop Menu");
    }
    
    /**
     * Handles posting a bouquet for auction
     */
    private static void handlePostBouquet(Player1 player, AuctionHouse auctionHouse, Scanner scanner) {
        if (auctionHouse.hasActiveAuction()) {
            System.out.println("\n❌ You already have a bouquet at auction!");
            System.out.println("Wait for it to finish or accept the current bid.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        if (auctionHouse.hasUncollectedEarnings()) {
            System.out.println("\n❌ You must collect your previous earnings first!");
            System.out.println("Use option 4 to collect " + 
                             (int)auctionHouse.getEarningsWaiting() + " credits.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        // Get bouquets from inventory
        List<Bouquet> bouquets = getBouquetsFromInventory(player);
        
        if (bouquets.isEmpty()) {
            System.out.println("\n❌ You don't have any bouquets to auction!");
            System.out.println("Create bouquets from your backpack menu.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        // Display bouquets
        System.out.println("\n💐 Your Bouquets:");
        for (int i = 0; i < bouquets.size(); i++) {
            Bouquet bouquet = bouquets.get(i);
            System.out.println((i + 1) + ": " + bouquet.getDisplayName());
        }
        
        System.out.print("\nWhich bouquet would you like to auction? (1-" + 
                        bouquets.size() + ", or 0 to cancel): ");
        
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }
        
        if (choice == 0) {
            System.out.println("Posting cancelled.");
            return;
        }
        
        if (choice < 1 || choice > bouquets.size()) {
            System.out.println("Invalid choice.");
            return;
        }
        
        Bouquet selectedBouquet = bouquets.get(choice - 1);
        
        // Show details
        System.out.println("\n" + selectedBouquet.getDetailedDescription());
        System.out.println("Starting bid: " + (int)selectedBouquet.getBaseValue() + " credits");
        System.out.println("\n📌 Auction Info:");
        System.out.println("  • The auction will last up to 7 days");
        System.out.println("  • Each day, fairies may notice special qualities in your bouquet");
        System.out.println("  • On day 7, a Royal Fairy will make the final bid");
        System.out.println("  • You can accept the bid early at any time");
        System.out.println("  • The bid will never go below base value");
        
        System.out.print("\nPost this bouquet for auction? (yes/no): ");
        String confirm = scanner.nextLine().toLowerCase();
        
        if (!confirm.equals("yes")) {
            System.out.println("Posting cancelled.");
            return;
        }
        
        // Remove bouquet from inventory and start auction
        player.removeFromInventory(selectedBouquet);
        
        // Check for custom name recognition bonus (silent - player doesn't know)
        if (selectedBouquet.hasCustomName()) {
            String signature = selectedBouquet.getCompositionSignature();
            String knownName = player.getKnownBouquetName(signature);
            
            if (knownName != null && knownName.equals(selectedBouquet.getCustomName())) {
            }
        }
        
        auctionHouse.startAuction(selectedBouquet, player.getDay(), player);
        
        System.out.println("\n✅ Bouquet posted to auction!");
        System.out.println("The auction begins today (Day 1).");
        System.out.println("Check back each day to see how the bidding progresses!");
        
        Journal.addJournalEntry(player, "Posted a bouquet for auction: " + 
                              selectedBouquet.getDisplayName());
        Journal.saveGame(player);
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Handles viewing auction details
     */
    private static void handleViewStatus(Player1 player, AuctionHouse auctionHouse, Scanner scanner) {
        if (!auctionHouse.hasActiveAuction()) {
            System.out.println("\n🔭 No active auction to view.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        int auctionDay = auctionHouse.getAuctionDay(player.getDay());
        
        System.out.println("\n📊 Auction Status");
        System.out.println("════════════════════════════════════");
        System.out.println("Bouquet: " + auctionHouse.getCurrentBouquet().getDisplayName());
        System.out.println("Day: " + auctionDay + "/7");
        System.out.println("Current Bid: " + (int)auctionHouse.getCurrentBid() + " credits");
        System.out.println("Starting Bid: " + (int)auctionHouse.getCurrentBouquet().getBaseValue() + " credits");
        System.out.println("════════════════════════════════════");
        
        System.out.println("\nBouquet Details:");
        System.out.println(auctionHouse.getCurrentBouquet().getDetailedDescription());
        
        if (auctionDay <= 6) {
            System.out.println("💡 The auction will continue until day 7, or you can accept now.");
        } else {
            System.out.println("👑 The Royal Fairy has made the final bid!");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Handles accepting the current bid
     */
    private static void handleAcceptBid(Player1 player, AuctionHouse auctionHouse, Scanner scanner) {
        if (!auctionHouse.hasActiveAuction()) {
            System.out.println("\n❌ No active auction to accept!");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("\n💰 Current Bid: " + (int)auctionHouse.getCurrentBid() + " credits");
        System.out.println("Base Value: " + (int)auctionHouse.getCurrentBouquet().getBaseValue() + " credits");
        
        int auctionDay = auctionHouse.getAuctionDay(player.getDay());
        System.out.println("Auction Day: " + auctionDay + "/7");
        
        System.out.println("\n⚠️  If you accept now, the auction ends immediately.");
        System.out.println("Waiting longer may increase the bid (or it may not).");
        
        System.out.print("\nAccept the current bid? (yes/no): ");
        String confirm = scanner.nextLine().toLowerCase();
        
        if (!confirm.equals("yes")) {
            System.out.println("Continuing auction.");
            return;
        }
        
        String result = auctionHouse.acceptBid();
        System.out.println("\n" + result);
        
        Journal.addJournalEntry(player, "Accepted auction bid of " + 
                              (int)auctionHouse.getEarningsWaiting() + " credits on day " + auctionDay + ".");
        Journal.saveGame(player);
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Handles collecting earnings
     */
    private static void handleCollectEarnings(Player1 player, AuctionHouse auctionHouse, Scanner scanner) {
        if (!auctionHouse.hasUncollectedEarnings()) {
            System.out.println("\n❌ No earnings to collect!");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        int earnings = auctionHouse.collectEarnings();
        player.setCredits(player.getCredits() + earnings);
        
        System.out.println("\n💰 Collected " + earnings + " credits!");
        System.out.println("New balance: " + player.getCredits() + " credits");
        
        Journal.addJournalEntry(player, "Collected " + earnings + " credits from auction house.");
        Journal.saveGame(player);
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
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
/* AuctionHouse.java
 * Manages bouquet auction state and bidding mechanics
 * UPDATED: Hidden multiplier system - players discover through experimentation
 * 
 * AUCTION MECHANICS:
 * - Day 1: Bouquet posted, no bids yet
 * - Days 2-6: Fairies bid, applying one random multiplier per day IF available
 * - Day 7: Royal fairy applies ALL remaining multipliers IF any exist
 * - If no multipliers available, bid increases by 1 credit per day
 * - Player can accept offer early or wait
 * - Must collect earnings before next auction
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AuctionHouse {
    private Bouquet currentBouquet;
    private int auctionStartDay;
    private double currentBid;
    private List<String> appliedMultipliers;
    private double earningsWaiting;
    private boolean hasCollectedEarnings;
    
    private static final Random random = new Random();
    
    /**
     * Creates a new auction house (starts with no active auction)
     */
    public AuctionHouse() {
        this.currentBouquet = null;
        this.auctionStartDay = -1;
        this.currentBid = 0;
        this.appliedMultipliers = new ArrayList<>();
        this.earningsWaiting = 0;
        this.hasCollectedEarnings = true;
    }
    
    /**
     * Checks if there's an active auction
     */
    public boolean hasActiveAuction() {
        return currentBouquet != null;
    }
    
    /**
     * Checks if there are uncollected earnings
     */
    public boolean hasUncollectedEarnings() {
        return !hasCollectedEarnings && earningsWaiting > 0;
    }
    
    /**
     * Starts a new auction with the given bouquet
     */
    public boolean startAuction(Bouquet bouquet, int currentDay) {
        if (hasActiveAuction()) {
            return false; // Already have an active auction
        }
        
        if (hasUncollectedEarnings()) {
            return false; // Must collect previous earnings first
        }
        
        this.currentBouquet = bouquet;
        this.auctionStartDay = currentDay;
        this.currentBid = bouquet.getBaseValue(); // Start at base value
        this.appliedMultipliers = new ArrayList<>();
        this.earningsWaiting = 0;
        this.hasCollectedEarnings = true;
        
        return true;
    }
    
    /**
     * Gets the current auction day (1-7)
     */
    public int getAuctionDay(int currentDay) {
        if (!hasActiveAuction()) {
            return 0;
        }
        return currentDay - auctionStartDay + 1;
    }
    
    /**
     * Processes daily bidding when player sleeps
     */
    public String processDailyBid(int currentDay, Player1 player) {
        if (!hasActiveAuction()) {
            return null;
        }
        
        int auctionDay = getAuctionDay(currentDay);
        
        if (auctionDay == 1) {
            return "📢 Your bouquet is now listed at the auction house!";
        }
        
        if (auctionDay >= 2 && auctionDay <= 6) {
            // Regular fairy bids (apply one random multiplier if available)
            return applyRandomMultiplier(player);
        }
        
        if (auctionDay == 7) {
            // Royal fairy applies ALL remaining multipliers (or +1 if none)
            return applyAllRemainingMultipliers(player);
        }
        
        // Day 8+: auction stays at current bid
        return null;
    }
    
    /**
     * Applies a random multiplier to the current bid
     * If no multipliers available, increases bid by 1
     */
    private String applyRandomMultiplier(Player1 player) {
        List<MultiplierRule> availableMultipliers = getAvailableMultipliers();
        
        if (availableMultipliers.isEmpty()) {
            // No multipliers available - just increase by 1
            double oldBid = currentBid;
            currentBid += 1;
            return "🧚 A fairy examined your bouquet.\n" +
                   "   Bid increased from " + (int)oldBid + " to " + (int)currentBid + " credits.";
        }
        
        // Pick random multiplier
        MultiplierRule chosen = availableMultipliers.get(random.nextInt(availableMultipliers.size()));
        appliedMultipliers.add(chosen.name);
        
        double oldBid = currentBid;
        currentBid *= chosen.multiplier;
        
        // Generic message - doesn't reveal what was noticed or the multiplier
        return "🧚 A fairy noticed something special about your bouquet!\n" +
               "   Bid increased from " + (int)oldBid + " to " + (int)currentBid + " credits!";
    }
    
    /**
     * Applies ALL remaining multipliers (Royal fairy on day 7)
     * If no multipliers available, increases bid by 1
     */
    private String applyAllRemainingMultipliers(Player1 player) {
        List<MultiplierRule> availableMultipliers = getAvailableMultipliers();
        
        if (availableMultipliers.isEmpty()) {
            // No multipliers left - just increase by 1 and end
            double oldBid = currentBid;
            currentBid += 1;
            endAuction();
            return "👑 The Royal Fairy examined your bouquet carefully.\n" +
                   "   Bid increased from " + (int)oldBid + " to " + (int)currentBid + " credits.\n" +
                   "   The auction has ended. You may collect " + (int)earningsWaiting + " credits.";
        }
        
        double oldBid = currentBid;
        
        // Apply all remaining multipliers
        for (MultiplierRule rule : availableMultipliers) {
            appliedMultipliers.add(rule.name);
            currentBid *= rule.multiplier;
        }
        
        // Generic message - doesn't reveal how many multipliers or what they were
        endAuction();
        return "👑 The Royal Fairy arrived and was deeply impressed by your bouquet!\n" +
               "   Final bid: " + (int)oldBid + " → " + (int)currentBid + " credits!\n" +
               "   The auction has ended. You may collect " + (int)earningsWaiting + " credits.";
    }
    
    /**
     * Gets all multipliers that haven't been applied yet
     */
    private List<MultiplierRule> getAvailableMultipliers() {
        List<MultiplierRule> available = new ArrayList<>();
        
        for (MultiplierRule rule : MultiplierRule.getAllRules()) {
            if (!appliedMultipliers.contains(rule.name) && rule.appliesTo(currentBouquet)) {
                available.add(rule);
            }
        }
        
        return available;
    }
    
    /**
     * Ends the auction and sets earnings
     */
    private void endAuction() {
        this.earningsWaiting = currentBid;
        this.hasCollectedEarnings = false;
        this.currentBouquet = null;
    }
    
    /**
     * Player accepts the current bid early
     */
    public String acceptBid() {
        if (!hasActiveAuction()) {
            return null;
        }
        
        endAuction();
        return "✅ You accepted the current bid of " + (int)earningsWaiting + " credits!\n" +
               "Visit the auction house to collect your earnings.";
    }
    
    /**
     * Collects earnings from completed auction
     */
    public int collectEarnings() {
        if (!hasUncollectedEarnings()) {
            return 0;
        }
        
        int earnings = (int)earningsWaiting;
        this.earningsWaiting = 0;
        this.hasCollectedEarnings = true;
        
        return earnings;
    }
    
    // Getters
    public Bouquet getCurrentBouquet() {
        return currentBouquet;
    }
    
    public double getCurrentBid() {
        return currentBid;
    }
    
    public double getEarningsWaiting() {
        return earningsWaiting;
    }
    
    public List<String> getAppliedMultipliers() {
        return new ArrayList<>(appliedMultipliers);
    }
    
    /**
     * Gets a status summary for display
     */
    public String getStatusSummary(int currentDay) {
        if (hasUncollectedEarnings()) {
            return "💰 Earnings waiting: " + (int)earningsWaiting + " credits";
        }
        
        if (!hasActiveAuction()) {
            return "No active auction.";
        }
        
        int auctionDay = getAuctionDay(currentDay);
        StringBuilder sb = new StringBuilder();
        sb.append("Current Auction:\n");
        sb.append("  Bouquet: ").append(currentBouquet.getDisplayName()).append("\n");
        sb.append("  Auction Day: ").append(auctionDay).append("/7\n");
        sb.append("  Current Bid: ").append((int)currentBid).append(" credits\n");
        sb.append("  Base Value: ").append((int)currentBouquet.getBaseValue()).append(" credits\n");
        
        return sb.toString();
    }
}
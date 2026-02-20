/* AuctionHouse.java
 * Manages bouquet auction state and bidding mechanics
 * UPDATED: Added save/load support, recognition bonus on day 2, hidden multiplier system
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
    private boolean recognitionBonusApplied; // NEW: Track if 1.2x bonus applied
    
    private static final Random random = new Random();
    
    public AuctionHouse() {
        this.currentBouquet = null;
        this.auctionStartDay = -1;
        this.currentBid = 0;
        this.appliedMultipliers = new ArrayList<>();
        this.earningsWaiting = 0;
        this.hasCollectedEarnings = true;
        this.recognitionBonusApplied = false;
    }
    
    public boolean hasActiveAuction() {
        return currentBouquet != null;
    }
    
    public boolean hasUncollectedEarnings() {
        return !hasCollectedEarnings && earningsWaiting > 0;
    }
    
    public boolean startAuction(Bouquet bouquet, int currentDay, Player1 player) {
        if (hasActiveAuction()) {
            return false;
        }
        
        if (hasUncollectedEarnings()) {
            return false;
        }
        
        this.currentBouquet = bouquet;
        this.auctionStartDay = currentDay;
        this.currentBid = bouquet.getBaseValue();
        this.appliedMultipliers = new ArrayList<>();
        this.earningsWaiting = 0;
        this.hasCollectedEarnings = true;
        this.recognitionBonusApplied = false;
        
        return true;
    }
    
    public int getAuctionDay(int currentDay) {
        if (!hasActiveAuction()) {
            return 0;
        }
        return currentDay - auctionStartDay + 1;
    }
    
    public String processDailyBid(int currentDay, Player1 player) {
        if (!hasActiveAuction()) {
            return null;
        }
        
        int auctionDay = getAuctionDay(currentDay);
        
        if (auctionDay == 1) {
            return "ðŸ“¢ Your bouquet is now listed at the auction house!";
        }
        
        // Day 2: Apply 1.2x bonus for ANY custom name
        if (auctionDay == 2 && !recognitionBonusApplied) {
            if (currentBouquet.hasCustomName()) {
                // Apply 1.2x bonus for having a custom name
                double oldBid = currentBid;
                currentBid *= 1.2;
                recognitionBonusApplied = true;
                
                return "🧚 A fairy noticed your \"" + currentBouquet.getCustomName() + "\" arrangement!\n" +
                       "   Bid increased from " + (int)oldBid + " to " + (int)currentBid + " credits.";
            }
        }
        
        // Days 3-6: Apply one random multiplier per day
        if (auctionDay >= 3 && auctionDay <= 6) {
            return applyRandomMultiplier(player);
        }
        
        if (auctionDay == 7) {
            return applyAllRemainingMultipliers(player);
        }
        
        return null;
    }
    
    private String applyRandomMultiplier(Player1 player) {
        List<MultiplierRule> availableMultipliers = getAvailableMultipliers();
        
        if (availableMultipliers.isEmpty()) {
            double oldBid = currentBid;
            currentBid += 1;
            return "ðŸ§š A fairy examined your bouquet.\n" +
                   "   Bid increased from " + (int)oldBid + " to " + (int)currentBid + " credits.";
        }
        
        MultiplierRule chosen = availableMultipliers.get(random.nextInt(availableMultipliers.size()));
        appliedMultipliers.add(chosen.name);
        
        double oldBid = currentBid;
        currentBid *= chosen.multiplier;
        
        return "ðŸ§š A fairy noticed something special about your bouquet!\n" +
               "   Bid increased from " + (int)oldBid + " to " + (int)currentBid + " credits!";
    }
    
    private String applyAllRemainingMultipliers(Player1 player) {
        List<MultiplierRule> availableMultipliers = getAvailableMultipliers();
        
        if (availableMultipliers.isEmpty()) {
            double oldBid = currentBid;
            currentBid += 1;
            endAuction();
            return "ðŸ§š A fairy examined your bouquet one last time.\n" +
                   "   Bid increased from " + (int)oldBid + " to " + (int)currentBid + " credits.\n" +
                   "   The auction has ended. You may collect " + (int)earningsWaiting + " credits.";
        }
        
        double oldBid = currentBid;
        
        for (MultiplierRule rule : availableMultipliers) {
            appliedMultipliers.add(rule.name);
            currentBid *= rule.multiplier;
        }
        
        endAuction();
        return "ðŸ‘‘ An important fairy arrived and was deeply impressed!\n" +
               "   Final bid: " + (int)oldBid + " â†’ " + (int)currentBid + " credits!\n" +
               "   The auction has ended. You may collect " + (int)earningsWaiting + " credits.";
    }
    
    private List<MultiplierRule> getAvailableMultipliers() {
        List<MultiplierRule> available = new ArrayList<>();
        
        for (MultiplierRule rule : MultiplierRule.getAllRules()) {
            if (!appliedMultipliers.contains(rule.name) && rule.appliesTo(currentBouquet)) {
                available.add(rule);
            }
        }
        
        return available;
    }
    
    private void endAuction() {
        this.earningsWaiting = currentBid;
        this.hasCollectedEarnings = false;
        this.currentBouquet = null;
    }
    
    public String acceptBid() {
        if (!hasActiveAuction()) {
            return null;
        }
        
        endAuction();
        return "âœ… You accepted the current bid of " + (int)earningsWaiting + " credits!\n" +
               "Visit the auction house to collect your earnings.";
    }
    
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
    
    public int getAuctionStartDay() {
        return auctionStartDay;
    }
    
    public boolean isRecognitionBonusApplied() {
        return recognitionBonusApplied;
    }
    
    // Setters for save/load
    public void setCurrentBouquet(Bouquet bouquet) {
        this.currentBouquet = bouquet;
    }
    
    public void setAuctionStartDay(int day) {
        this.auctionStartDay = day;
    }
    
    public void setCurrentBid(double bid) {
        this.currentBid = bid;
    }
    
    public void setAppliedMultipliers(List<String> multipliers) {
        this.appliedMultipliers = new ArrayList<>(multipliers);
    }
    
    public void setEarningsWaiting(double earnings) {
        this.earningsWaiting = earnings;
    }
    
    public void setHasCollectedEarnings(boolean collected) {
        this.hasCollectedEarnings = collected;
    }
    
    public void setRecognitionBonusApplied(boolean applied) {
        this.recognitionBonusApplied = applied;
    }
    
    public String getStatusSummary(int currentDay) {
        if (hasUncollectedEarnings()) {
            return "ðŸ’° Earnings waiting: " + (int)earningsWaiting + " credits";
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
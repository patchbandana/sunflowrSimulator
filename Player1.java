/* Player1.java
 * Player data management and day advancement
 * Updated: November 21, 2025
 */

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public class Player1 {

	private String name;
	private int nrg;
	private int credits;
	private int day;

	private ArrayList<Object> inventory;
	private List<String> journalEntries;
	private List<gardenPlot> gardenPlots;

	private int flowerPotsCrafted;
	private static final int MAX_FLOWER_POTS = 10;

	private boolean hasBuiltExtraPlot;
	private Set<String> unlockedDreams;
	private Set<String> unlockedHints;

	private boolean hasCompostBin;
	private int compostWitheredCount;
	
	private AuctionHouse auctionHouse;
	private Map<String, String> knownBouquetCompositions;
	private Map<String, Integer> bouquetHighScores;
	
	// Compost bin upgrades
	private boolean hasMulcher;
	private int mulcherDaysRemaining; // Days of 0.25x weed growth remaining
	private boolean hasSprinklerSystem;

	public Player1(String name) {
		this.name = name;
		this.nrg = 10;
		this.credits = 100;
		this.day = 1;
		this.inventory = new ArrayList<>();
		this.journalEntries = new ArrayList<>();
		this.flowerPotsCrafted = 0;
		this.hasBuiltExtraPlot = false;
		this.unlockedDreams = new HashSet<>();
		this.unlockedHints = new HashSet<>();

		this.hasCompostBin = false;
		this.compostWitheredCount = 0;
		
		this.auctionHouse = new AuctionHouse();
		this.knownBouquetCompositions = new HashMap<>();
		this.bouquetHighScores = new HashMap<>();

		this.gardenPlots = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			this.gardenPlots.add(new gardenPlot());
		}
		this.hasMulcher = false;
	    this.mulcherDaysRemaining = 0;
	    this.hasSprinklerSystem = false;
	}

	public void addToInventory(Object item) {
		inventory.add(item);
	}

	public boolean removeFromInventory(Object item) {
		return inventory.remove(item);
	}

	public Object removeFromInventory(int index) {
		if (index >= 0 && index < inventory.size()) {
			return inventory.remove(index);
		}
		return null;
	}

	public ArrayList<Object> getInventory() {
		return inventory;
	}

	public List<gardenPlot> getGardenPlots() {
		return gardenPlots;
	}

	public gardenPlot getGardenPlot(int index) {
		if (index >= 0 && index < gardenPlots.size()) {
			return gardenPlots.get(index);
		}
		return null;
	}

	public void addGardenPlot() {
		gardenPlots.add(new gardenPlot());
	}

	public void addFlowerPotToGarden(gardenPlot flowerPot) {
		gardenPlots.add(flowerPot);
	}

	public int getPlacedFlowerPotCount() {
		int count = 0;
		for (gardenPlot plot : gardenPlots) {
			if (plot.isFlowerPot()) {
				count++;
			}
		}
		return count;
	}

	public int getInventoryFlowerPotCount() {
		int count = 0;
		for (Object item : inventory) {
			if (item instanceof gardenPlot && ((gardenPlot)item).isFlowerPot()) {
				count++;
			}
		}
		return count;
	}

	public boolean canCraftFlowerPot() {
		int totalFlowerPots = getPlacedFlowerPotCount() + getInventoryFlowerPotCount();
		return totalFlowerPots < MAX_FLOWER_POTS;
	}

	public int getTotalFlowerPots() {
		return getPlacedFlowerPotCount() + getInventoryFlowerPotCount();
	}

	public void craftFlowerPot() {
		flowerPotsCrafted++;
	}

	public int getFlowerPotsCrafted() {
		return flowerPotsCrafted;
	}

	public void setFlowerPotsCrafted(int count) {
		this.flowerPotsCrafted = count;
	}

	public boolean hasBuiltExtraPlot() {
		return hasBuiltExtraPlot;
	}

	public void setHasBuiltExtraPlot(boolean hasBuilt) {
		this.hasBuiltExtraPlot = hasBuilt;
	}

	public void unlockDream(String dreamFilename) {
		unlockedDreams.add(dreamFilename);
	}

	public boolean hasDreamUnlocked(String dreamFilename) {
		return unlockedDreams.contains(dreamFilename);
	}

	public Set<String> getUnlockedDreams() {
		return new HashSet<>(unlockedDreams);
	}
	
	public void unlockHint(String hintFilename) {
	    unlockedHints.add(hintFilename);
	}

	public boolean hasHintUnlocked(String hintFilename) {
	    return unlockedHints.contains(hintFilename);
	}

	public Set<String> getUnlockedHints() {
	    return new HashSet<>(unlockedHints);
	}

	public void setUnlockedHints(Set<String> hints) {
	    this.unlockedHints = new HashSet<>(hints);
	}

	public int getUnlockedHintCount() {
	    return unlockedHints.size();
	}

	public boolean hasCompostBin() {
		return hasCompostBin;
	}

	public void setHasCompostBin(boolean hasCompostBin) {
		this.hasCompostBin = hasCompostBin;
	}

	public void buildCompostBin() {
		this.hasCompostBin = true;
	}

	public int getCompostWitheredCount() {
		return compostWitheredCount;
	}

	public void setCompostWitheredCount(int count) {
		this.compostWitheredCount = Math.max(0, count);
	}

	public void setUnlockedDreams(Set<String> dreams) {
		this.unlockedDreams = new HashSet<>(dreams);
	}

	public int getUnlockedDreamCount() {
		return unlockedDreams.size();
	}

	public void addJournalEntry(String entry) {
		journalEntries.add(entry);
	}

	public List<String> getJournalEntries() {
		return journalEntries;
	}

	public void setJournalEntries(List<String> entries) {
		this.journalEntries = entries;
	}

	public void printInventory() {
		if (inventory.isEmpty()) {
			System.out.println("Your backpack is empty.");
		} else {
			System.out.println("ðŸ“¦ Inventory:");
			for (int i = 0; i < inventory.size(); i++) {
				System.out.println("- " + inventory.get(i));
			}
		}
	}

	public void printGarden() {
		System.out.println("\nðŸŒ± Your Garden ðŸŒ±");
		for (int i = 0; i < gardenPlots.size(); i++) {
			System.out.println("Plot #" + (i+1) + ":");
			System.out.println(gardenPlots.get(i));
		}
	}

	public String getName() {
		return name;
	}

	public int getNRG() {
		return nrg;
	}

	public void setNRG(int nrg) {
		this.nrg = nrg;
	}

	public int getCredits() {
		return credits;
	}

	public void setCredits(int credits) {
		this.credits = credits;
	}

	public int getDay() {
		return day;
	}

	public AuctionHouse getAuctionHouse() {
	    return auctionHouse;
	}

	public void setAuctionHouse(AuctionHouse auctionHouse) {
	    this.auctionHouse = auctionHouse;
	}

	public void addKnownBouquetComposition(String signature, String customName) {
	    knownBouquetCompositions.put(signature, customName);
	}

	public boolean hasKnownBouquetComposition(String signature) {
	    return knownBouquetCompositions.containsKey(signature);
	}

	public String getKnownBouquetName(String signature) {
	    return knownBouquetCompositions.get(signature);
	}

	public Map<String, String> getKnownBouquetCompositions() {
	    return new HashMap<>(knownBouquetCompositions);
	}

	public void setKnownBouquetCompositions(Map<String, String> compositions) {
	    this.knownBouquetCompositions = new HashMap<>(compositions);
	}

	public void recordBouquetSale(String signature, String customName, int salePrice) {
	    if (customName != null && !customName.isEmpty()) {
	        Integer currentHigh = bouquetHighScores.get(signature);
	        if (currentHigh == null || salePrice > currentHigh) {
	            bouquetHighScores.put(signature, salePrice);
	            
	            if (!knownBouquetCompositions.containsKey(signature)) {
	                knownBouquetCompositions.put(signature, customName);
	            }
	        }
	    }
	}

	public Integer getBouquetHighScore(String signature) {
	    return bouquetHighScores.get(signature);
	}

	public Map<String, Integer> getBouquetHighScores() {
	    return new HashMap<>(bouquetHighScores);
	}

	public void setBouquetHighScores(Map<String, Integer> highScores) {
	    this.bouquetHighScores = new HashMap<>(highScores);
	}
	
	/**
	 * Checks if player has installed the mulcher upgrade
	 */
	public boolean hasMulcher() {
	    return hasMulcher;
	}

	/**
	 * Sets mulcher installation status
	 */
	public void setHasMulcher(boolean hasMulcher) {
	    this.hasMulcher = hasMulcher;
	}

	/**
	 * Installs the mulcher upgrade
	 */
	public void installMulcher() {
	    this.hasMulcher = true;
	}

	/**
	 * Gets remaining days of mulcher effect
	 */
	public int getMulcherDaysRemaining() {
	    return mulcherDaysRemaining;
	}

	/**
	 * Sets mulcher days remaining
	 */
	public void setMulcherDaysRemaining(int days) {
	    this.mulcherDaysRemaining = Math.max(0, days);
	}

	/**
	 * Activates mulcher effect for 7 days (called when player weeds garden)
	 */
	public void activateMulcherEffect() {
	    if (hasMulcher) {
	        this.mulcherDaysRemaining = 7;
	    }
	}

	/**
	 * Decrements mulcher days (called in advanceDay)
	 */
	public void decrementMulcherDays() {
	    if (mulcherDaysRemaining > 0) {
	        mulcherDaysRemaining--;
	    }
	}

	/**
	 * Checks if mulcher effect is currently active
	 */
	public boolean isMulcherActive() {
	    return hasMulcher && mulcherDaysRemaining > 0;
	}

	/**
	 * Checks if player has installed the sprinkler system
	 */
	public boolean hasSprinklerSystem() {
	    return hasSprinklerSystem;
	}

	/**
	 * Sets sprinkler system installation status
	 */
	public void setHasSprinklerSystem(boolean hasSprinklerSystem) {
	    this.hasSprinklerSystem = hasSprinklerSystem;
	}

	/**
	 * Installs the sprinkler system upgrade
	 */
	public void installSprinklerSystem() {
	    this.hasSprinklerSystem = true;
	}

	public void advanceDay() {
		this.day++;
		this.nrg = 10;
		
		decrementMulcherDays();

		int totalGrew = 0;
		int totalMutated = 0;
		int totalWithered = 0;
		int soilUpgrades = 0;

		for (int i = 0; i < gardenPlots.size(); i++) {
			gardenPlot plot = gardenPlots.get(i);
			String previousSoil = plot.getSoilQuality();

			String stageBefore = plot.isOccupied() ? plot.getPlantedFlower().getGrowthStage() : null;

			plot.advanceDay(this);

			String stageAfter = plot.isOccupied() ? plot.getPlantedFlower().getGrowthStage() : null;

			String newSoil = plot.getSoilQuality();

			if (!previousSoil.equals(newSoil)) {
				soilUpgrades++;
				String plotType = plot.isFlowerPot() ? "flower pot" : "plot #" + (i + 1);
				addJournalEntry("✨ The soil in your " + plotType + " improved from " + 
						previousSoil + " to " + newSoil + "!");
			}

			if (stageBefore != null && stageAfter != null && !stageBefore.equals(stageAfter)) {
				if (stageAfter.equals("Mutated")) {
					totalMutated++;
				} else if (stageAfter.equals("Withered")) {
					totalWithered++;
				} else {
					totalGrew++;
				}
			}
		}

		if (totalGrew > 0) {
			if (totalGrew == 1) {
				addJournalEntry("ðŸŒ± 1 plant grew overnight!");
			} else {
				addJournalEntry("ðŸŒ± " + totalGrew + " plants grew overnight!");
			}
		}

		if (totalMutated > 0) {
			if (totalMutated == 1) {
				addJournalEntry("✨ 1 plant mutated into something special!");
			} else {
				addJournalEntry("✨ " + totalMutated + " plants mutated into something special!");
			}
		}

		if (totalWithered > 0) {
			if (totalWithered == 1) {
				addJournalEntry("ðŸ¥€ 1 plant withered overnight.");
			} else {
				addJournalEntry("ðŸ¥€ " + totalWithered + " plants withered overnight.");
			}
		}

		if (auctionHouse.hasActiveAuction()) {
		    String bidResult = auctionHouse.processDailyBid(this.day, this);
		    if (bidResult != null) {
		        addJournalEntry(bidResult);
		    }
		}

		boolean needsWater = false;
		boolean needsWeeding = false;

		for (gardenPlot plot : gardenPlots) {
			if (plot.isOccupied() && !plot.isWatered()) {
				needsWater = true;
			}
			if (!plot.isFlowerPot() && !plot.isWeeded()) {
				needsWeeding = true;
			}
		}

		if (needsWater) {
			addJournalEntry("ðŸ’§ Your plants need watering!");
		}
		if (needsWeeding) {
			addJournalEntry("ðŸŒ¿ Some weeds appeared in the garden.");
		}
	}
}
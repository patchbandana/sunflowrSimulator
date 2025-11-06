/* FlowerInstance.java
 * (Formerly MammothSunflower.java)
 * 
 * Generic flower instance class used by the CSV database system.
 * All flowers loaded from flowers.csv are instantiated as FlowerInstance objects.
 * 
 * NOTE: This class extends Flower and adds the NRGRestored property.
 * The name "MammothSunflower" was kept in the original implementation but was 
 * misleading since it represents ALL flower types (roses, tulips, etc.)
 */

/**
 * The FlowerInstance class creates flower objects with stats loaded from the CSV database.
 * These flowers can be planted, grown, eaten, or sold.
 * Stats are accessed through journal, check, or backpack interfaces.
 */
public class FlowerInstance extends Flower {
	private int NRGRestored;
	
	/**
	 * Constructs a new FlowerInstance object
	 * @param name of the flower (loaded from CSV database)
	 * @param growthStage "Seed", "Seedling", "Bloomed", "Matured", "Withered", or "Mutated"
	 * @param daysPlanted increments one day at a time, initializes at 0 for seeds
	 * @param durability decrements with negative events
	 * @param NRGRestored amount of energy restored when eaten (typically only in Seed form)
	 * @param cost when bought or sold in various forms
	 */
	public FlowerInstance(String name, String growthStage, int daysPlanted, double durability, int NRGRestored, double cost) {
		super(name, growthStage, daysPlanted, durability, cost);
		setNRGRestored(NRGRestored);
	}

	/**
	 * Returns the amount of NRG restored when this flower is eaten
	 * @return the NRG restored (with input validation)
	 */
	public int getNRGRestored() {
		return NRGRestored;
	}
	
	/**
	 * Sets the amount of NRG a seed will restore when eaten
	 * @param NRGRestored the NRG amount to set
	 */
	public void setNRGRestored(int NRGRestored) {
		this.NRGRestored = NRGRestored;
	}

	/**
	 * Checks if this flower is currently planted in the garden
	 * @return true if planted (daysPlanted > 0), false otherwise
	 */
	public boolean isPlanted() {
		return getDaysPlanted() > 0;
	}
	
	/**
	 * Returns a user-friendly string representation of this flower.
	 * Displays the actual flower name and current growth stage with appropriate emoji.
	 * 
	 * @return formatted string with emoji, name, and growth stage
	 */
	@Override
	public String toString() {
		// Get the actual name and growth stage from the parent Flower class
		String name = getName();
		String stage = getGrowthStage();
		
		// Choose emoji based on growth stage for visual feedback
		String emoji = "🌻"; // Default sunflower emoji
		
		switch (stage) {
			case "Seed":
				emoji = "🌱"; // Seedling emoji for seeds
				break;
			case "Seedling":
				emoji = "🌿"; // Young plant emoji
				break;
			case "Bloomed":
				emoji = "🌸"; // Blossom emoji
				break;
			case "Matured":
				emoji = "🌻"; // Full sunflower emoji
				break;
			case "Withered":
				emoji = "🥀"; // Wilted flower emoji
				break;
			case "Mutated":
				emoji = "✨"; // Sparkle emoji for mutations
				break;
		}
		
		// Return the formatted string with name and stage
		return emoji + " " + name + " (" + stage + ")";
	}
}
/* Pat Eizenga
 * 2024-07-09
 * Description: MammothSunflower class. The first seed you start with. WIP
 * 
 * BUG FIX: toString() now properly displays the flower's name and current growth stage
 * instead of always showing "(Seed)"
 */


/**
 * The MammothSunflower class will create MammothSunflower objects, each holding different stats
 * as the game progresses. Initially in inventory, they can be planted and grown, eaten, or sold.
 * A variety of stats will be needed for each MammothSunflower. Accessed through journal, check, 
 * or backpack.
 */
public class MammothSunflower extends Flower {
	private int NRGRestored;
	
	/**Constructs a new MammothSunflower object
	 * @param name of the sunflower "Mammoth Sunflower for now WIP 7/27/2024
	 * @param growthStage "Seed", "Seedling", "Bloomed", "Matured", or "Withered"
	 * @param daysPlanted increments one day at a time, initializes at 1
	 * @param durability decrements with negative events, very sturdy flower
	 * @param nRGRestored only when in "Seed" form
	 * @param cost when bought or sold in various forms
	 */
	public MammothSunflower(String name, String growthStage, int daysPlanted, double durability, int NRGRestored, double cost) {
		super(name, growthStage, daysPlanted, durability, cost);
		setNRGRestored(NRGRestored);
	}


	/**Returns an NRG amount restored for Player1 when eaten in Seed form
	 * @return the nRGRestored with input validation
	 */
	public int getNRGRestored() {
		return NRGRestored;
	}
	/**Sets amount of NRG a seed will restore when eaten. Shouldn't change much
	 * @param nRGRestored the nRGRestored to set
	 */
	public void setNRGRestored(int NRGRestored) {
		this.NRGRestored = NRGRestored;
	}
	/**Returns the cost of the sunflower in different forms. (Seed, seedling, bloomed, matured, withered. And buy or sell)
	 * @return the cost of this particular MammothSunflower
	 */

	/**Returns true if the seed is planted.
	 * @return true if seed is planted, return false if not planted WIP 7/27/2024
	 */
	public boolean isPlanted() {
		if (getDaysPlanted() > 0)
		{
			return true;
		}
		else 
			return false;	
	}
	
	/**
	 * BUG FIX: toString() method now properly displays the flower's actual name and growth stage
	 * 
	 * Previously this was hardcoded to always show "🌻 Mammoth Sunflower (Seed)" which meant:
	 * - Rose seeds showed as "Mammoth Sunflower (Seed)"
	 * - Bloomed flowers showed as "(Seed)" 
	 * - Matured flowers showed as "(Seed)"
	 * 
	 * Now it dynamically displays:
	 * - The actual flower name (from getName())
	 * - The current growth stage (from getGrowthStage())
	 * - Different emoji based on growth stage for visual clarity
	 * 
	 * @return a string representation of this flower with correct name and stage
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
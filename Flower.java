/* Pat Eizenga
 * 2024-27-07
 * Description: The abstract flower class from which all flowers will extend.
 * Encapsulates the general idea of a flower.
 * Flowers will inherit code from this superclass for use in future/smaller classes
 */
package sunflowrSimulator;

/**
* The Flower abstract class will encapsulate Flower objects, each holding different stats
* as the game progresses. Initially in inventory, they can be planted and grown, eaten, or sold.
* A variety of stats will be needed for each Flower. Accessed through journal, check, 
* or backpack.
*/
public abstract class Flower {
	/**Every flower gets a name*/
	private String name = "";
	/**The growthStage of the Flower, starting with "Seed"*/
	private String growthStage = "";
	/**The daysPlanted of this particular Flower*/
	private int daysPlanted;
	/**The durability of this particular Flower, decrements until 0 and then destroys this Flower*/
	private double durability;
	/**Cost of a Flower, modified by form*/
	private double cost;
	
	/**Constructs a new Flower object
	 * @param growthStage "Seed", "Seedling", "Bloomed", "Matured", "Withered", or "Mutated"
	 * @param daysPlanted increments one day at a time, initializes at 1
	 * @param durability decrements with negative events, very sturdy flower
	 * @param cost when bought or sold in various forms
	 */
	public Flower(String name, String growthStage, int daysPlanted, double durability, double cost) {
		setName(name);
		setGrowthStage(growthStage);
		setDaysPlanted(daysPlanted);
		setDurability(durability);
		setCost(cost);
	}
	
	
	
	/**The name of this particular Flower object
	 * @return the name of this flower
	 */
	public String getName() {
		return name;
	}

	/**Sets the name of the flower. Shouldn't change much. 
	 * @param name of this particular flower
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**Returns the growthStage of this particular Flower
	 * @return the growthStage when called
	 */
	public String getGrowthStage() {
		return growthStage;
	}
	
	/**Sets growthStage at certain intervals.
	 * @param growthStage the growthStage to set
	 */
	public void setGrowthStage(String growthStage) {
		this.growthStage = growthStage;
	}
	/**Returns the daysPlanted of this particular Flower
	 * @return the daysPlanted when checked in garden plot
	 */
	public int getDaysPlanted() {
		return daysPlanted;
	}
	/**Sets the daysPlanted of this particular Flower
	 * @param daysPlanted the daysPlanted to set increments when going to bed
	 */
	public void setDaysPlanted(int daysPlanted) {
		this.daysPlanted = daysPlanted;
	}
	/**Returns the current durability of this particular Flower
	 * @return the durability of this flower, decrements until 0 is reached
	 */
	public double getDurability() {
		return durability;
	}
	/**Sets the durability of this particular Flower
	 * @param durability the durability to set decrements upon negative events
	 */
	public void setDurability(double durability) {
		this.durability = durability;
	}
	
	/**Returns the cost of the flower in different forms. (Seed, seedling, bloomed, matured, withered, mutated; Buy/Sell)
	 * @return the cost of this particular flower
	 */
	public double getCost() {
		return cost;
	}
	/**Sets the cost of the flower depending on form
	 * @param cost the cost to set can also be influenced by items/gardening level
	 */
	public void setCost(double cost) {
		this.cost = cost;
	}
	
	/**Returns a user-friendly String representing the flower
	 * 
	 * @return the user-friendly String representing the flower
	 */
	@Override 
	public String toString() {
		String returnString = "\n" + getName() + "\n" + getGrowthStage()
			+ "\nDurability: " + getDurability() + "\nValue: " + getCost() + "\n";
		return returnString;
}
}
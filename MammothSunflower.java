/* Pat Eizenga
 * 2024-09-07
 * Description: MammothSunflower class. The first seed you start with. WIP
 * 
 */
package sunflowerSimulator;

/**
 * The MammothSunflower class will create MammothSunflower objects, each holding different stats
 * as the game progresses. Initially in inventory, they can be planted and grown, eaten, or sold.
 * A variety of stats will be needed for each MammothSunflower. Accessed through journal, check, 
 * or backpack.
 */
public class MammothSunflower {
	/**The growthStage of the MammothSunflower, starting with "Seed"*/
	private String growthStage;
	/**The daysPlanted of this particular MammothSunflower*/
	private int daysPlanted;
	/**The durability of this particular MammothSunflower, decrements until 0 and then destroys this MammothSunflower*/
	private double durability;
	/**Players can choose to eat sunflower seeds (only sunflowers, not other seed types and only while in "Seed" form)*/
	private int NRGRestored;
	/**Cost of a MammothSunflower, modified by form*/
	private double cost;
	
	
	
	/**Constructs a new MammothSunflower object
	 * @param growthStage "Seed", "Seedling", "Bloomed", "Matured", or "Withered"
	 * @param daysPlanted increments one day at a time, initializes at 1
	 * @param durability decrements with negative events, very sturdy flower
	 * @param nRGRestored only when in "Seed" form
	 * @param cost when bought or sold in various forms
	 */
	public MammothSunflower(String growthStage, int daysPlanted, double durability, int NRGRestored, double cost) {
		setGrowthStage(growthStage);
		setDaysPlanted(daysPlanted);
		setDurability(durability);
		setNRGRestored(NRGRestored);
		setCost(cost);
	}

	/**Returns the growthStage of this particular MammothSunflower
	 * @return the growthStage when called
	 */
	public String getGrowthStage() {
		return growthStage;
	}
	
	/**Sets growthStage at certain intervals. Easiest flower.
	 * @param growthStage the growthStage to set
	 */
	public void setGrowthStage(String growthStage) {
		this.growthStage = growthStage;
	}
	/**Returns the daysPlanted of this particular MammothSunflower
	 * @return the daysPlanted when checked in garden plot
	 */
	public int getDaysPlanted() {
		return daysPlanted;
	}
	/**Sets the daysPlanted of this particular MammothSunflower
	 * @param daysPlanted the daysPlanted to set increments when going to bed
	 */
	public void setDaysPlanted(int daysPlanted) {
		this.daysPlanted = daysPlanted;
	}
	/**Returns the current durability of this particular MammothSunflower
	 * @return the durability of this sunflower, decrements until 0 is reached
	 */
	public double getDurability() {
		return durability;
	}
	/**Sets the durability of this particular MammothSunflower
	 * @param durability the durability to set decrements upon negative events
	 */
	public void setDurability(double durability) {
		this.durability = durability;
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
	public double getCost() {
		return cost;
	}
	/**Sets the cost of the sunflower depending on form/
	 * @param cost the cost to set can also be influenced by items/gardening level
	 */
	public void setCost(double cost) {
		this.cost = cost;
	}
	
	/**Returns true if the seed is planted.
	 * @return true if seed is planted, return false if not planted
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
	 * To string method WIP, for now just displays every method we have so far in a legible way
	 * @return a string representation of this mammoth sunflower
	 */
	@Override
	public String toString() {
		return "\nMammoth Sunflower " + getGrowthStage() + ":\nDays Planted: " + getDaysPlanted()
				+ "\nDurability: " + getDurability() + "\nNRG Restored: " + getNRGRestored() +
				"\nCost: " + getCost() + "\n";
	}
	
}

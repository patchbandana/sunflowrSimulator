/* Pat Eizenga
 * 2023-19-06
 * Description: Player Save File for manipulating the player's stats and details
 * 
 */
package sunflowerSimulator;

/**
 * The Player1 class will contain all ways of manipulating and remembering the player's changes
 */
public class Player1 {
	
	/**The Player1 name, set by the user*/
	private String playerName;
	/**The day of this game, increments when you go to bed*/
	private int day;
	/**The current amount of energy the player has*/
	private double NRG;
	/**The maximum NRG, affected by Gardening level or equipment*/
	private int nrgMax;
	/**The Player1 name, set by the user*/
	private double credits;
	
	/**Constructor of Player1 with only PlayerName 
	 * @param playerName passed as an argument from user
	 */
	public Player1(String playerName) {
		setPlayerName(playerName); 
	}

	/**Constructor of Player1 with *all* modifiers
	 * @param playerName passed as an argument from user or loaded from save file
	 * @param day intitialized to 0 or loaded from save file
	 * @param NRG initialized to 10.0 or loaded from save file
	 * @param nrgMax initialized to 10.0 or loaded from save file
	 * @param credits intitialized to 100.0 or loaded from save file
	 */
	public Player1(String playerName, int day, double NRG, int nrgMax, double credits) {
		setPlayerName(playerName);
		setDay(day);
		setNRG(NRG);
		setNrgMax(nrgMax);
		setCredits(credits);
	}

	/**Gets the Player's name
	 * @return the Player's name after being initialized
	 */
	public String getPlayerName() {
		return playerName;
	}
	
	/**Sets the player's name
	 * @param playerName the player's name
	 */
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
		if (this.playerName.equalsIgnoreCase("Noah"))
		{
			this.playerName = "Baby Doll";
		}
	}
	
	/**Gets the day of this save file
	 * @return the day of this save file
	 */
	public int getDay() {
		return day;
	}
	
	/**Sets the day of the save file
	 * @param day sets day
	 */
	public void setDay(int day) {
		this.day = day;
	}
	
	/**Gets the current NRG amount
	 * @return NRG current
	 */
	public double getNRG() {
		return NRG;
	}
	
	/**Sets the current NRG amount
	 * @param NRG is set
	 */
	public void setNRG(double NRG) {
		this.NRG = NRG;
	}
	
	/**Gets the maximum NRG for Player1
	 * @return NrgMax for Player1
	 */
	public int getNrgMax() {
		return nrgMax;
	}
	
	/**Sets the maximum NRG for Player1
	 * @param NrgMax for Player1
	 */
	public void setNrgMax(int nrgMax) {
		this.nrgMax = nrgMax;
	}
	
	/**Gets the credits the player currently possesses
	 * @return Credits for Player1
	 */
	public double getCredits() {
		return credits;
	}
	
	/**Sets the amount of credits the player currently possesses
	 * @param Credits for Player1
	 */
	public void setCredits(double credits) {
		this.credits = credits;
	}

	/**Returns a string representation of Player1 current info
	 * 
	 * @return a string with playerinfo
	 */
	@Override
	public String toString() {
		return "\n" + getPlayerName() + "\nNRG: " + getNRG() + "\nCredits: "
				+ getCredits() + "\nDay: " + getDay();
	}
	
	
}

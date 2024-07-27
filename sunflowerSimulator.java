/* Creator: Pat Eizenga
 * Created: 6/18/2024
 * Last Updated: 7/27/2024
 * Project: Open source, open dialog, gardening game developed with love, focus and dreams.
 * */

package sunflowerSimulator;

import java.util.Random;
import java.util.Scanner;

import typesOfFlowers.*;

import java.util.ArrayList;

/**
 * Main game executed here.
 */
public class sunflowerSimulator {

	/**
	 * Main game.
	 * Day 0 notes: We'll start with walking through a newgame/tutorial mode to flesh out ideas.
	 * Day 1 notes: looping logic for action menu, create a class.
	 * Day 2 notes: write a few dreams in txt files, finish up accessor/mutator methods, get main to run
	 * Day 3 notes: player1 class toString method, 2nd action menu choice
	 * Day 4 notes: shop menu, 5 more dream.txt files, 3rd action menu choice (backpack), sunflower class?
	 * Day 5 notes: backpack menu, iterating across arraylist review, mammothsunflower forms
	 * Day 6 notes: the abstract class (one class extended) 7/24/2024
	 * 
	 * @param args NOT USED
	 */
	public static void main(String[] args) {
		//Tutorial and introductions
		System.out.println("Hello World! I'm the sunflowerSimulator. But we'll be planting lots of "
				+ "things together! ^-^");
		//Declare a scanner for user input
		Scanner scanner = new Scanner(System.in);
		System.out.print("What's your name? ");
		//Create a new Player1 object with name from user
		Player1 player = new Player1(scanner.nextLine(), 0, 10, 10, 100);
		System.out.println("Hey there, " + player.getPlayerName() + "! It's a pleasure to meet you. :) \n"
				+ "Let's get started with some of the basics!");
		player.setNRG(10);
		System.out.println("Each day, you will start with a certain amount of NRG. \n" +
				player);
		
		//NRG (NeverReallyGone) introduced and initialized to double 10, credits initialized to 100
		System.out.println("Spend your energy wisely! When you run out you will need to go to bed and that means no more gardening"
				+ " for the day. :(");
		System.out.println("Enter the number from the menu for which action you'd like to take: ");
		
		//Introduction to the action menu (review switch statements)
		char actionMenuChoice;
		char shopMenuChoice;
		String choice = "N";
		
		do
		{
		System.out.print("1: Weed Garden \n");
		System.out.print("2. Water Garden \n");
		System.out.print("3. Plant \n");
		System.out.print("4. Build \n");
		System.out.print("5. Shop \n");
		System.out.print("6. Backpack \n");
		System.out.print("7. Trim Plants \n");
		System.out.print("8. Check \n");
		System.out.print("9. Journal \n");
		System.out.print("0. Go to bed \n");
		
		
		//Take choice from user into branch
		actionMenuChoice = scanner.next().charAt(0);
		switch(actionMenuChoice) {
		case '0':
			//Increment day at sleep and roll chance of dream
			System.out.print("Going to bed will restore NRG and prepare you for the next day. Are you ready? (Y/N?)");
			choice = scanner.next();
			if (choice.charAt(0) == 'Y' || choice.charAt(0) == 'y')
			{
				player.setDay(player.getDay() + 1);
				//Roll chance of dream here (ADD LATER)
				//10 and counting
				
				Random random = new Random();
				int chanceOfDreamRoll = random.nextInt(100);
				//DreamRoll tester: comment out
				//System.out.print(chanceOfDreamRoll);
				
				if (chanceOfDreamRoll > 50)
				{
					//Dream event pull, placeholder indicator/comment out
					System.out.print("Dream! \n");
				} //nested
				else
				{
					System.out.println("You wake up feeling refreshed and rested. It's a new day! :D \n");
					player.setNRG(player.getNrgMax());
				}
				System.out.print("It is now day " + player.getDay() + ". \n");
			} //end confirmation screen if
			break;
		case '5':
			//Introduce shop and credits
			System.out.println("I see you like shopping! Let's get you started with some funds."
					+ "\nCredits: " + player.getCredits());
			System.out.println("Merchandise rotates based on day and your gardening level so check often!");
			
			
			//Shop Menu display, WIP 6/27/2024
			//Add random chance of 4 items
			//Deduct cost from credits
			do
			{
			System.out.print("0. Exit Shop\n");
			System.out.print("1. Item 1 [Placeholder]\n");
			System.out.print("2. Item 2\n");
			System.out.print("3. Item 3\n");
			System.out.print("4. Item 4\n");
			
			shopMenuChoice = scanner.next().charAt(0);
			
				switch (shopMenuChoice) {
				//Take choice from user into branch
				case '0': 
					break;
				//Placeholder price modifications. When cost is added to item classes, change to implement credit deductions
				case '1':
					System.out.println("Item " + shopMenuChoice + " purchased! ");
					System.out.println("Credits: " + (player.getCredits() - 5));
					break;
				case '2':
					System.out.println("Item " + shopMenuChoice + " purchased! ");
					System.out.println("Credits: " + (player.getCredits() - 10));
					break;
				case '3':
					System.out.println("Item " + shopMenuChoice + " purchased! ");
					System.out.println("Credits: " + (player.getCredits() - 15));
					break;
				case '4':
					System.out.println("Item " + shopMenuChoice + " purchased! ");
					System.out.println("Credits: " + (player.getCredits() - 20));
					break;
				default:
					System.out.println("Invalid choice! Try again, silly. :)");
				} //end shop menu switch
			} //end do-while loop for shop menu
			while (shopMenuChoice != '0');
		break;
		case '6':
			System.out.println("You decide to take a look-see in your standard grey bag.");
			System.out.println("Looks like there's something inside! Check your backpack to see what " +
					"you're working with.");
			char backpackChoice = '1';
			do {
			//Create a new arraylist for inventory and add tutorial sunflower.
				MammothSunflower mammothSunflower1 = new MammothSunflower("MammothSunflower", "Seed", 0, 10, 1, 5);
				ArrayList<Object> inventory = new ArrayList<>();
				inventory.add(mammothSunflower1);
				inventory.add(new MammothSunflower("Mammoth Sunflower", "Sapling", 2, 9, 0, 6));
				inventory.add(new MammothSunflower("Mammoth Sunflower", "Bloomed", 4, 8.33, 0, 35));
				inventory.add(new MammothSunflower("Mammoth Sunflower", "Matured", 5, 7, 0, 50));
				inventory.add(new MammothSunflower("Mammoth Sunflower", "Withered", 10, 0, 0, 1));
				inventory.add(new MammothSunflower("Mammoth Sunflower", "Mutated", 7, 20, 0, 200));
				System.out.println(inventory);
				backpackChoice = '0';
			//Display contents of bag, for loop through array of inventory items, return to menu
			}
			while (backpackChoice != '0');
			break;
		default:
			System.out.println("Invalid choice! Try again, silly. :)");
		} //end action menu switch
		 //end do-while loop 
		}
		while (actionMenuChoice != '0' || (choice.charAt(0) == 'N' || choice.charAt(0) == 'n'));
		//Loop currently terminates when going to bed and progressing to the next day 7/9/2024
		//Close the scanner
		scanner.close();
		
		}
	}

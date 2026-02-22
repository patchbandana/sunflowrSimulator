/* Creator: Pat Eizenga
 * Created: June 18, 2024
 * Last Updated: November 25, 2025
 * Project: Open source gardening game developed with love, focus and dreams
 * UPDATED: Fixed weather to occur independently of dreams/hints (25% chance always)
 */

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class sunflowerSimulator {

	public static void main(String[] args) {
		System.out.println("🌻 Welcome to Sunflower Simulator! 🌻");
		System.out.println("A gardening game developed with love, focus, and dreams.\n");

		Scanner scanner = new Scanner(System.in);

		System.out.print("Please enter your name: ");
		String playerName = scanner.nextLine();

		Player1 player;
		boolean newGame = !Journal.saveExists(playerName);

		FlowerRegistry.loadFlowerData();
		DreamReader.loadDreamFiles();
		HintReader.loadHintFiles();

		if (newGame) {
			player = new Player1(playerName);

			System.out.println("\nHey there, " + player.getName() + "! It's a pleasure to meet you. :)");
			System.out.println("Let's get started with some of the basics!");
			System.out.println("Each day, you will start with " + player.getNRG() + " NRG.");
			System.out.println("Spend your energy wisely! When you run out you will need to go to bed.");
			System.out.println("You start with " + player.getCredits() + " credits to buy seeds.");

			FlowerInstance starterSeed = new FlowerInstance(
					"Mammoth Sunflower", "Seed", 0, 10, 1, 5);
			player.addToInventory(starterSeed);
			System.out.println("\nYou've been given a Mammoth Sunflower seed to start your garden!");

			Journal.saveGame(player);
			Journal.addJournalEntry(player, "Started my gardening adventure!");
		} else {
			player = Journal.loadGame(playerName);
			System.out.println("\nWelcome back, " + player.getName() + "!");
			System.out.println("Your game has been loaded from day " + player.getDay() + ".");
			System.out.println("You have " + player.getNRG() + " NRG and " + player.getCredits() + " credits.");
			Journal.addJournalEntry(player, "Resumed my gardening adventure.");
		}

		boolean gameContinues = true;

		do {
			System.out.println("\n=== Day " + player.getDay() + " ===");
			System.out.println("NRG: " + player.getNRG() + " | Credits: " + player.getCredits());
			System.out.println("\nWhat would you like to do?");
			System.out.println("1: Weed Garden");
			System.out.println("2: Water Garden");
			System.out.println("3: Plant");
			System.out.println("4: Build");
			System.out.println("5: Shop");
			System.out.println("6: Backpack");
			if (player.hasBuzzsaw()) {
				System.out.println("7: Trim All Plants");
			} else {
				System.out.println("7: Trim Plants");
			}
			System.out.println("8: Check");
			System.out.println("9: Journal");
			System.out.println("0: Go to bed");
			System.out.println("X: Save & Exit Game");

			System.out.print("\nEnter your choice: ");
			String actionMenuChoice = scanner.next();
			scanner.nextLine();

			switch(actionMenuChoice.toUpperCase()) {

			case "0":
				handleBedtimeMenu(player, scanner);
				break;

			case "1":
				GardenActions.weedGarden(player);
				break;

			case "2":
				GardenActions.waterGarden(player);
				break;

			case "3":
				PlantingActions.handlePlanting(player, scanner);
				break;

			case "4":
				BuildingActions.handleBuildMenu(player, scanner);
				break;

			case "5":
				ShopActions.handleShop(player, scanner);
				break;

			case "6":
				BackpackActions.displayBackpack(player, scanner);
				break;

			case "7":
				TrimmingActions.handleTrimming(player, scanner);
				break;

			case "8":
				GardenCheckActions.handleGardenCheck(player, scanner);
				break;

			case "9":
				player = JournalActions.handleJournal(player, scanner);
				break;

			case "X":
				System.out.println("Saving game and exiting...");
				Journal.addJournalEntry(player, "Ended gardening session on day " + player.getDay() + ".");
				if (Journal.saveGame(player)) {
					System.out.println("Game saved successfully. Thanks for playing!");
				} else {
					System.out.println("Warning: There was an issue saving the game.");
					System.out.println("Exiting anyway. Thanks for playing!");
				}
				gameContinues = false;
				break;

			default:
				System.out.println("Invalid choice! Please try again.");
				break;
			}

			if (player.getNRG() <= 0 && gameContinues) {
				System.out.println("\nYou've run out of energy! You need to go to bed (option 0) or save & exit (option X).");
			}

		} while (gameContinues);

		scanner.close();
	}

	private static void handleBedtimeMenu(Player1 player, Scanner scanner) {
		System.out.println("What would you like to do?");
		System.out.println("1: Go to bed (save & continue)");
		System.out.println("2: Save & exit game");

		System.out.print("\nEnter your choice: ");
		String bedChoice = scanner.next();
		scanner.nextLine();

		switch (bedChoice) {
		case "1":
			advanceDay(player);
			break;

		case "2":
			System.out.println("Saving game and exiting...");
			Journal.saveGame(player);
			Journal.addJournalEntry(player, "Ended gardening session on day " + player.getDay() + ".");
			System.out.println("Game saved successfully. Thanks for playing!");
			System.exit(0);
			break;

		default:
			System.out.println("Invalid choice. Please select 1 or 2.");
		}
	}

	private static void advanceDay(Player1 player) {
		Journal.saveGame(player);

		System.out.println("\n💤 You drift off to sleep...");

		String dreamContent = null;
		String dreamFilename = null;
		boolean showedHint = false;
		boolean weatherOccurred = false;

		// WEATHER SYSTEM CHECK - Independent 25% chance
		weatherOccurred = WeatherSystem.shouldWeatherOccur();

		// Special hint for day 30+ if player hasn't built extra plot
		if (!weatherOccurred && player.getDay() >= 30 && !player.hasBuiltExtraPlot() && HintReader.hasHints()) {
			if (Math.random() < 0.5) {
				dreamContent = HintReader.getSpecificHint("build_expansion.txt");
				if (dreamContent == null) {
					String[] hintData = HintReader.getRandomHintWithFilename(); 
					if (hintData != null) {
						dreamContent = hintData[1];
					}
				}
				showedHint = true;
			}
		}

		// Get all available dream files
		List<String> allDreamFiles = new ArrayList<>();
		try {
			File dreamDir = new File("dream.txt/");
			if (dreamDir.exists() && dreamDir.isDirectory()) {
				File[] files = dreamDir.listFiles((dir, name) -> name.endsWith(".txt"));
				if (files != null) {
					for (File file : files) {
						allDreamFiles.add(file.getName());
					}
				}
			}
		} catch (Exception e) {
			// Silent fail
		}
		
		// Filter out already-unlocked dreams
		List<String> unviewedDreams = new ArrayList<>();
		for (String dreamFile : allDreamFiles) {
			if (!player.hasDreamUnlocked(dreamFile)) {
				unviewedDreams.add(dreamFile);
			}
		}
		
		// Get all available hint files
		List<String> allHintFiles = new ArrayList<>();
		try {
			File hintDir = new File("hints.txt/");
			if (hintDir.exists() && hintDir.isDirectory()) {
				File[] files = hintDir.listFiles((dir, name) -> name.endsWith(".txt"));
				if (files != null) {
					for (File file : files) {
						allHintFiles.add(file.getName());
					}
				}
			}
		} catch (Exception e) {
			// Silent fail
		}
		
		// Filter out already-unlocked hints
		List<String> unviewedHints = new ArrayList<>();
		for (String hintFile : allHintFiles) {
			if (!player.hasHintUnlocked(hintFile)) {
				unviewedHints.add(hintFile);
			}
		}
		
		// Dreams/hints can still occur even if weather happens
		if (dreamContent == null) {
			// 25% chance of dream/hint if any are available
			if ((!unviewedDreams.isEmpty() || !unviewedHints.isEmpty()) && Math.random() < 0.25) {
				boolean tryHint = false;
				
				// Day 20+: 50/50 between hints and dreams
				if (player.getDay() >= 20 && !unviewedHints.isEmpty() && !unviewedDreams.isEmpty()) {
					tryHint = Math.random() < 0.5;
				} else if (player.getDay() >= 20 && !unviewedHints.isEmpty() && unviewedDreams.isEmpty()) {
					tryHint = true; // Only hints left
				} else if (unviewedDreams.isEmpty() && !unviewedHints.isEmpty()) {
					tryHint = true; // Only hints left
				}
				// else: Only dreams available or before day 20
				
				if (tryHint && !unviewedHints.isEmpty()) {
					// Show a random unviewed hint
					String selectedHintFile = unviewedHints.get((int)(Math.random() * unviewedHints.size()));
					dreamContent = HintReader.readHintFile(selectedHintFile);
					if (dreamContent != null) {
						player.unlockHint(selectedHintFile);
						showedHint = true;
					}
				} else if (!unviewedDreams.isEmpty()) {
					// Show a random unviewed dream
					dreamFilename = unviewedDreams.get((int)(Math.random() * unviewedDreams.size()));
					try (BufferedReader reader = new BufferedReader(
							new FileReader("dream.txt/" + dreamFilename, java.nio.charset.StandardCharsets.UTF_8))) {
						StringBuilder dream = new StringBuilder();
						String line;
						while ((line = reader.readLine()) != null) {
							dream.append(line).append("\n");
						}
						dreamContent = dream.toString().trim();
					} catch (IOException e) {
						// Silent fail
					}
				}
			}
		}

		// Display weather FIRST if it occurred
		if (weatherOccurred) {
			String weatherDesc = WeatherSystem.getWeatherDescription();
			if (weatherDesc != null) {
				System.out.println("\n" + weatherDesc);
			}
		}
		
		// Then display dream/hint if applicable
		if (dreamContent != null) {
			System.out.println("\n✨ You had a strange dream...\n");
			System.out.println("╔═══════════════════════════════════════╗");
			System.out.println(dreamContent);
			System.out.println("╚═══════════════════════════════════════╝");

			if (showedHint) {
				System.out.println("\nYou wake up feeling thoughtful about your garden's potential.");
				Journal.addJournalEntry(player, "Had a dream with helpful advice.");
			} else {
				System.out.println("\nYou wake up feeling refreshed!");

				if (dreamFilename != null) {
					player.unlockDream(dreamFilename);
					Journal.addJournalEntry(player, "Had a vivid dream tonight.");
				}
			}
		} else if (!weatherOccurred) {
			// Only show this if no weather AND no dream
			System.out.println("\nYou slept soundly through the night. It's a new day! :D");
			Journal.addJournalEntry(player, "Slept soundly through the night.");
		}

		// Advance day (plants grow, daily state resets)
		player.advanceDay();
		
		// Apply weather effects AFTER plants have grown
		if (weatherOccurred) {
			WeatherSystem.applyWeatherEffects(player);
		}
		
		ShopActions.resetShopInventory(); 

		System.out.println("\n🌅 Day " + player.getDay() + " begins.");
		System.out.println("You feel refreshed! (NRG restored to " + player.getNRG() + ")");

		displayGardenSummary(player, weatherOccurred);
	}

	private static void displayGardenSummary(Player1 player, boolean weatherOccurred) {
		final String[] SUMMARY_KEYWORDS = {
				"grew overnight!", 
				"mutated into something special!", 
				"withered overnight.",
				"watered", // Weather-related
				"damaged", // Weather-related
				"prevented", // Weather-related
				"earthquake", // Weather-related
				"hurricane", // Weather-related
				"Moles", // Weather-related
				"fairies" // Weather-related
		};

		final Map<String, String> CONSOLE_EMOJI_MAP = new HashMap<>();
		CONSOLE_EMOJI_MAP.put("grew overnight!", "✓");
		CONSOLE_EMOJI_MAP.put("mutated into something special!", "✨");
		CONSOLE_EMOJI_MAP.put("withered overnight.", "⚠");
		CONSOLE_EMOJI_MAP.put("watered", "💧");
		CONSOLE_EMOJI_MAP.put("damaged", "⚡");
		CONSOLE_EMOJI_MAP.put("prevented", "❄️");
		CONSOLE_EMOJI_MAP.put("earthquake", "🌋");
		CONSOLE_EMOJI_MAP.put("hurricane", "🌀");
		CONSOLE_EMOJI_MAP.put("Moles", "🐭");
		CONSOLE_EMOJI_MAP.put("fairies", "🧚");

		List<String> summaryMessages = new ArrayList<>();
		List<String> allEntries = player.getJournalEntries();

		int totalEntries = allEntries.size();
		int checkStart = Math.max(0, totalEntries - 15); 

		for (int i = totalEntries - 1; i >= checkStart; i--) {
			String entry = allEntries.get(i);

			String matchingKeyword = null;
			for (String keyword : SUMMARY_KEYWORDS) {
				if (entry.contains(keyword)) {
					matchingKeyword = keyword;
					break;
				}
			}

			if (matchingKeyword != null) {
				int contentStart = entry.lastIndexOf(':') + 2;
				if (contentStart < entry.length()) {
					String message = entry.substring(contentStart).trim();

					int firstDigitIndex = -1;
					for (int k = 0; k < message.length(); k++) {
						if (Character.isDigit(message.charAt(k))) {
							firstDigitIndex = k;
							break;
						}
					}

					String consoleEmoji = CONSOLE_EMOJI_MAP.get(matchingKeyword);
					String bodyMessage = "";

					if (firstDigitIndex != -1) {
						bodyMessage = message.substring(firstDigitIndex);
					} else {
						bodyMessage = message;
					}

					// Only add emoji if we have one
					if (consoleEmoji != null) {
						message = consoleEmoji + " " + bodyMessage;
					} else {
						message = bodyMessage;
					}

					summaryMessages.add(message);
				}
			} else {
				if (entry.contains("Slept soundly") || entry.contains("vivid dream")) {
					break;
				}
			}
		}

		if (!summaryMessages.isEmpty()) {
			System.out.println("\n🌱 Garden Update:");

			for (int i = summaryMessages.size() - 1; i >= 0; i--) {
				System.out.println("  " + summaryMessages.get(i));
			}
		}
		
		// Show weather summary if applicable
		if (weatherOccurred) {
			String weatherSummary = WeatherSystem.getWeatherSummary();
			if (weatherSummary != null) {
				System.out.println("\n🌤️ Weather: " + weatherSummary);
			}
		}
	}
}
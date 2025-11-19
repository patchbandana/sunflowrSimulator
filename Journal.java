/* Journal class for handling save/load functionality
 * Added to sunflowrSimulator package
 * * FINAL FIXES:
 * - Ensures list pruning to MAX_ENTRIES (100) happens in loadGame() to prevent 21-page bug.
 * - getJournalEntries() uses reverse indexing so Page 1 contains the newest entries.
 * - Removed print statements from loadGame() and saveGame() to stop duplicate messages.
 */

import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Journal {
	private static final String SAVE_DIRECTORY = "saves/";
	public static final int ENTRIES_PER_PAGE = 5;
	public static final int MAX_PAGES = 20;
	public static final int MAX_ENTRIES = ENTRIES_PER_PAGE * MAX_PAGES; // 100 entries

	private static class PlotData {
		boolean watered;
		boolean weeded;
		boolean fertilized;
		String soilQuality;
		boolean isFlowerPot;
		int consecutiveDaysWithoutWater;
		String[] flowerData;

		PlotData() {
			this.watered = false;
			this.weeded = true;
			this.fertilized = false;
			this.soilQuality = "Average";
			this.isFlowerPot = false;
			this.consecutiveDaysWithoutWater = 0;
			this.flowerData = null;
		}
	}

	/**
	 * Saves the player's current state to a journal file.
	 * NOTE: This method is now silent. Success messages are handled by JournalActions.
	 */
	public static boolean saveGame(Player1 player) {
		File directory = new File(SAVE_DIRECTORY);
		if (!directory.exists()) {
			directory.mkdirs();
		}

		String filename = SAVE_DIRECTORY + player.getName() + ".txt";
		
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
			// Write player basic info
			writer.write("[PLAYER]\n");
			writer.write("Name=" + player.getName() + "\n");
			writer.write("NRG=" + player.getNRG() + "\n");
			writer.write("Credits=" + player.getCredits() + "\n");
			writer.write("Day=" + player.getDay() + "\n");
			writer.write("FlowerPotsCrafted=" + player.getFlowerPotsCrafted() + "\n");
			writer.write("HasBuiltExtraPlot=" + player.hasBuiltExtraPlot() + "\n");
			writer.write("HasCompostBin=" + player.hasCompostBin() + "\n");
			writer.write("CompostWitheredCount=" + player.getCompostWitheredCount() + "\n");

			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			writer.write("SaveDate=" + now.format(formatter) + "\n");

			// Save unlocked dreams
			writer.write("[UNLOCKED_DREAMS]\n");
			for (String dreamFile : player.getUnlockedDreams()) {
				writer.write("Dream=" + dreamFile + "\n");
			}

			// Write inventory items
			writer.write("[INVENTORY]\n");
			ArrayList<Object> inventory = player.getInventory();

			for (Object item : inventory) {
				if (item instanceof Flower) {
					Flower flower = (Flower) item;
					writer.write("Flower=" + flower.getName() + "," +
							flower.getGrowthStage() + "," +
							flower.getDaysPlanted() + "," +
							flower.getDurability() + "," +
							flower.getCost());

					if (item instanceof FlowerInstance) {
						writer.write("," + ((FlowerInstance) item).getNRGRestored());
					}

					writer.write("\n");
				} else if (item instanceof gardenPlot) {
					gardenPlot pot = (gardenPlot) item;
					if (pot.isFlowerPot()) {
						writer.write("FlowerPot=empty\n");
					}
				}
			}

			// Write garden plots
			writer.write("[GARDEN_PLOTS]\n");
			List<gardenPlot> gardenPlots = player.getGardenPlots();
			writer.write("PlotCount=" + gardenPlots.size() + "\n");

			for (int i = 0; i < gardenPlots.size(); i++) {
				gardenPlot plot = gardenPlots.get(i);
				writer.write("Plot=" + i + "," + 
						plot.isWatered() + "," + 
						plot.isWeeded() + "," + 
						plot.isFertilized() + "," +
						plot.getSoilQuality() + "," +
						plot.isFlowerPot() + "," +
						plot.getConsecutiveDaysWithoutWater() + "\n");

				if (plot.isOccupied()) {
					Flower flower = plot.getPlantedFlower();
					writer.write("PlotFlower=" + i + "," +
							flower.getName() + "," +
							flower.getGrowthStage() + "," +
							flower.getDaysPlanted() + "," +
							flower.getDurability() + "," +
							flower.getCost());

					if (flower instanceof FlowerInstance) {
						writer.write("," + ((FlowerInstance) flower).getNRGRestored());
					}

					writer.write("\n");
				}
			}

			// Write journal entries from the PRUNED in-memory list (oldest-to-newest)
			writer.write("[JOURNAL_ENTRIES]\n");

			// Get the in-memory list (which should already be pruned on load)
			List<String> entriesToSave = new ArrayList<>(player.getJournalEntries());
			
			// 1. Add the new "Game saved" entry to the *end* of the list (making it the newest)
			String newSaveEntry = "Day " + player.getDay() + " (" + now.format(formatter) + "): Game saved on day " + player.getDay() + ".";
			entriesToSave.add(newSaveEntry);
			
			// 2. Prune one more if necessary (for the newly added save entry)
			if (entriesToSave.size() > MAX_ENTRIES) {
				entriesToSave.remove(0); // Remove oldest entry
			}
			
			// 3. Reverse the list for saving, so that it appears newest-to-oldest in the file.
			Collections.reverse(entriesToSave);

			// 4. Write the entries to the file, converting the formatted string back to the file's 'Entry=' line.
			for (String formattedEntry : entriesToSave) {
				// We need to parse the format "Day X (Date): Entry Text" back to "Entry=X,Date,Text"
				try {
					int dayStart = formattedEntry.indexOf(' ') + 1;
					int dayEnd = formattedEntry.indexOf('(') - 1; 
					String day = formattedEntry.substring(dayStart, dayEnd).trim();

					int dateStart = formattedEntry.indexOf('(') + 1;
					int dateEnd = formattedEntry.indexOf(')');
					String date = formattedEntry.substring(dateStart, dateEnd);
					
					String entryText;

					int lastParenColonSpaceIndex = formattedEntry.lastIndexOf("): ");
					
					if (lastParenColonSpaceIndex != -1) {
						entryText = formattedEntry.substring(lastParenColonSpaceIndex + 3).trim();
					} else if (dateEnd != -1) {
						entryText = formattedEntry.substring(dateEnd + 3).trim();
					} else {
						entryText = ""; 
					}
					
					// Write in file format: Entry=Day,Date,Text
					writer.write("Entry=" + day + "," + date + "," + entryText + "\n");
				} catch (Exception e) {
					writer.write("# Malformed Entry Skipped: " + formattedEntry + "\n");
				}
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Loads a player's saved game data.
	 * NOTE: This method is now silent. Success messages are handled by sunflowerSimulator.
	 */
	public static Player1 loadGame(String playerName) {
		String filename = SAVE_DIRECTORY + playerName + ".txt";
		File saveFile = new File(filename);

		if (!saveFile.exists()) {
			return null;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			Player1 player = null;
			String section = "";
			String line;
			List<String> journalEntries = new ArrayList<>();
			Set<String> unlockedDreams = new HashSet<>();

			int expectedPlotCount = 0;
			Map<Integer, PlotData> plotDataMap = new HashMap<>();

			while ((line = reader.readLine()) != null) {
				// Section headers
				if (line.equals("[PLAYER]")) {
					section = "PLAYER";
					continue;
				} else if (line.equals("[UNLOCKED_DREAMS]")) {
					section = "UNLOCKED_DREAMS";
					continue;
				} else if (line.equals("[INVENTORY]")) {
					section = "INVENTORY";
					continue;
				} else if (line.equals("[GARDEN_PLOTS]")) {
					section = "GARDEN_PLOTS";
					continue;
				} else if (line.equals("[JOURNAL_ENTRIES]")) {
					section = "JOURNAL_ENTRIES";
					continue;
				}

				// Process data
				if (section.equals("PLAYER")) {
					if (line.startsWith("Name=")) {
						String name = line.substring(5);
						player = new Player1(name);
					} else if (line.startsWith("NRG=")) {
						player.setNRG(Integer.parseInt(line.substring(4)));
					} else if (line.startsWith("Credits=")) {
						player.setCredits(Integer.parseInt(line.substring(8)));
					} else if (line.startsWith("Day=")) {
						int targetDay = Integer.parseInt(line.substring(4));
						if (player != null) {
							while (player.getDay() < targetDay) {
								player.advanceDay();
							}
						}
					} else if (line.startsWith("FlowerPotsCrafted=")) {
						if (player != null) player.setFlowerPotsCrafted(Integer.parseInt(line.substring(18)));
					} else if (line.startsWith("HasBuiltExtraPlot=")) {
						if (player != null) player.setHasBuiltExtraPlot(Boolean.parseBoolean(line.substring(18)));
					} else if (line.startsWith("HasCompostBin=")) {
						if (player != null) player.setHasCompostBin(Boolean.parseBoolean(line.substring(14)));
					} else if (line.startsWith("CompostWitheredCount=")) {
						if (player != null) player.setCompostWitheredCount(Integer.parseInt(line.substring(21)));
					}

				} else if (section.equals("UNLOCKED_DREAMS")) {
					if (line.startsWith("Dream=")) {
						unlockedDreams.add(line.substring(6));
					}
				} else if (section.equals("INVENTORY") && player != null) {
					if (line.startsWith("Flower=")) {
						String[] flowerData = line.substring(7).split(",");
						if (flowerData.length >= 5) {
							String name = flowerData[0];
							String growthStage = flowerData[1];
							int daysPlanted = Integer.parseInt(flowerData[2]);
							double durability = Double.parseDouble(flowerData[3]);
							double cost = Double.parseDouble(flowerData[4]);
							int nrgRestored = (flowerData.length >= 6) ? Integer.parseInt(flowerData[5]) : 1;

							FlowerInstance flower = new FlowerInstance(
									name, growthStage, daysPlanted, durability, nrgRestored, cost);
							player.addToInventory(flower);
						}
					} else if (line.startsWith("FlowerPot=")) {
						gardenPlot pot = new gardenPlot(true);
						player.addToInventory(pot);
					}
				} else if (section.equals("GARDEN_PLOTS") && player != null) {
					if (line.startsWith("PlotCount=")) {
						expectedPlotCount = Integer.parseInt(line.substring(10));
					} else if (line.startsWith("Plot=")) {
						String[] plotData = line.substring(5).split(",");
						if (plotData.length >= 5) {
							int plotIndex = Integer.parseInt(plotData[0]);
							PlotData pd = new PlotData();
							pd.watered = Boolean.parseBoolean(plotData[1]);
							pd.weeded = Boolean.parseBoolean(plotData[2]);
							pd.fertilized = Boolean.parseBoolean(plotData[3]);
							pd.soilQuality = plotData[4];

							if (plotData.length >= 6) {
								pd.isFlowerPot = Boolean.parseBoolean(plotData[5]);
							}

							if (plotData.length >= 7) {
								pd.consecutiveDaysWithoutWater = Integer.parseInt(plotData[6]);
							}

							plotDataMap.put(plotIndex, pd);
						}
					} else if (line.startsWith("PlotFlower=")) {
						String[] flowerData = line.substring(11).split(",");
						if (flowerData.length >= 6) {
							int plotIndex = Integer.parseInt(flowerData[0]);
							PlotData pd = plotDataMap.get(plotIndex);
							if (pd != null) {
								pd.flowerData = flowerData;
							}
						}
					}
				} else if (section.equals("JOURNAL_ENTRIES") && player != null) {
					if (line.startsWith("Entry=")) {
						String[] parts = line.substring(6).split(",", 3);
						if (parts.length >= 3) {
							String day = parts[0];
							String date = parts[1];
							String entryText = parts[2];
							String formattedEntry = "Day " + day + " (" + date + "): " + entryText;
							journalEntries.add(formattedEntry);
						}
					}
				}
			}

			// Restore garden plots
			if (player != null && !plotDataMap.isEmpty()) {
				List<gardenPlot> playerPlots = player.getGardenPlots();
				playerPlots.clear();

				for (int i = 0; i < expectedPlotCount; i++) {
					PlotData pd = plotDataMap.get(i);
					if (pd != null) {
						gardenPlot plot = new gardenPlot(pd.isFlowerPot);
						plot.setSoilQuality(pd.soilQuality);
						plot.setWatered(pd.watered);
						plot.setWeeded(pd.weeded);
						plot.setFertilized(pd.fertilized);
						plot.setConsecutiveDaysWithoutWater(pd.consecutiveDaysWithoutWater);

						playerPlots.add(plot);

						// Restore flower if present
						if (pd.flowerData != null) {
							String name = pd.flowerData[1];
							String growthStage = pd.flowerData[2];
							int daysPlanted = Integer.parseInt(pd.flowerData[3]);
							double durability = Double.parseDouble(pd.flowerData[4]);
							double cost = Double.parseDouble(pd.flowerData[5]);
							int nrgRestored = (pd.flowerData.length >= 7) ? Integer.parseInt(pd.flowerData[6]) : 1;

							FlowerInstance flower = new FlowerInstance(
									name, growthStage, daysPlanted, durability, nrgRestored, cost);

							plot.forcePlantFlower(flower);
						}
					} else {
						playerPlots.add(new gardenPlot());
					}
				}
			}

			// Add journal entries (will be reversed when displayed)
			if (player != null && !journalEntries.isEmpty()) {
				// Step 1: Reverse the list so it is oldest-to-newest in memory.
				// Entries are loaded newest-to-oldest from file, so reversing them makes them oldest-to-newest.
				Collections.reverse(journalEntries); 

				// CRITICAL FIX: Prune the list down to MAX_ENTRIES (100) *before* assigning it to the player.
				while (journalEntries.size() > MAX_ENTRIES) {
					journalEntries.remove(0); // Remove the oldest entry
				}
				
				// Final step: Update the player's internal list with the correctly pruned list.
				List<String> playerJournalList = player.getJournalEntries();
				playerJournalList.clear(); 
				playerJournalList.addAll(journalEntries); 
			}

			// Restore unlocked dreams
			if (player != null && !unlockedDreams.isEmpty()) {
				player.setUnlockedDreams(unlockedDreams);
			}

			return player;
		} catch (IOException | NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Adds a journal entry with automatic pruning to maintain max 100 entries.
	 * NOTE: This method is now silent. Success messages are handled by JournalActions.
	 */
	public static boolean addJournalEntry(Player1 player, String entry) {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String formattedEntry = "Day " + player.getDay() + " (" + now.format(formatter) + "): " + entry;

		// Enforce 100 entry limit - remove oldest if needed
		List<String> entries = player.getJournalEntries();
		if (entries.size() >= MAX_ENTRIES) {
			entries.remove(0); // Remove oldest entry
		}

		player.addJournalEntry(formattedEntry);

		// saveGame will now rewrite the file using the correctly pruned in-memory list.
		return saveGame(player);
	}

	public static List<String> getJournalEntries(String playerName) {
		return getJournalEntries(playerName, 0);
	}

	/**
	 * Gets a list of journal entries for a specific page.
	 * FIX: Page 0 corresponds to the newest entries due to reverse index calculation.
	 */
	public static List<String> getJournalEntries(String playerName, int page) {
		Player1 player = loadGame(playerName);

		if (player == null) {
			return new ArrayList<>();
		}

		// The list from player.getJournalEntries() is oldest-to-newest and max 100 entries.
		List<String> allEntries = player.getJournalEntries();
		
		int totalEntries = allEntries.size();
		int totalPages = (int) Math.ceil((double) totalEntries / ENTRIES_PER_PAGE);

		if (page < 0 || page >= totalPages) {
			return new ArrayList<>();
		}
		
		// FIX: Calculate indices for reverse page order (Page 0 = Newest entries)
		// totalEntries - (page * ENTRIES_PER_PAGE) gives the end index of the desired chunk
		int skipEntries = page * ENTRIES_PER_PAGE;
		int endIndex = totalEntries - skipEntries;
		int startIndex = Math.max(0, endIndex - ENTRIES_PER_PAGE);

		// Returns the list of 5 entries for the requested page, in chronological order (oldest-to-newest for this chunk)
		return allEntries.subList(startIndex, endIndex);
	}

	public static int getTotalJournalPages(String playerName) {
		Player1 player = loadGame(playerName);

		if (player == null) {
			return 0;
		}

		List<String> allEntries = player.getJournalEntries();
		return (int) Math.ceil((double) allEntries.size() / ENTRIES_PER_PAGE);
	}

	public static List<String> getAllJournalEntries(String playerName) {
		Player1 player = loadGame(playerName);

		if (player == null) {
			return new ArrayList<>();
		}

		return player.getJournalEntries();
	}

	public static boolean saveExists(String playerName) {
		String filename = SAVE_DIRECTORY + playerName + ".txt";
		File saveFile = new File(filename);
		return saveFile.exists();
	}

	public static boolean resetGame(Player1 player) {
		File directory = new File(SAVE_DIRECTORY);
		if (!directory.exists()) {
			directory.mkdirs();
		}

		String filename = SAVE_DIRECTORY + player.getName() + ".txt";
		File saveFile = new File(filename);
		if (saveFile.exists()) {
			saveFile.delete();
		}

		return saveGame(player);
	}
}
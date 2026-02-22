/* Journal.java - FIXED VERSION
 */

import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.charset.StandardCharsets;

public class Journal {
	private static final String SAVE_DIRECTORY = "saves/";
	public static final int ENTRIES_PER_PAGE = 5;
	public static final int MAX_PAGES = 20;
	public static final int MAX_ENTRIES = ENTRIES_PER_PAGE * MAX_PAGES; // 100 entries HARD LIMIT

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
	 * Saves the player's current state to a journal file
	 * FIXED: No longer prints success message (handled by caller)
	 * FIXED: Enforces 100-entry hard limit before saving
	 */
	public static boolean saveGame(Player1 player) {
		File directory = new File(SAVE_DIRECTORY);
		if (!directory.exists()) {
			directory.mkdirs();
		}

		String filename = SAVE_DIRECTORY + player.getName() + ".txt";

		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8))) {
			// Write player basic info
			writer.write("[PLAYER]\n");
			writer.write("Name=" + player.getName() + "\n");
			writer.write("NRG=" + player.getNRG() + "\n");
			writer.write("Credits=" + player.getCredits() + "\n");
			writer.write("Day=" + player.getDay() + "\n");
			writer.write("FlowerPotsCrafted=" + player.getFlowerPotsCrafted() + "\n");
			writer.write("HasBuiltExtraPlot=" + player.hasBuiltExtraPlot() + "\n");

			// Save compost bin upgrade status
			writer.write("HasCompostBin=" + player.hasCompostBin() + "\n");
			writer.write("CompostWitheredCount=" + player.getCompostWitheredCount() + "\n");
			writer.write("HasMulcher=" + player.hasMulcher() + "\n");
			writer.write("MulcherDaysRemaining=" + player.getMulcherDaysRemaining() + "\n");
			writer.write("HasSprinklerSystem=" + player.hasSprinklerSystem() + "\n");
			writer.write("GreenhouseCount=" + player.getGreenhouseCount() + "\n");
			writer.write("HasDripIrrigationLines=" + player.hasDripIrrigationLines() + "\n");
			writer.write("HasGrowLight=" + player.hasGrowLight() + "\n");
			writer.write("HasSeedStartingTray=" + player.hasSeedStartingTray() + "\n");
			writer.write("HasHeatLamp=" + player.hasHeatLamp() + "\n");
			writer.write("HasBuzzsaw=" + player.hasBuzzsaw() + "\n");
			writer.write("HasCraftedMantle=" + player.hasCraftedMantle() + "\n");
			writer.write("HasPlacedMantle=" + player.hasPlacedMantle() + "\n");

			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			writer.write("SaveDate=" + now.format(formatter) + "\n");

			// Save unlocked dreams
			writer.write("[UNLOCKED_DREAMS]\n");
			for (String dreamFile : player.getUnlockedDreams()) {
				writer.write("Dream=" + dreamFile + "\n");
			}

			// Save unlocked hints
			writer.write("[UNLOCKED_HINTS]\n");
			for (String hintFile : player.getUnlockedHints()) {
				writer.write("Hint=" + hintFile + "\n");
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
				} else if (item instanceof Bouquet) {
					Bouquet bouquet = (Bouquet) item;
					writer.write("Bouquet=" + bouquet.getFlowerCount() + "," + 
							bouquet.getDayCreated() + "," + 
							bouquet.getBaseValue());
					if (bouquet.hasCustomName()) {
						writer.write("," + bouquet.getCustomName());
					}
					writer.write("\n");

					// Save constituent flowers
					for (int i = 0; i < bouquet.getFlowers().size(); i++) {
						Flower f = bouquet.getFlowers().get(i);
						writer.write("BouquetFlower=" + i + "," + f.getName() + "," +
								f.getGrowthStage() + "," + f.getDaysPlanted() + "," +
								f.getDurability() + "," + f.getCost() + "\n");
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

			// In Journal.saveGame(), add after [GARDEN_PLOTS]:
			writer.write("[AUCTION_HOUSE]\n");
			AuctionHouse auctionHouse = player.getAuctionHouse();
			writer.write("HasCollectedEarnings=" + !auctionHouse.hasUncollectedEarnings() + "\n");
			writer.write("RecognitionBonusApplied=" + auctionHouse.isRecognitionBonusApplied() + "\n");
			if (auctionHouse.hasActiveAuction()) {
				Bouquet bouquet = auctionHouse.getCurrentBouquet();
				writer.write("ActiveAuction=true\n");
				writer.write("AuctionStartDay=" + auctionHouse.getAuctionStartDay() + "\n");
				writer.write("CurrentBid=" + auctionHouse.getCurrentBid() + "\n");
				writer.write("BouquetFlowerCount=" + bouquet.getFlowerCount() + "\n");
				writer.write("BouquetDayCreated=" + bouquet.getDayCreated() + "\n");

				// Save each flower in bouquet
				for (int i = 0; i < bouquet.getFlowers().size(); i++) {
					Flower f = bouquet.getFlowers().get(i);
					writer.write("BouquetFlower=" + i + "," + f.getName() + "," +
							f.getGrowthStage() + "," + f.getDaysPlanted() + "," +
							f.getDurability() + "," + f.getCost());
					if (f instanceof FlowerInstance) {
						writer.write("," + ((FlowerInstance) f).getNRGRestored());
					}
					writer.write("\n");
				}

				if (bouquet.hasCustomName()) {
					writer.write("BouquetName=" + bouquet.getCustomName() + "\n");
				}

				// Save applied multipliers
				for (String mult : auctionHouse.getAppliedMultipliers()) {
					writer.write("AppliedMultiplier=" + mult + "\n");
				}
			}

			if (auctionHouse.hasUncollectedEarnings()) {
				writer.write("UncollectedEarnings=" + auctionHouse.getEarningsWaiting() + "\n");
			}

			// Save known bouquet compositions
			writer.write("[KNOWN_BOUQUETS]\n");
			for (Map.Entry<String, String> entry : player.getKnownBouquetCompositions().entrySet()) {
				writer.write("Composition=" + entry.getKey() + "," + entry.getValue());
				Integer score = player.getBouquetHighScore(entry.getKey());
				if (score != null) {
					writer.write("," + score);
				}
				writer.write("\n");
			}

			writer.write("[MANTLE_DISPLAY]\n");
			if (player.hasPlacedMantle() && player.getPlacedMantle() != null) {
				List<Bouquet> displayed = player.getPlacedMantle().getDisplayedBouquets();
				writer.write("DisplayedCount=" + displayed.size() + "\n");
				for (int i = 0; i < displayed.size(); i++) {
					Bouquet bouquet = displayed.get(i);
					writer.write("MantleBouquet=" + i + "," + bouquet.getFlowerCount() + "," + bouquet.getDayCreated());
					if (bouquet.hasCustomName()) {
						writer.write("," + bouquet.getCustomName());
					}
					writer.write("\n");
					for (int f = 0; f < bouquet.getFlowers().size(); f++) {
						Flower flower = bouquet.getFlowers().get(f);
						writer.write("MantleFlower=" + i + "," + f + "," + flower.getName() + "," +
								flower.getGrowthStage() + "," + flower.getDaysPlanted() + "," +
								flower.getDurability() + "," + flower.getCost());
						if (flower instanceof FlowerInstance) {
							writer.write("," + ((FlowerInstance) flower).getNRGRestored());
						}
						writer.write("\n");
					}
				}
			} else {
				writer.write("DisplayedCount=0\n");
			}

			// CRITICAL FIX: Enforce 100-entry limit BEFORE saving
			List<String> allEntries = player.getJournalEntries();
			if (allEntries.size() > MAX_ENTRIES) {
				// Keep only the most recent 100 entries
				List<String> trimmedEntries = new ArrayList<>(
						allEntries.subList(allEntries.size() - MAX_ENTRIES, allEntries.size())
						);
				player.setJournalEntries(trimmedEntries);
				allEntries = trimmedEntries;
			}

			// Write journal entries (stored chronologically, oldest to newest)
			writer.write("[JOURNAL_ENTRIES]\n");
			for (String entry : allEntries) {
				// Entries are already formatted as "Day X (date): message"
				// Extract components to save in parseable format
				if (entry.startsWith("Day ")) {
					int dayEnd = entry.indexOf(" (");
					int dateEnd = entry.indexOf("): ");

					if (dayEnd > 0 && dateEnd > 0) {
						String dayPart = entry.substring(4, dayEnd); // Skip "Day "
						String datePart = entry.substring(dayEnd + 2, dateEnd); // Skip " ("
						String messagePart = entry.substring(dateEnd + 3); // Skip "): "

						writer.write("Entry=" + dayPart + "," + datePart + "," + messagePart + "\n");
					}
				}
			}

			return true;
		} catch (IOException e) {
			System.out.println("[X] Error saving game: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Loads a player's saved game data
	 * FIXED: Enforces 100-entry limit during load
	 * FIXED: Removed DEBUG print statements
	 */
	public static Player1 loadGame(String playerName) {
		String filename = SAVE_DIRECTORY + playerName + ".txt";
		File saveFile = new File(filename);

		if (!saveFile.exists()) {
			return null;
		}

		Player1 player = null;
		String section = "";
		Map<Integer, PlotData> plotDataMap = new HashMap<>();
		int expectedPlotCount = 0;
		List<String> journalEntries = new ArrayList<>();
		List<String> unlockedDreams = new ArrayList<>();
		List<String> unlockedHints = new ArrayList<>();
		List<Flower> auctionBouquetFlowers = new ArrayList<>();
		List<String> auctionAppliedMultipliers = new ArrayList<>();
		Map<String, String> knownBouquetCompositions = new HashMap<>();
		Map<String, Integer> bouquetHighScores = new HashMap<>();
		boolean hasActiveAuction = false;
		int auctionStartDay = -1;
		double currentBid = 0;
		double uncollectedEarnings = 0;
		boolean hasCollectedEarnings = true;
		boolean recognitionBonusApplied = false;
		String auctionBouquetName = null;
		int auctionBouquetDayCreated = -1;
		Map<Integer, List<Flower>> mantleFlowersByBouquet = new HashMap<>();
		Map<Integer, String> mantleBouquetNames = new HashMap<>();
		Map<Integer, Integer> mantleBouquetDays = new HashMap<>();

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8))) {
			String line;

			while ((line = reader.readLine()) != null) {
				line = line.trim();

				if (line.isEmpty()) continue;

				if (line.startsWith("[") && line.endsWith("]")) {
					section = line.substring(1, line.length() - 1);
					continue;
				}

				if (section.equals("PLAYER")) {
					if (line.startsWith("Name=")) {
						String name = line.substring(5);
						player = new Player1(name);
					} else if (player != null) {
				if (player.hasPlacedMantle()) {
					Mantle mantle = player.getPlacedMantle();
					List<Integer> keys = new ArrayList<>(mantleFlowersByBouquet.keySet());
					Collections.sort(keys);
					for (Integer idx : keys) {
						List<Flower> flowers = mantleFlowersByBouquet.get(idx);
						if (flowers != null && !flowers.isEmpty()) {
							String cname = mantleBouquetNames.get(idx);
							int d = mantleBouquetDays.getOrDefault(idx, player.getDay());
							mantle.addBouquet(new Bouquet(flowers, cname, d));
						}
					}
				}

						if (line.startsWith("NRG=")) {
							player.setNRG(Integer.parseInt(line.substring(4)));
						} else if (line.startsWith("Credits=")) {
							player.setCredits(Integer.parseInt(line.substring(8)));
						} else if (line.startsWith("Day=")) {
							player.setDay(Integer.parseInt(line.substring(4)));
						} else if (line.startsWith("FlowerPotsCrafted=")) {
							player.setFlowerPotsCrafted(Integer.parseInt(line.substring(18)));
						} else if (line.startsWith("HasBuiltExtraPlot=")) {
							player.setHasBuiltExtraPlot(Boolean.parseBoolean(line.substring(18)));
						} else if (line.startsWith("HasCompostBin=")) {
							player.setHasCompostBin(Boolean.parseBoolean(line.substring(14)));
						} else if (line.startsWith("CompostWitheredCount=")) {
							player.setCompostWitheredCount(Integer.parseInt(line.substring(21)));
						} else if (line.startsWith("HasMulcher=")) {
							player.setHasMulcher(Boolean.parseBoolean(line.substring(11)));
						} else if (line.startsWith("MulcherDaysRemaining=")) {
							player.setMulcherDaysRemaining(Integer.parseInt(line.substring(21)));
						} else if (line.startsWith("HasSprinklerSystem=")) {
							player.setHasSprinklerSystem(Boolean.parseBoolean(line.substring(19)));
						} else if (line.startsWith("GreenhouseCount=")) {
							player.setGreenhouseCount(Integer.parseInt(line.substring(16)));
						} else if (line.startsWith("HasDripIrrigationLines=")) {
							player.setHasDripIrrigationLines(Boolean.parseBoolean(line.substring(23)));
						} else if (line.startsWith("HasGrowLight=")) {
							player.setHasGrowLight(Boolean.parseBoolean(line.substring(13)));
						} else if (line.startsWith("HasSeedStartingTray=")) {
							player.setHasSeedStartingTray(Boolean.parseBoolean(line.substring(20)));
						} else if (line.startsWith("HasHeatLamp=")) {
							player.setHasHeatLamp(Boolean.parseBoolean(line.substring(12)));
						} else if (line.startsWith("HasBuzzsaw=")) {
							player.setHasBuzzsaw(Boolean.parseBoolean(line.substring(11)));
						} else if (line.startsWith("HasCraftedMantle=")) {
							player.setHasCraftedMantle(Boolean.parseBoolean(line.substring(16)));
						} else if (line.startsWith("HasPlacedMantle=")) {
							if (Boolean.parseBoolean(line.substring(15))) {
								player.setPlacedMantle(new Mantle());
							}
						}
					}
				} else if (section.equals("UNLOCKED_DREAMS") && line.startsWith("Dream=")) {
					unlockedDreams.add(line.substring(6));
				} else if (section.equals("UNLOCKED_HINTS") && line.startsWith("Hint=")) {
					unlockedHints.add(line.substring(5));
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
					} else if (line.startsWith("FlowerPot=empty")) {
						gardenPlot pot = new gardenPlot(true);
						player.addToInventory(pot);
					}
				} else if (section.equals("AUCTION_HOUSE")) {
					if (line.startsWith("ActiveAuction=")) {
						hasActiveAuction = Boolean.parseBoolean(line.substring(14));
					} else if (line.startsWith("AuctionStartDay=")) {
						auctionStartDay = Integer.parseInt(line.substring(16));
					} else if (line.startsWith("CurrentBid=")) {
						currentBid = Double.parseDouble(line.substring(11));
					} else if (line.startsWith("BouquetName=")) {
						auctionBouquetName = line.substring(12);
					} else if (line.startsWith("BouquetDayCreated=")) {
						auctionBouquetDayCreated = Integer.parseInt(line.substring(18));
					} else if (line.startsWith("BouquetFlower=")) {
						String[] flowerData = line.substring(13).split(",");
						if (flowerData.length >= 6) {
							String name = flowerData[1];
							String growthStage = flowerData[2];
							int daysPlanted = Integer.parseInt(flowerData[3]);
							double durability = Double.parseDouble(flowerData[4]);
							double cost = Double.parseDouble(flowerData[5]);
							int nrgRestored = (flowerData.length >= 7) ? Integer.parseInt(flowerData[6]) : 1;
							auctionBouquetFlowers.add(new FlowerInstance(
									name, growthStage, daysPlanted, durability, nrgRestored, cost));
						}
					} else if (line.startsWith("AppliedMultiplier=")) {
						auctionAppliedMultipliers.add(line.substring(18));
					} else if (line.startsWith("UncollectedEarnings=")) {
						uncollectedEarnings = Double.parseDouble(line.substring(20));
						hasCollectedEarnings = false;
					} else if (line.startsWith("HasCollectedEarnings=")) {
						hasCollectedEarnings = Boolean.parseBoolean(line.substring(20));
					} else if (line.startsWith("RecognitionBonusApplied=")) {
						recognitionBonusApplied = Boolean.parseBoolean(line.substring(23));
					}
				} else if (section.equals("KNOWN_BOUQUETS") && line.startsWith("Composition=")) {
					String compositionData = line.substring(12);
					Integer parsedHighScore = null;

					int lastComma = compositionData.lastIndexOf(',');
					if (lastComma > 0) {
						String maybeScore = compositionData.substring(lastComma + 1);
						if (!maybeScore.isEmpty() && maybeScore.matches("-?\\d+")) {
							parsedHighScore = Integer.parseInt(maybeScore);
							compositionData = compositionData.substring(0, lastComma);
						}
					}

					int separatorIndex = compositionData.lastIndexOf(',');
					if (separatorIndex > 0) {
						String signature = compositionData.substring(0, separatorIndex);
						String knownName = compositionData.substring(separatorIndex + 1);
						knownBouquetCompositions.put(signature, knownName);
						if (parsedHighScore != null) {
							bouquetHighScores.put(signature, parsedHighScore);
						}
					}
				
				} else if (section.equals("MANTLE_DISPLAY") && player != null) {
					if (line.startsWith("MantleBouquet=")) {
						String[] parts = line.substring(14).split(",");
						if (parts.length >= 3) {
							int bouquetIndex = Integer.parseInt(parts[0]);
							int dayCreated = Integer.parseInt(parts[2]);
							String customName = (parts.length >= 4) ? parts[3] : null;
							mantleBouquetDays.put(bouquetIndex, dayCreated);
							if (customName != null && !customName.isEmpty()) {
								mantleBouquetNames.put(bouquetIndex, customName);
							}
						}
					} else if (line.startsWith("MantleFlower=")) {
						String[] flowerData = line.substring(12).split(",");
						if (flowerData.length >= 7) {
							int bouquetIndex = Integer.parseInt(flowerData[0]);
							String name = flowerData[2];
							String stage = flowerData[3];
							int daysPlanted = Integer.parseInt(flowerData[4]);
							double durability = Double.parseDouble(flowerData[5]);
							double cost = Double.parseDouble(flowerData[6]);
							int nrgRestored = (flowerData.length >= 8) ? Integer.parseInt(flowerData[7]) : 1;
							FlowerInstance flower = new FlowerInstance(name, stage, daysPlanted, durability, nrgRestored, cost);
							mantleFlowersByBouquet.computeIfAbsent(bouquetIndex, k -> new ArrayList<>()).add(flower);
						}
					}
				} else if (section.equals("GARDEN_PLOTS")) {
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

			// CRITICAL FIX: Enforce 100-entry limit during load
			if (journalEntries.size() > MAX_ENTRIES) {
				// Keep only the most recent 100 entries
				journalEntries = new ArrayList<>(
						journalEntries.subList(journalEntries.size() - MAX_ENTRIES, journalEntries.size())
						);
			}

			// Add journal entries to player (stored chronologically)
			if (player != null && !journalEntries.isEmpty()) {
				player.setJournalEntries(journalEntries);
			}

			// Restore unlocked dreams
			if (player != null && !unlockedDreams.isEmpty()) {
				player.setUnlockedDreams(new HashSet<>(unlockedDreams));
			}

			// Restore unlocked hints
			if (player != null && !unlockedHints.isEmpty()) {
				player.setUnlockedHints(new HashSet<>(unlockedHints));
			}

			if (player != null) {
				if (player.hasPlacedMantle()) {
					Mantle mantle = player.getPlacedMantle();
					List<Integer> keys = new ArrayList<>(mantleFlowersByBouquet.keySet());
					Collections.sort(keys);
					for (Integer idx : keys) {
						List<Flower> flowers = mantleFlowersByBouquet.get(idx);
						if (flowers != null && !flowers.isEmpty()) {
							String cname = mantleBouquetNames.get(idx);
							int d = mantleBouquetDays.getOrDefault(idx, player.getDay());
							mantle.addBouquet(new Bouquet(flowers, cname, d));
						}
					}
				}

				if (!knownBouquetCompositions.isEmpty()) {
					player.setKnownBouquetCompositions(knownBouquetCompositions);
				}
				if (!bouquetHighScores.isEmpty()) {
					player.setBouquetHighScores(bouquetHighScores);
				}

				AuctionHouse auctionHouse = new AuctionHouse();
				auctionHouse.setHasCollectedEarnings(hasCollectedEarnings);
				auctionHouse.setRecognitionBonusApplied(recognitionBonusApplied);
				if (uncollectedEarnings > 0) {
					auctionHouse.setEarningsWaiting(uncollectedEarnings);
					auctionHouse.setHasCollectedEarnings(false);
				}

				if (hasActiveAuction && !auctionBouquetFlowers.isEmpty()) {
					int dayCreated = (auctionBouquetDayCreated > 0) ? auctionBouquetDayCreated : auctionStartDay;
					Bouquet currentBouquet = new Bouquet(auctionBouquetFlowers, auctionBouquetName, dayCreated);
					auctionHouse.setCurrentBouquet(currentBouquet);
					auctionHouse.setAuctionStartDay(auctionStartDay);
					auctionHouse.setCurrentBid(currentBid);
					auctionHouse.setAppliedMultipliers(auctionAppliedMultipliers);
				}

				player.setAuctionHouse(auctionHouse);
			}

			if (player != null) {
				if (player.hasPlacedMantle()) {
					Mantle mantle = player.getPlacedMantle();
					List<Integer> keys = new ArrayList<>(mantleFlowersByBouquet.keySet());
					Collections.sort(keys);
					for (Integer idx : keys) {
						List<Flower> flowers = mantleFlowersByBouquet.get(idx);
						if (flowers != null && !flowers.isEmpty()) {
							String cname = mantleBouquetNames.get(idx);
							int d = mantleBouquetDays.getOrDefault(idx, player.getDay());
							mantle.addBouquet(new Bouquet(flowers, cname, d));
						}
					}
				}

				//System.out.println("[OK] Story loaded successfully!");
			}

			return player;
		} catch (IOException | NumberFormatException e) {
			System.out.println("[X] Error loading game: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Adds a journal entry with automatic pruning to maintain max 100 entries
	 * FIXED: No longer calls saveGame() internally (prevents duplicate saves)
	 * FIXED: Deduplicates consecutive similar messages
	 */
	public static boolean addJournalEntry(Player1 player, String entry) {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String formattedEntry = "Day " + player.getDay() + " (" + now.format(formatter) + "): " + entry;

		// CRITICAL FIX: Deduplicate consecutive similar weed messages
		List<String> entries = player.getJournalEntries();
		if (!entries.isEmpty()) {
			int consecutiveWeedCount = 0;
			String weedMessage = "Some weeds appeared in the garden.";

			// Count consecutive weed messages from the end
			for (int i = entries.size() - 1; i >= 0; i--) {
				if (entries.get(i).contains(weedMessage)) {
					consecutiveWeedCount++;
				} else {
					break;
				}
			}

			// If we already have 3 consecutive weed messages, don't add another
			if (entry.contains(weedMessage) && consecutiveWeedCount >= 3) {
				return false; // Silently skip this entry
			}
		}

		// Enforce 100 entry limit - remove oldest if needed
		if (entries.size() >= MAX_ENTRIES) {
			entries.remove(0); // Remove oldest entry
		}

		player.addJournalEntry(formattedEntry);
		return true;
	}

	/**
	 * Gets journal entries for a specific page (displayed in reverse chronological order)
	 * FIXED: Returns entries in reverse order (newest first)
	 */
	public static List<String> getJournalEntries(String playerName, int page) {
		Player1 player = loadGame(playerName);

		if (player == null) {
			return new ArrayList<>();
		}

		List<String> allEntries = player.getJournalEntries();
		int totalEntries = allEntries.size();

		if (totalEntries == 0 || page < 0) {
			return new ArrayList<>();
		}

		// FIX: Reverse the entire list FIRST so newest entries are at index 0
		List<String> reversedEntries = new ArrayList<>(allEntries);
		java.util.Collections.reverse(reversedEntries);

		// Now paginate from the reversed list (newest first)
		int startIndex = page * ENTRIES_PER_PAGE;
		int endIndex = Math.min(startIndex + ENTRIES_PER_PAGE, totalEntries);

		if (startIndex >= totalEntries) {
			return new ArrayList<>();
		}

		return reversedEntries.subList(startIndex, endIndex);
	}

	/**
	 * Gets total number of journal pages
	 * FIXED: Correctly calculates based on actual entry count
	 */
	public static int getTotalJournalPages(String playerName) {
		Player1 player = loadGame(playerName);

		if (player == null) {
			return 0;
		}

		List<String> allEntries = player.getJournalEntries();
		int totalEntries = allEntries.size();

		// Calculate pages: 1-5 entries = 1 page, 6-10 entries = 2 pages, etc.
		return (int) Math.ceil((double) totalEntries / ENTRIES_PER_PAGE);
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

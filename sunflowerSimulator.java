/* Creator: Pat Eizenga
 * Created: 6/18/2024
 * Last Updated: 4/5/2025
 * Project: Open source, open dialog, gardening game developed with love, focus and dreams.
 * */


import java.util.Random;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

/**
 * Main game executed here.
 */
public class sunflowerSimulator {

    /**
     * Main game.
     * @param args NOT USED
     */
    public static void main(String[] args) {
        // Tutorial and introductions
        System.out.println("🌻 Welcome to Sunflower Simulator! 🌻");
        System.out.println("A gardening game developed with love, focus, and dreams.\n");
                
        // Declare a scanner for user input
        Scanner scanner = new Scanner(System.in);
        
        // Get player name and check for existing save
        System.out.print("Please enter your name: ");
        String playerName = scanner.nextLine();
        
        // Check if a save file exists for this player
        Player1 player;
        boolean newGame = !Journal.saveExists(playerName);
        
        if (newGame) {
            // Create a new player
            player = new Player1(playerName);
            
            // New game introduction
            System.out.println("\nHey there, " + player.getName() + "! It's a pleasure to meet you. :)");
            System.out.println("Let's get started with some of the basics!");
            System.out.println("Each day, you will start with " + player.getNRG() + " NRG.");
            System.out.println("Spend your energy wisely! When you run out you will need to go to bed.");
            System.out.println("You start with " + player.getCredits() + " credits to buy seeds.");
            
            // Add a starting flower seed to the player's inventory
            MammothSunflower starterSeed = new MammothSunflower(
                "Mammoth Sunflower", "Seed", 0, 10, 1, 5);
            player.addToInventory(starterSeed);
            System.out.println("\nYou've been given a Mammoth Sunflower seed to start your garden!");
            
            // Create first journal entry
            Journal.saveGame(player);
            Journal.addJournalEntry(player, "Started my gardening adventure!");
        } else {
            // Load existing player
            player = Journal.loadGame(playerName);
            System.out.println("\nWelcome back, " + player.getName() + "!");
            System.out.println("Your game has been loaded from day " + player.getDay() + ".");
            System.out.println("You have " + player.getNRG() + " NRG and " + player.getCredits() + " credits.");
            Journal.addJournalEntry(player, "Resumed my gardening adventure.");
        }
        
        // Main game loop
        boolean gameContinues = true;
        
        do {
            // Display menu
            System.out.println("\n=== Day " + player.getDay() + " ===");
            System.out.println("NRG: " + player.getNRG() + " | Credits: " + player.getCredits());
            System.out.println("\nWhat would you like to do?");
            System.out.println("1: Weed Garden");
            System.out.println("2: Water Garden");
            System.out.println("3: Plant");
            System.out.println("4: Build");
            System.out.println("5: Shop");
            System.out.println("6: Backpack");
            System.out.println("7: Trim Plants");
            System.out.println("8: Check");
            System.out.println("9: Journal");
            System.out.println("0: Go to bed");
            System.out.println("X: Save & Exit Game");
            
            System.out.print("\nEnter your choice: ");
            String actionMenuChoice = scanner.next();
            scanner.nextLine(); // Clear input buffer
            
            // Process menu choice
            switch(actionMenuChoice) {
                case "0": // Go to bed
                    System.out.println("What would you like to do?");
                    System.out.println("1: Go to bed (save & continue)");
                    System.out.println("2: Save & exit game");
                    
                    System.out.print("\nEnter your choice: ");
                    String bedChoice = scanner.next();
                    scanner.nextLine(); // Clear buffer
                    
                    switch (bedChoice) {
                        case "1": // Go to bed and continue
                            // Save game before advancing day
                            Journal.saveGame(player);
                            
                            // Handle dream chance
                            Random random = new Random();
                            int chanceOfDreamRoll = random.nextInt(100);
                            
                            if (chanceOfDreamRoll > 50) {
                                System.out.println("\n💤 You had a strange dream about your garden...");
                                // TODO: Implement dream content
                                System.out.println("You wake up feeling inspired.");
                                Journal.addJournalEntry(player, "Had a strange dream about the garden.");
                            } else {
                                System.out.println("\nYou feel refreshed. It's a new day! :D");
                                Journal.addJournalEntry(player, "Slept soundly through the night.");
                            }
                            
                            // Advance to next day
                            player.advanceDay();
                            System.out.println("It is now day " + player.getDay() + ".");
                            
                            // Advance growth of planted flowers
                            // This would require tracking planted flowers separately
                            break;
                            
                        case "2": // Save and exit
                            System.out.println("Saving game and exiting...");
                            Journal.saveGame(player);
                            Journal.addJournalEntry(player, "Ended gardening session on day " + player.getDay() + ".");
                            System.out.println("Game saved successfully. Thanks for playing!");
                            gameContinues = false; // Exit the game loop
                            break;
                            
                        default:
                            System.out.println("Invalid choice. Please select 1 or 2.");
                    }
                    break;
                    
                case "1": // Weed Garden
                    if (player.getNRG() <= 0) {
                        System.out.println("You're too tired to do that. You need to go to bed first!");
                        break;
                    }
                    
                    // Check if there are any plots that need weeding
                    List<gardenPlot> weedyPlots = new ArrayList<>();
                    for (int i = 0; i < player.getGardenPlots().size(); i++) {
                        gardenPlot plot = player.getGardenPlot(i);
                        if (!plot.isWeeded()) {
                            weedyPlots.add(plot);
                        }
                    }
                    
                    if (weedyPlots.isEmpty()) {
                        System.out.println("Your garden is already free of weeds!");
                        break;
                    }
                    
                    System.out.println("You spend some time weeding your garden.");
                    
                    // Weed all plots that need it
                    int weedCount = 0;
                    for (gardenPlot plot : weedyPlots) {
                        if (player.getNRG() <= 0) {
                            break; // Stop if out of energy
                        }
                        
                        if (plot.weedPlot()) {
                            weedCount++;
                            player.setNRG(player.getNRG() - 1);
                        }
                    }
                    
                    if (weedCount > 0) {
                        System.out.println("You weeded " + weedCount + " garden plots.");
                        System.out.println("Remaining NRG: " + player.getNRG());
                        Journal.addJournalEntry(player, "Spent time weeding " + weedCount + " garden plots.");
                    } else {
                        System.out.println("You didn't find any weeds to remove.");
                    }
                    break;
                    
                case "2": // Water Garden
                    if (player.getNRG() <= 0) {
                        System.out.println("You're too tired to do that. You need to go to bed first!");
                        break;
                    }
                    
                    // Check for plants that need watering
                    List<gardenPlot> dryPlots = new ArrayList<>();
                    for (int i = 0; i < player.getGardenPlots().size(); i++) {
                        gardenPlot plot = player.getGardenPlot(i);
                        if (plot.isOccupied() && !plot.isWatered()) {
                            dryPlots.add(plot);
                        }
                    }
                    
                    if (dryPlots.isEmpty()) {
                        System.out.println("All of your plants are already watered for today!");
                        break;
                    }
                    
                    System.out.println("You water your plants.");
                    
                    // Water all plots that need it
                    int waterCount = 0;
                    for (gardenPlot plot : dryPlots) {
                        if (player.getNRG() <= 0) {
                            break; // Stop if out of energy
                        }
                        
                        if (plot.waterPlot()) {
                            waterCount++;
                            player.setNRG(player.getNRG() - 1);
                        }
                    }
                    
                    if (waterCount > 0) {
                        System.out.println("You watered " + waterCount + " plants.");
                        System.out.println("Remaining NRG: " + player.getNRG());
                        Journal.addJournalEntry(player, "Watered " + waterCount + " plants in the garden.");
                    } else {
                        System.out.println("You didn't find any plants that needed watering.");
                    }
                    break;
                case "3": // Plant
                    if (player.getNRG() <= 0) {
                        System.out.println("You're too tired to do that. You need to go to bed first!");
                        break;
                    }
                    
                    // Check if player has any seeds
                    List<Flower> availableSeeds = new ArrayList<>();
                    for (Object item : player.getInventory()) {
                        if (item instanceof Flower && ((Flower)item).getGrowthStage().equals("Seed")) {
                            availableSeeds.add((Flower)item);
                        }
                    }
                    
                    if (availableSeeds.isEmpty()) {
                        System.out.println("You don't have any seeds to plant! Visit the shop to buy some.");
                        break;
                    }
                    
                    // Display available garden plots
                    System.out.println("\n🌱 Your Garden Plots 🌱");
                    List<gardenPlot> gardenPlots = player.getGardenPlots();
                    for (int i = 0; i < gardenPlots.size(); i++) {
                        gardenPlot plot = gardenPlots.get(i);
                        System.out.println("Plot #" + (i+1) + ": " + 
                                          (plot.isOccupied() ? 
                                           "[Occupied - " + plot.getPlantedFlower().getName() + " (" + 
                                           plot.getPlantedFlower().getGrowthStage() + ")]" : 
                                           "[Empty]"));
                    }
                    
                    // Ask which plot to plant in
                    System.out.print("\nWhich plot would you like to plant in? (1-" + gardenPlots.size() + 
                                     ", or 0 to cancel): ");
                    int plotChoice;
                    try {
                        plotChoice = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a number.");
                        break;
                    }
                    
                    if (plotChoice == 0) {
                        System.out.println("Planting cancelled.");
                        break;
                    }
                    
                    if (plotChoice < 1 || plotChoice > gardenPlots.size()) {
                        System.out.println("Invalid plot number. Please choose a valid plot.");
                        break;
                    }
                    
                    gardenPlot selectedPlot = gardenPlots.get(plotChoice - 1);
                    
                    if (selectedPlot.isOccupied()) {
                        System.out.println("This plot is already occupied! Choose an empty plot.");
                        break;
                    }
                    
                    // Display available seeds
                    System.out.println("\nAvailable Seeds:");
                    for (int i = 0; i < availableSeeds.size(); i++) {
                        Flower seed = availableSeeds.get(i);
                        System.out.println((i+1) + ": " + seed.getName() + " - Value: " + seed.getCost());
                    }
                    
                    // Ask which seed to plant
                    System.out.print("\nWhich seed would you like to plant? (1-" + availableSeeds.size() + 
                                     ", or 0 to cancel): ");
                    int seedChoice;
                    try {
                        seedChoice = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a number.");
                        break;
                    }
                    
                    if (seedChoice == 0) {
                        System.out.println("Planting cancelled.");
                        break;
                    }
                    
                    if (seedChoice < 1 || seedChoice > availableSeeds.size()) {
                        System.out.println("Invalid seed number. Please choose a valid seed.");
                        break;
                    }
                    
                    Flower selectedSeed = availableSeeds.get(seedChoice - 1);
                    
                    // Plant the seed
                    if (selectedPlot.plantFlower(selectedSeed)) {
                        // Remove the seed from inventory
                        player.removeFromInventory(selectedSeed);
                        
                        System.out.println("\n✅ You successfully planted " + selectedSeed.getName() + 
                                          " in plot #" + plotChoice + "!");
                        System.out.println("Remember to water it regularly for it to grow!");
                        
                        // Add journal entry
                        Journal.addJournalEntry(player, "Planted a " + selectedSeed.getName() + " seed.");
                        
                        // Use energy
                        player.setNRG(player.getNRG() - 2);
                        System.out.println("You used 2 NRG. Remaining NRG: " + player.getNRG());
                    } else {
                        System.out.println("\n❌ Something went wrong. The seed couldn't be planted.");
                    }
                    break;
                    
                case "4": // Build
                    if (player.getNRG() <= 0) {
                        System.out.println("You're too tired to do that. You need to go to bed first!");
                        break;
                    }
                    
                    System.out.println("Building functionality coming soon!");
                    // TODO: Implement building mechanics
                    player.setNRG(player.getNRG() - 3);
                    System.out.println("You used 3 NRG. Remaining NRG: " + player.getNRG());
                    Journal.addJournalEntry(player, "Worked on building something in the garden.");
                    break;
                    
                case "5": // Shop
                    boolean inShop = true;
                    while (inShop) {
                        System.out.println("\n🌼 Welcome to the Flower Shop! 🌼");
                        System.out.println("You have " + player.getCredits() + " credits.");
                        System.out.println("Here are today's seeds for sale:");
                        
                        System.out.println("1. Mammoth Sunflower Seed - 5 credits");
                        System.out.println("2. Mammoth Sunflower Seed - 5 credits");
                        System.out.println("3. Mammoth Sunflower Seed - 5 credits");
                        System.out.println("4. Mammoth Sunflower Seed - 5 credits");
                        System.out.println("5. Leave Shop");
                        
                        System.out.print("Pick a seed to buy (1-5): ");
                        String shopChoice = scanner.next();
                        scanner.nextLine(); // Clear buffer
                        
                        // Handle seed buying
                        switch (shopChoice) {
                            case "1":
                            case "2":
                            case "3":
                            case "4":
                                if (player.getCredits() >= 5) {
                                    // Create a unique seed name
                                    int seedNumber = player.getInventory().size() + 1;
                                    Flower seed = new MammothSunflower(
                                        "Mammoth Sunflower #" + seedNumber, 
                                        "Seed", 0, 100, 1, 5.0);
                                    player.addToInventory(seed);
                                    player.setCredits(player.getCredits() - 5);
                                    System.out.println("✅ You bought a Mammoth Sunflower seed!");
                                    Journal.addJournalEntry(player, "Purchased a Mammoth Sunflower seed from the shop.");
                                    Journal.saveGame(player); // Save after purchase
                                } else {
                                    System.out.println("❌ You don't have enough credits!");
                                }
                                break;
                            case "5":
                                inShop = false;
                                System.out.println("Thank you for visiting the shop!");
                                break;
                            default:
                                System.out.println("Please enter a valid choice (1-5).");
                        }
                    }
                    break;
                    
                case "6": // Backpack/Inventory
                    System.out.println("\n📦 Checking your backpack...");
                    if (player.getInventory().isEmpty()) {
                        System.out.println("Your backpack is empty.");
                    } else {
                        System.out.println("Items in your backpack:");
                        for (int i = 0; i < player.getInventory().size(); i++) {
                            System.out.println((i+1) + ". " + player.getInventory().get(i));
                        }
                    }
                    
                    System.out.println("\nPress Enter to return to the main menu...");
                    scanner.nextLine();
                    break;
                    
                case "7": // Trim Plants
                    if (player.getNRG() <= 0) {
                        System.out.println("You're too tired to do that. You need to go to bed first!");
                        break;
                    }
                    
                    // Check if there are any mature plants to trim
                    boolean hasTrimablePlants = false;
                    for (gardenPlot plot : player.getGardenPlots()) {
                        if (plot.isOccupied()) {
                            String stage = plot.getPlantedFlower().getGrowthStage();
                            if (stage.equals("Bloomed") || stage.equals("Matured")) {
                                hasTrimablePlants = true;
                                break;
                            }
                        }
                    }
                    
                    if (!hasTrimablePlants) {
                        System.out.println("You don't have any plants that need trimming yet!");
                        System.out.println("Plants need to be at least in the 'Bloomed' stage to be trimmed.");
                        break;
                    }
                    
                    // Display garden plots with their plants
                    System.out.println("\n🌱 Your Garden Plants 🌱");
                    gardenPlots = player.getGardenPlots();
                    List<Integer> trimablePlotIndices = new ArrayList<>();
                    
                    for (int i = 0; i < gardenPlots.size(); i++) {
                        gardenPlot plot = gardenPlots.get(i);
                        if (plot.isOccupied()) {
                            Flower plant = plot.getPlantedFlower();
                            String stage = plant.getGrowthStage();
                            System.out.println("Plot #" + (i+1) + ": " + plant.getName() + 
                                              " (" + stage + ")" + 
                                              (stage.equals("Bloomed") || stage.equals("Matured") ? 
                                               " - Can be trimmed" : ""));
                            
                            if (stage.equals("Bloomed") || stage.equals("Matured")) {
                                trimablePlotIndices.add(i);
                            }
                        } else {
                            System.out.println("Plot #" + (i+1) + ": [Empty]");
                        }
                    }
                    
                    // Ask which plot to trim
                    System.out.print("\nWhich plot would you like to trim? (");
                    for (int i = 0; i < trimablePlotIndices.size(); i++) {
                        System.out.print((trimablePlotIndices.get(i) + 1));
                        if (i < trimablePlotIndices.size() - 1) {
                            System.out.print(", ");
                        }
                    }
                    System.out.print(", or 0 to cancel): ");
                    
                    try {
                        plotChoice = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a number.");
                        break;
                    }
                    
                    if (plotChoice == 0) {
                        System.out.println("Trimming cancelled.");
                        break;
                    }
                    
                    if (plotChoice < 1 || plotChoice > gardenPlots.size() || 
                        !trimablePlotIndices.contains(plotChoice - 1)) {
                        System.out.println("Invalid plot choice. Please select a plot with a trimable plant.");
                        break;
                    }
                    
                    // Trim the plant
                    selectedPlot = gardenPlots.get(plotChoice - 1);
                    Flower plant = selectedPlot.getPlantedFlower();
                    
                    // Increase durability slightly when trimmed
                    plant.setDurability(plant.getDurability() + 2);
                    
                    // Use energy
                    player.setNRG(player.getNRG() - 1);
                    
                    System.out.println("You carefully trim the " + plant.getName() + ".");
                    System.out.println("It looks healthier now! Durability increased.");
                    System.out.println("You used 1 NRG. Remaining NRG: " + player.getNRG());
                    
                    Journal.addJournalEntry(player, "Trimmed a " + plant.getName() + " in plot #" + plotChoice + ".");
                    break;
                    
                case "8": // Check
                    System.out.println("\n🌱 Checking your garden... 🌱");
                    
                    List<gardenPlot> plots = player.getGardenPlots();
                    if (plots.isEmpty()) {
                        System.out.println("You don't have any garden plots yet!");
                    } else {
                        player.printGarden();
                        
                        // Offer actions on specific plots
                        System.out.println("\nWould you like to perform an action on a specific plot?");
                        System.out.println("1: Water a plot");
                        System.out.println("2: Weed a plot");
                        System.out.println("3: Fertilize a plot");
                        System.out.println("4: Harvest a plant");
                        System.out.println("0: Return to main menu");
                        
                        System.out.print("\nEnter your choice: ");
                        String gardenAction = scanner.nextLine();
                        
                        if (gardenAction.equals("0")) {
                            break;
                        }
                        
                        // Energy check for all garden actions
                        if (player.getNRG() <= 0) {
                            System.out.println("You're too tired to do that. You need to go to bed first!");
                            break;
                        }
                        
                        // Ask which plot to perform action on
                        System.out.print("\nWhich plot would you like to work with? (1-" + plots.size() + "): ");

                        try {
                            plotChoice = Integer.parseInt(scanner.nextLine());
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input. Please enter a number.");
                            break;
                        }
                        
                        if (plotChoice < 1 || plotChoice > plots.size()) {
                            System.out.println("Invalid plot number. Please choose a valid plot.");
                            break;
                        }
                        
                        selectedPlot = plots.get(plotChoice - 1);
                        
                        switch (gardenAction) {
                            case "1": // Water
                                if (selectedPlot.isOccupied()) {
                                    if (selectedPlot.isWatered()) {
                                        System.out.println("This plot is already watered today!");
                                    } else {
                                        selectedPlot.waterPlot();
                                        System.out.println("You watered the " + 
                                                          selectedPlot.getPlantedFlower().getName() + ".");
                                        player.setNRG(player.getNRG() - 1);
                                        System.out.println("You used 1 NRG. Remaining NRG: " + player.getNRG());
                                        Journal.addJournalEntry(player, "Watered a " + 
                                                              selectedPlot.getPlantedFlower().getName() + ".");
                                    }
                                } else {
                                    System.out.println("There's nothing planted in this plot to water!");
                                }
                                break;
                                
                            case "2": // Weed
                                if (!selectedPlot.isWeeded()) {
                                    selectedPlot.weedPlot();
                                    System.out.println("You removed the weeds from plot #" + plotChoice + ".");
                                    player.setNRG(player.getNRG() - 1);
                                    System.out.println("You used 1 NRG. Remaining NRG: " + player.getNRG());
                                    Journal.addJournalEntry(player, "Weeded plot #" + plotChoice + ".");
                                } else {
                                    System.out.println("This plot is already free of weeds!");
                                }
                                break;
                                
                            case "3": // Fertilize
                                if (selectedPlot.isOccupied()) {
                                    if (selectedPlot.isFertilized()) {
                                        System.out.println("This plot is already fertilized!");
                                    } else {
                                        selectedPlot.fertilizePlot();
                                        System.out.println("You fertilized the " + 
                                                          selectedPlot.getPlantedFlower().getName() + ".");
                                        player.setNRG(player.getNRG() - 1);
                                        System.out.println("You used 1 NRG. Remaining NRG: " + player.getNRG());
                                        Journal.addJournalEntry(player, "Fertilized a " + 
                                                              selectedPlot.getPlantedFlower().getName() + ".");
                                    }
                                } else {
                                    System.out.println("There's nothing planted in this plot to fertilize!");
                                }
                                break;
                                
                            case "4": // Harvest
                                if (selectedPlot.isOccupied()) {
                                    plant = selectedPlot.getPlantedFlower();
                                    String growthStage = plant.getGrowthStage();
                                    
                                    if (growthStage.equals("Seed") || growthStage.equals("Seedling")) {
                                        System.out.println("This plant is too young to harvest!");
                                    } else {
                                        // Harvest the plant
                                        Flower harvestedFlower = selectedPlot.harvestFlower();
                                        player.addToInventory(harvestedFlower);
                                        
                                        System.out.println("You harvested the " + harvestedFlower.getName() + 
                                                          " (" + harvestedFlower.getGrowthStage() + ").");
                                        System.out.println("It has been added to your inventory.");
                                        
                                        player.setNRG(player.getNRG() - 2);
                                        System.out.println("You used 2 NRG. Remaining NRG: " + player.getNRG());
                                        Journal.addJournalEntry(player, "Harvested a " + harvestedFlower.getName() + 
                                                              " (" + harvestedFlower.getGrowthStage() + ").");
                                    }
                                } else {
                                    System.out.println("There's nothing planted in this plot to harvest!");
                                }
                                break;
                                
                            default:
                                System.out.println("Invalid choice! Please try again.");
                        }
                    }
                    break;
                    
                case "9": // Journal
                    boolean inJournal = true;
                    int currentPage = 0; // Start at first page
                    int totalPages = Math.max(1, (int)Math.ceil((double)player.getJournalEntries().size() / Journal.ENTRIES_PER_PAGE));
                    
                    while (inJournal) {
                        System.out.println("\n📓 Journal Menu 📓");
                        System.out.println("1. View Journal Entries");
                        System.out.println("2. Add New Entry");
                        System.out.println("3. Save Game / Exit");
                        System.out.println("4. Return to Main Menu");
                        System.out.println("5. Reset Game (New Game+)");
                        
                        System.out.print("\nEnter your choice: ");
                        String journalChoice = scanner.next();
                        scanner.nextLine(); // Clear buffer
                        
                        switch (journalChoice) {
                            case "1":
                                // View journal entries with pagination
                                boolean viewingEntries = true;
                                
                                // Recalculate total pages based on current journal entries
                                List<String> allEntries = player.getJournalEntries();
                                totalPages = Math.max(1, (int)Math.ceil((double)allEntries.size() / Journal.ENTRIES_PER_PAGE));
                                
                                while (viewingEntries) {
                                    System.out.println("\n=== Journal Entries (Page " + (currentPage + 1) + " of " + totalPages + ") ===");
                                    
                                    // Calculate indices for current page
                                    int startIndex = currentPage * Journal.ENTRIES_PER_PAGE;
                                    int endIndex = Math.min(startIndex + Journal.ENTRIES_PER_PAGE, allEntries.size());
                                    
                                    if (allEntries.isEmpty()) {
                                        System.out.println("No journal entries yet. Add some with option 2!");
                                        viewingEntries = false;
                                        System.out.println("\nPress Enter to continue...");
                                        scanner.nextLine();
                                        break;
                                    } else if (startIndex >= allEntries.size()) {
                                        System.out.println("No entries on this page.");
                                        currentPage = 0; // Reset to first page
                                        continue;
                                    } else {
                                        // Display entries for current page
                                        for (int i = startIndex; i < endIndex; i++) {
                                            System.out.println(allEntries.get(i));
                                        }
                                    }
                                    
                                    // Navigation options
                                    System.out.println("\nNavigation:");
                                    if (currentPage > 0) {
                                        System.out.println("P: Previous Page");
                                    }
                                    if (currentPage < totalPages - 1 && endIndex < allEntries.size()) {
                                        System.out.println("N: Next Page");
                                    }
                                    System.out.println("B: Back to Journal Menu");
                                    
                                    System.out.print("\nEnter your choice: ");
                                    String pageChoice = scanner.next().toUpperCase();
                                    scanner.nextLine(); // Clear buffer
                                    
                                    switch (pageChoice) {
                                        case "P":
                                            if (currentPage > 0) {
                                                currentPage--;
                                            }
                                            break;
                                        case "N":
                                            if (currentPage < totalPages - 1 && endIndex < allEntries.size()) {
                                                currentPage++;
                                            }
                                            break;
                                        case "B":
                                            viewingEntries = false;
                                            break;
                                        default:
                                            System.out.println("Invalid choice. Please try again.");
                                    }
                                }
                                break;
                                
                            case "2":
                                // Add new entry
                                System.out.println("\nWrite a new journal entry:");
                                String newEntry = scanner.nextLine();
                                if (Journal.addJournalEntry(player, newEntry)) {
                                    System.out.println("Journal entry added successfully!");
                                    
                                    // Save game immediately after adding entry to ensure it persists
                                    Journal.saveGame(player);
                                    
                                    // Recalculate total pages
                                    totalPages = Math.max(1, (int)Math.ceil((double)player.getJournalEntries().size() / Journal.ENTRIES_PER_PAGE));
                                } else {
                                    System.out.println("Failed to add journal entry.");
                                }
                                break;
                                
                            case "3":
                                // Save game / Exit submenu
                                System.out.println("\nSave / Exit Options:");
                                System.out.println("1. Save Game and Continue");
                                System.out.println("2. Save Game and Exit");
                                System.out.print("\nEnter your choice: ");
                                String saveChoice = scanner.next();
                                scanner.nextLine(); // Clear buffer
                                
                                switch (saveChoice) {
                                    case "1":
                                        // Save and continue
                                        if (Journal.saveGame(player)) {
                                            System.out.println("Game saved successfully!");
                                        } else {
                                            System.out.println("Failed to save game.");
                                        }
                                        break;
                                    case "2":
                                        // Save and exit
                                        System.out.println("Saving game and exiting...");
                                        Journal.addJournalEntry(player, "Ended gardening session on day " + player.getDay() + ".");
                                        if (Journal.saveGame(player)) {
                                            System.out.println("Game saved successfully. Thanks for playing!");
                                        } else {
                                            System.out.println("Warning: There was an issue saving the game.");
                                            System.out.println("Exiting anyway. Thanks for playing!");
                                        }
                                        inJournal = false;
                                        gameContinues = false; // Exit the main game loop
                                        break;
                                    default:
                                        System.out.println("Invalid choice. Returning to Journal Menu.");
                                }
                                break;
                                
                            case "4":
                                inJournal = false;
                                break;
                                
                            case "5":
                                // Reset Game (New Game+) option
                                System.out.println("\n⚠️ WARNING: This will reset your game while keeping your name! ⚠️");
                                System.out.println("All progress, inventory items, and stats will be reset to default values.");
                                System.out.println("This cannot be undone. Your previous save will be overwritten.");
                                System.out.print("\nAre you sure you want to reset? (yes/no): ");
                                String confirmReset = scanner.next().toLowerCase();
                                scanner.nextLine(); // Clear buffer
                                
                                if (confirmReset.equals("yes")) {
                                    // Save the player's name
                                    playerName = player.getName();
                                    
                                    // Create a new player with the same name (reset everything else)
                                    player = new Player1(playerName);
                                    
                                    // Add a starting flower seed to the player's inventory (just like new game)
                                    MammothSunflower starterSeed = new MammothSunflower(
                                        "Mammoth Sunflower", "Seed", 0, 10, 1, 5);
                                    player.addToInventory(starterSeed);
                                    
                                    // Use our special reset method to completely wipe the old save
                                    Journal.resetGame(player);
                                    Journal.addJournalEntry(player, "Started a new adventure! (New Game+)");
                                    
                                    System.out.println("\n🔄 Game has been reset successfully!");
                                    System.out.println("Welcome to your new adventure, " + playerName + "!");
                                    System.out.println("It is day " + player.getDay() + ".");
                                    System.out.println("You have " + player.getNRG() + " NRG and " + player.getCredits() + " credits.");
                                    System.out.println("A new Mammoth Sunflower seed has been added to your inventory.");
                                    
                                    // Exit journal menu to return to main game with reset player
                                    inJournal = false;
                                } else {
                                    System.out.println("Reset cancelled. Your game remains unchanged.");
                                }
                                break;
                                
                            default:
                                System.out.println("Invalid choice. Please try again.");
                        }
                    }
                    break;
                    
                case "X":
                case "x": // Save & Exit
                    System.out.println("Saving game and exiting...");
                    Journal.addJournalEntry(player, "Ended gardening session on day " + player.getDay() + ".");
                    if (Journal.saveGame(player)) {
                        System.out.println("Game saved successfully. Thanks for playing!");
                    } else {
                        System.out.println("Warning: There was an issue saving the game.");
                        System.out.println("Exiting anyway. Thanks for playing!");
                    }
                    gameContinues = false; // Exit the main game loop
                    break;
                    
                default:
                    System.out.println("Invalid choice! Please try again.");
                    break;
            }
            
            // Check if player ran out of energy
            if (player.getNRG() <= 0) {
                System.out.println("\nYou've run out of energy! You need to go to bed (option 0) or save & exit (option X).");
            }
            
        } while (gameContinues);
        
        // Close the scanner
        scanner.close();
    }
}
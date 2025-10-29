/* DreamReader.java
 * Loads and randomly selects dream text files
 * Dreams are displayed when the player goes to sleep
 */

import java.io.*;
import java.util.*;

public class DreamReader {
    private static final String DREAM_DIRECTORY = "dream.txt/";
    private static List<String> dreamFiles = new ArrayList<>();
    private static Map<String, String> dreamCache = new HashMap<>();
    private static boolean isLoaded = false;
    private static Random random = new Random();
    
    /**
     * Loads all dream file names from the dream.txt directory
     */
    public static void loadDreamFiles() {
        if (isLoaded) {
            return;
        }
        
        File dreamDir = new File(DREAM_DIRECTORY);
        
        if (!dreamDir.exists() || !dreamDir.isDirectory()) {
            System.err.println("[DEBUG] Dream directory not found at: " + DREAM_DIRECTORY);
            isLoaded = true; // Mark as loaded even if empty to avoid repeated attempts
            return;
        }
        
        File[] files = dreamDir.listFiles((dir, name) -> name.endsWith(".txt"));
        
        if (files == null || files.length == 0) {
            System.err.println("[DEBUG] No dream files found in " + DREAM_DIRECTORY);
            isLoaded = true;
            return;
        }
        
        for (File file : files) {
            dreamFiles.add(file.getName());
        }
        
        System.out.println("[DEBUG] Loaded " + dreamFiles.size() + " dream files.");
        isLoaded = true;
    }
    
    /**
     * Reads a specific dream file and caches it
     * @param filename The name of the dream file
     * @return The dream text, or null if file not found
     */
    private static String readDreamFile(String filename) {
        // Check cache first
        if (dreamCache.containsKey(filename)) {
            return dreamCache.get(filename);
        }
        
        try (BufferedReader reader = new BufferedReader(
                new FileReader(DREAM_DIRECTORY + filename))) {
            StringBuilder dream = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                dream.append(line).append("\n");
            }
            
            String dreamText = dream.toString().trim();
            dreamCache.put(filename, dreamText);
            return dreamText;
            
        } catch (IOException e) {
            System.err.println("[DEBUG] Error reading dream file: " + filename);
            return null;
        }
    }
    
    /**
     * Gets a random dream text
     * @param chanceOfDream Percentage chance (0-100) that a dream occurs
     * @return Dream text if dream occurs, null if no dream or no files available
     */
    public static String getRandomDream(int chanceOfDream) {
        if (!isLoaded) {
            loadDreamFiles();
        }
        
        if (dreamFiles.isEmpty()) {
            return null; // No dreams available
        }
        
        // Roll for dream chance
        if (random.nextInt(100) >= chanceOfDream) {
            return null; // No dream tonight
        }
        
        // Select random dream file
        String randomFile = dreamFiles.get(random.nextInt(dreamFiles.size()));
        return readDreamFile(randomFile);
    }
    
    /**
     * Convenience method with default 50% dream chance
     * @return Dream text or null
     */
    public static String getRandomDream() {
        return getRandomDream(50);
    }
    
    /**
     * Checks if dreams are available
     * @return true if dream files were found
     */
    public static boolean hasDreams() {
        if (!isLoaded) {
            loadDreamFiles();
        }
        return !dreamFiles.isEmpty();
    }
    
    /**
     * Gets the number of available dream files
     * @return Number of dream files
     */
    public static int getDreamCount() {
        if (!isLoaded) {
            loadDreamFiles();
        }
        return dreamFiles.size();
    }
}
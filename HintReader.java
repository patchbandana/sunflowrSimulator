/* HintReader.java
 * Loads and randomly selects hint text files
 * Hints are displayed in dreams when certain conditions are met
 * Similar structure to DreamReader.java
 */

import java.io.*;
import java.util.*;

public class HintReader {
    private static final String HINT_DIRECTORY = "hints.txt/";
    private static List<String> hintFiles = new ArrayList<>();
    private static Map<String, String> hintCache = new HashMap<>();
    private static boolean isLoaded = false;
    private static Random random = new Random();
    
    /**
     * Loads all hint file names from the hints.txt directory
     */
    public static void loadHintFiles() {
        if (isLoaded) {
            return;
        }
        
        File hintDir = new File(HINT_DIRECTORY);
        
        if (!hintDir.exists() || !hintDir.isDirectory()) {
            System.err.println("[DEBUG] Hint directory not found at: " + HINT_DIRECTORY);
            isLoaded = true; // Mark as loaded even if empty to avoid repeated attempts
            return;
        }
        
        File[] files = hintDir.listFiles((dir, name) -> name.endsWith(".txt"));
        
        if (files == null || files.length == 0) {
            System.err.println("[DEBUG] No hint files found in " + HINT_DIRECTORY);
            isLoaded = true;
            return;
        }
        
        for (File file : files) {
            hintFiles.add(file.getName());
        }
        
        System.out.println("[DEBUG] Loaded " + hintFiles.size() + " hint files.");
        isLoaded = true;
    }
    
    /**
     * Reads a specific hint file and caches it
     * @param filename The name of the hint file
     * @return The hint text, or null if file not found
     */
    private static String readHintFile(String filename) {
        // Check cache first
        if (hintCache.containsKey(filename)) {
            return hintCache.get(filename);
        }
        
        try (BufferedReader reader = new BufferedReader(
                new FileReader(HINT_DIRECTORY + filename))) {
            StringBuilder hint = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                hint.append(line).append("\n");
            }
            
            String hintText = hint.toString().trim();
            hintCache.put(filename, hintText);
            return hintText;
            
        } catch (IOException e) {
            System.err.println("[DEBUG] Error reading hint file: " + filename);
            return null;
        }
    }
    
    /**
     * Gets a random hint text
     * @param chanceOfHint Percentage chance (0-100) that a hint occurs
     * @return Hint text if hint occurs, null if no hint or no files available
     */
    public static String getRandomHint(int chanceOfHint) {
        if (!isLoaded) {
            loadHintFiles();
        }
        
        if (hintFiles.isEmpty()) {
            return null; // No hints available
        }
        
        // Roll for hint chance
        if (random.nextInt(100) >= chanceOfHint) {
            return null; // No hint tonight
        }
        
        // Select random hint file
        String randomFile = hintFiles.get(random.nextInt(hintFiles.size()));
        return readHintFile(randomFile);
    }
    
    /**
     * Gets a specific hint by filename
     * @param filename The hint filename (without directory path)
     * @return Hint text or null if not found
     */
    public static String getSpecificHint(String filename) {
        if (!isLoaded) {
            loadHintFiles();
        }
        
        if (!hintFiles.contains(filename)) {
            return null;
        }
        
        return readHintFile(filename);
    }
    
    /**
     * Checks if hints are available
     * @return true if hint files were found
     */
    public static boolean hasHints() {
        if (!isLoaded) {
            loadHintFiles();
        }
        return !hintFiles.isEmpty();
    }
    
    /**
     * Gets the number of available hint files
     * @return Number of hint files
     */
    public static int getHintCount() {
        if (!isLoaded) {
            loadHintFiles();
        }
        return hintFiles.size();
    }
}
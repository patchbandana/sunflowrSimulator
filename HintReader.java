/* HintReader.java - FIXED VERSION
 * Removed all DEBUG print statements
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
     * FIXED: Removed DEBUG print statements
     */
    public static void loadHintFiles() {
        if (isLoaded) {
            return;
        }
        
        File hintDir = new File(HINT_DIRECTORY);
        
        if (!hintDir.exists() || !hintDir.isDirectory()) {
            // Silent fail - directory not found
            isLoaded = true;
            return;
        }
        
        File[] files = hintDir.listFiles((dir, name) -> name.endsWith(".txt"));
        
        if (files == null || files.length == 0) {
            // Silent fail - no hint files found
            isLoaded = true;
            return;
        }
        
        for (File file : files) {
            hintFiles.add(file.getName());
        }
        
        // REMOVED: System.out.println("[DEBUG] Loaded " + hintFiles.size() + " hint files.");
        isLoaded = true;
    }
    
    /**
     * Reads a specific hint file and caches it
     * FIXED: Removed DEBUG print statements
     */
    public static String readHintFile(String filename) {
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
            // REMOVED: System.err.println("[DEBUG] Error reading hint file: " + filename);
            return null;
        }
    }
    
    /**
     * Gets a random hint text and returns the filename for tracking
     */
    public static String[] getRandomHintWithFilename() {
        if (!isLoaded) {
            loadHintFiles();
        }
        
        if (hintFiles.isEmpty()) {
            return null;
        }
        
        String randomFile = hintFiles.get(random.nextInt(hintFiles.size()));
        String content = readHintFile(randomFile);
        
        if (content == null) {
            return null;
        }
        
        return new String[]{randomFile, content};
    }
    
    /**
     * Gets unlocked hints with their filenames for display
     */
    public static Map<String, String> getUnlockedHintsWithTitles(Set<String> unlockedHints) {
        Map<String, String> hintFilenames = new LinkedHashMap<>();
        
        List<String> sortedHints = new ArrayList<>(unlockedHints);
        Collections.sort(sortedHints);
        
        for (String filename : sortedHints) {
            hintFilenames.put(filename, filename);
        }
        return hintFilenames;
    }
    
    /**
     * Gets a specific hint by filename
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
     */
    public static boolean hasHints() {
        if (!isLoaded) {
            loadHintFiles();
        }
        return !hintFiles.isEmpty();
    }
    
    /**
     * Gets the number of available hint files
     */
    public static int getHintCount() {
        if (!isLoaded) {
            loadHintFiles();
        }
        return hintFiles.size();
    }
}
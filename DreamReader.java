/* DreamReader.java
 * Loads and randomly selects dream text files
 * Dreams are displayed when the player goes to sleep
 * * FINAL FIX: Simplifies dream display to use filenames as titles and returns full content.
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
            isLoaded = true;
            return;
        }
        
        File[] files = dreamDir.listFiles((dir, name) -> name.endsWith(".txt"));
        
        if (files == null || files.length == 0) {
            isLoaded = true;
            return;
        }
        
        for (File file : files) {
            dreamFiles.add(file.getName());
        }
        
        isLoaded = true;
    }
    
    /**
     * Reads a specific dream file and caches it
     * @param filename The name of the dream file
     * @return The dream text, or null if file not found
     */
    public static String readDreamFile(String filename) {
        // Check cache first
        if (dreamCache.containsKey(filename)) {
            return dreamCache.get(filename);
        }
        
        try (BufferedReader reader = new BufferedReader(
                new FileReader(DREAM_DIRECTORY + filename, java.nio.charset.StandardCharsets.UTF_8))) {
            StringBuilder dream = new StringBuilder();
            String line;
            
            // Read all lines and append them, preserving original formatting
            while ((line = reader.readLine()) != null) {
                dream.append(line).append("\n"); 
            }
            
            String dreamText = dream.toString().trim();
            dreamCache.put(filename, dreamText);
            return dreamText;
            
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Gets a map of unlocked dream filenames, using the filename itself as the display name.
     * This restores the behavior requested by the user.
     * @param unlockedDreams Set of dream file names the player has unlocked.
     * @return A Map<String, String> where key is filename and value is the filename (for display).
     */
    public static Map<String, String> getUnlockedDreamsWithTitles(Set<String> unlockedDreams) {
        Map<String, String> dreamFilenames = new LinkedHashMap<>();
        
        // Ensure the dreams are displayed in a consistent, sorted order
        List<String> sortedDreams = new ArrayList<>(unlockedDreams);
        Collections.sort(sortedDreams); 

        for (String filename : sortedDreams) {
            // Map the filename to itself for display
            dreamFilenames.put(filename, filename); 
        }
        return dreamFilenames;
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
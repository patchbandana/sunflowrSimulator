/* Mantle.java
 * Decorative display furniture for showcasing bouquet accomplishments.
 */

import java.util.ArrayList;
import java.util.List;

public class Mantle {
    private List<Bouquet> displayedBouquets;

    public Mantle() {
        this.displayedBouquets = new ArrayList<>();
    }

    public void addBouquet(Bouquet bouquet) {
        displayedBouquets.add(bouquet);
    }

    public boolean removeBouquet(Bouquet bouquet) {
        return displayedBouquets.remove(bouquet);
    }

    public List<Bouquet> getDisplayedBouquets() {
        return new ArrayList<>(displayedBouquets);
    }

    public int getDisplayedCount() {
        return displayedBouquets.size();
    }

    public String getDisplaySummary() {
        if (displayedBouquets.isEmpty()) {
            return "🏠 Mantle is placed, but no bouquets are displayed yet.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🏠 Mantle Display (" + displayedBouquets.size() + " bouquet");
        sb.append(displayedBouquets.size() == 1 ? ")\n" : "s)\n");

        for (int i = 0; i < displayedBouquets.size(); i++) {
            Bouquet bouquet = displayedBouquets.get(i);
            sb.append("\n").append(i + 1).append(": ").append(bouquet.getDisplayName()).append("\n");
            sb.append("   Base Value: ").append((int) bouquet.getBaseValue()).append(" credits\n");
            sb.append("   Contents:\n");

            java.util.Map<String, Integer> flowerCounts = new java.util.HashMap<>();
            for (Flower flower : bouquet.getFlowers()) {
                String key = flower.getName() + " (" + flower.getGrowthStage() + ")";
                flowerCounts.put(key, flowerCounts.getOrDefault(key, 0) + 1);
            }

            flowerCounts.entrySet().stream()
                    .sorted(java.util.Map.Entry.comparingByKey())
                    .forEach(entry -> sb.append("      • ")
                            .append(entry.getValue())
                            .append("x ")
                            .append(entry.getKey())
                            .append("\n"));
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return "🏠 Mantle (decorative bouquet display)";
    }
}

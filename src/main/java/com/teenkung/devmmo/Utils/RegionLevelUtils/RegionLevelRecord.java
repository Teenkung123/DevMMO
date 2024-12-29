package com.teenkung.devmmo.Utils.RegionLevelUtils;

import java.util.*;

public class RegionLevelRecord {
    private final String region;
    private final int baseLevel;
    private final List<LevelWeight> levels;
    private final NavigableMap<Double, Integer> cumulativeMap;
    private final double totalWeight;

    /**
     * Constructor for RegionLevelRecord.
     * @param region The region name.
     * @param baseLevel The base level.
     * @param levels The list of levels and their weights.
     */
    public RegionLevelRecord(String region, int baseLevel, List<LevelWeight> levels) {
        this.region = region;
        this.baseLevel = baseLevel;
        this.levels = List.copyOf(levels);
        this.cumulativeMap = new TreeMap<>();
        double cumulative = 0.0;
        for (LevelWeight level : levels) {
            cumulative += level.weight();
            this.cumulativeMap.put(cumulative, level.level());
        }
        this.totalWeight = cumulative;
    }

    /**
     * Get the region name.
     * @return The region name.
     */
    public String getRegion() {
        return region;
    }

    /**
     * Get the base level.
     * @return The base level.
     */
    public int getBaseLevel() {
        return baseLevel;
    }

    /**
     * Get the list of levels and their weights.
     * @return The list of levels and their weights.
     */
    public List<LevelWeight> getLevels() {
        return levels;
    }

    /**
     * Get the cumulative map of weights to levels.
     * @return The cumulative map of weights to levels.
     */
    public NavigableMap<Double, Integer> getCumulativeMap() {
        return cumulativeMap;
    }

    /**
     * Get the total weight of all levels.
     * @return The total weight of all levels.
     */
    public double getTotalWeight() {
        return totalWeight;
    }
}

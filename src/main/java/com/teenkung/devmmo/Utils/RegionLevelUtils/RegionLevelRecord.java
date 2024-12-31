package com.teenkung.devmmo.Utils.RegionLevelUtils;

import java.util.*;

public class RegionLevelRecord {

    private final String region;
    private final int baseLevel;
    private final List<LevelWeight> levels;
    private final NavigableMap<Double, Integer> cumulativeMap;

    /**
     * Map of (mobType -> custom RegionLevelRecord)
     * for handling special weights/levels on a per-mob basis.
     */
    private final Map<String, RegionLevelRecord> customRecords = new HashMap<>();

    private final double totalWeight;

    /**
     * Constructor for RegionLevelRecord.
     *
     * @param region    The region name.
     * @param baseLevel The base level.
     * @param levels    The list of levels and their weights.
     */
    public RegionLevelRecord(String region, int baseLevel, List<LevelWeight> levels) {
        this.region = region;
        this.baseLevel = baseLevel;
        this.levels = List.copyOf(levels);

        this.cumulativeMap = new TreeMap<>();
        double cumulative = 0.0;
        for (LevelWeight lw : levels) {
            cumulative += lw.weight();
            this.cumulativeMap.put(cumulative, lw.level());
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

    /**
     * Retrieve the map of custom mob records.
     *
     * @return Map where key = mobType, value = a custom RegionLevelRecord
     */
    public Map<String, RegionLevelRecord> getCustomRecords() {
        return customRecords;
    }

    /**
     * Add or overwrite a custom record for a specific mob type.
     *
     * @param mobType      the MythicMob internal name (e.g., "example_mob")
     * @param customRecord the RegionLevelRecord that contains custom weighting for that mob
     */
    public void addCustomRecord(String mobType, RegionLevelRecord customRecord) {
        this.customRecords.put(mobType, customRecord);
    }
}

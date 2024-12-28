package com.teenkung.devmmo.Utils.RegionLevelUtils;

import java.util.*;

public class RegionLevelRecord {
    private final String region;
    private final int baseLevel;
    private final List<LevelWeight> levels;
    private final NavigableMap<Double, Integer> cumulativeMap;
    private final double totalWeight;

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

    public String getRegion() {
        return region;
    }

    public int getBaseLevel() {
        return baseLevel;
    }

    public List<LevelWeight> getLevels() {
        return levels;
    }

    public NavigableMap<Double, Integer> getCumulativeMap() {
        return cumulativeMap;
    }

    public double getTotalWeight() {
        return totalWeight;
    }
}

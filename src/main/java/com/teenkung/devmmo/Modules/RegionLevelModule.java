package com.teenkung.devmmo.Modules;

import com.teenkung.devmmo.DevMMO;
import com.teenkung.devmmo.Utils.RegionLevelUtils.LevelWeight;
import com.teenkung.devmmo.Utils.RegionLevelUtils.RegionLevelRecord;
import com.teenkung.devmmo.Utils.WorldGuardUtils;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class RegionLevelModule implements Listener {

    private final DevMMO plugin;
    private final Map<String, RegionLevelRecord> regionLevelRecords = new HashMap<>();
    private boolean debugMode = false;

    public RegionLevelModule(DevMMO plugin) {
        this.plugin = plugin;
        if (plugin.getConfigLoader().isModuleEnabled("RegionLevel")) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            loadRegionLevelConfig();
        }
    }

    @EventHandler
    public void onSpawn(MythicMobSpawnEvent event) {
        Location location = event.getLocation();
        for (Map.Entry<String, RegionLevelRecord> entry : regionLevelRecords.entrySet()) {
            String region = entry.getKey();
            RegionLevelRecord record = entry.getValue();
            if (WorldGuardUtils.isInRegion(location, region)) {
                event.setMobLevel(getWeightedLevel(record));
                break;
            }
        }

        event.getMob().setDisplayName("&aLv.&e"+Double.valueOf(event.getMobLevel()).intValue()+" &r&f"+event.getMob().getDisplayName());
        event.getMob().getEntity().setNoDamageTicks(1);
    }


    /**
     * This method selects a random level based on the weighted chances defined in the record.
     *
     * @param record The record of the region.
     * @return The level of the mob.
     */
    public int getWeightedLevel(RegionLevelRecord record) {
        if (record.getTotalWeight() == 0.0) {
            return record.getBaseLevel();
        }

        double rand = ThreadLocalRandom.current().nextDouble() * record.getTotalWeight();
        return record.getCumulativeMap().ceilingEntry(rand).getValue();
    }

    /**
     * Load the RegionLevel config.
     */
    public void loadRegionLevelConfig() {
        ConfigurationSection config = plugin.getConfigLoader().getRegionLevelConfig().getConfigurationSection("RegionLevel");
        if (config == null) {
            plugin.getLogger().warning("RegionsLevel config not found!");
            return;
        }

        debugMode = config.getBoolean("DebugMode", false);
        if (debugMode) {
            plugin.getLogger().info("Debug Mode enabled for RegionLevelModule!");
        }

        ConfigurationSection regions = config.getConfigurationSection("Regions");
        if (regions == null) {
            plugin.getLogger().warning("RegionsLevel Regions config not found!");
            return;
        }

        for (String key : regions.getKeys(false)) {
            plugin.getLogger().info("Loading Region: " + key);
            ConfigurationSection weightSection = regions.getConfigurationSection(key + ".Weight");
            if (weightSection == null) {
                plugin.getLogger().warning("RegionsLevel Regions " + key + " Weight config not found!");
                continue;
            }

            List<LevelWeight> weights = new ArrayList<>();
            double totalWeight = 0.0;

            for (String level : weightSection.getKeys(false)) {
                if (level.contains("-")) {
                    String[] split = level.split("-");
                    if (split.length != 2) {
                        plugin.getLogger().warning("Invalid range format for level: " + level + " in region: " + key);
                        continue;
                    }
                    try {
                        int min = Integer.parseInt(split[0]);
                        int max = Integer.parseInt(split[1]);
                        if (min > max) {
                            plugin.getLogger().warning("Min level greater than max level in range: " + level + " in region: " + key);
                            continue;
                        }
                        double rangeWeight = weightSection.getDouble(level);
                        if (rangeWeight < 0) {
                            plugin.getLogger().warning("Negative weight for range: " + level + " in region: " + key);
                            continue;
                        }
                        int rangeSize = max - min + 1;
                        double perLevelWeight = rangeWeight / rangeSize;

                        for (int i = min; i <= max; i++) {
                            weights.add(new LevelWeight(i, perLevelWeight));
                            totalWeight += perLevelWeight;
                        }
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("Invalid number format in range: " + level + " in region: " + key);
                    }
                } else {
                    try {
                        int singleLevel = Integer.parseInt(level);
                        double levelWeight = weightSection.getDouble(level);
                        if (levelWeight < 0) {
                            plugin.getLogger().warning("Negative weight for level: " + level + " in region: " + key);
                            continue;
                        }
                        weights.add(new LevelWeight(singleLevel, levelWeight));
                        totalWeight += levelWeight;
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("Invalid level number: " + level + " in region: " + key);
                    }
                }
            }

            if (weights.isEmpty()) {
                plugin.getLogger().warning("No valid weights found for region: " + key);
                continue;
            }

            RegionLevelRecord record = new RegionLevelRecord(key, regions.getInt(key + ".BaseLevel"), weights);
            regionLevelRecords.put(key, record);

            plugin.getLogger().info("Total Weight for region " + key + ": " + totalWeight);
        }
        plugin.getLogger().info("Region Level Config loaded!");
    }

}

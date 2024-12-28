package com.teenkung.devmmo.Utils;

import com.teenkung.devmmo.DevMMO;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ConfigLoader {

    private final DevMMO plugin;

    // FileConfigurations for each module
    private FileConfiguration modulesConfig;
    private FileConfiguration healthConfig;
    private FileConfiguration staminaConfig;
    private FileConfiguration fireworkBlockerConfig;
    private FileConfiguration damageTrackerConfig;
    private FileConfiguration mobXPConfig;
    private FileConfiguration expShareConfig;
    private FileConfiguration regionLevelConfig;

    public ConfigLoader(DevMMO plugin) {
        this.plugin = plugin;
        loadAllConfigs();
    }

    private void loadAllConfigs() {
        // modules.yml
        modulesConfig = loadConfig("modules.yml");

        // Each module’s own config
        healthConfig = loadConfig("Modules/HealthModule.yml");
        staminaConfig = loadConfig("Modules/StaminaModule.yml");
        fireworkBlockerConfig = loadConfig("Modules/FireworkBlocker.yml");
        damageTrackerConfig = loadConfig("Modules/DamageTracker.yml");
        mobXPConfig = loadConfig("Modules/MobXPModule.yml");
        expShareConfig = loadConfig("Modules/EXPShareModule.yml");
        regionLevelConfig = loadConfig("Modules/RegionLevelModule.yml");
    }

    /**
     * Helper method to load or create a config file.
     */
    private FileConfiguration loadConfig(String filename) {
        File file = new File(plugin.getDataFolder(), filename);

        try {
            // Ensure parent directories exist
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    plugin.getLogger().severe("Failed to create directories for " + parentDir.getPath());
                    return null;
                }
            }

            // If the file doesn't exist, save the resource
            if (!file.exists()) {
                // Check if the resource exists within the JAR
                InputStream resourceStream = plugin.getResource(filename);
                if (resourceStream == null) {
                    plugin.getLogger().severe("Resource " + filename + " not found in JAR.");
                    return null;
                }

                // Save the resource to the file
                Files.copy(resourceStream, file.toPath());
                resourceStream.close();
            }

            // Load and return the configuration
            return YamlConfiguration.loadConfiguration(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not load configuration file " + filename);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Example method to see if a module is enabled from modules.yml.
     */
    public boolean isModuleEnabled(String moduleName) {
        return modulesConfig.getBoolean(moduleName + ".Enabled", false);
    }

    // GETTERS for each module's config
    public Configuration getHealthConfig() {
        return healthConfig;
    }

    public Configuration getStaminaConfig() {
        return staminaConfig;
    }

    public Configuration getFireworkBlockerConfig() {
        return fireworkBlockerConfig;
    }

    public Configuration getDamageTrackerConfig() {
        return damageTrackerConfig;
    }

    public Configuration getMobXPConfig() {
        return mobXPConfig;
    }

    public Configuration getExpShareConfig() {
        return expShareConfig;
    }

    public Configuration getRegionLevelConfig() {
        return regionLevelConfig;
    }
}

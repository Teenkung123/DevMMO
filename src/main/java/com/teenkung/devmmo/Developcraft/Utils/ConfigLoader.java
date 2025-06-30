package com.teenkung.devmmo.Developcraft.Utils;

import com.teenkung.devmmo.DevMMO;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ConfigLoader {

    private final DevMMO plugin;

    // FileConfigurations for each module
    private FileConfiguration modulesConfig;
    private FileConfiguration spawnerLimiterConfig;

    public ConfigLoader(DevMMO plugin) {
        this.plugin = plugin;
        loadAllConfigs();
    }

    /**
     * Loads all configuration files.
     */
    private void loadAllConfigs() {
        // modules.yml
        modulesConfig = loadConfig("Modules/Developcraft/modules.yml");

        // Each module’s own config
        spawnerLimiterConfig = loadConfig("Modules/Developcraft/SpawnerLimiter.yml");
    }

    /**
     * Loads a configuration file from the plugin's data folder.
     *
     * @param filename The name of the file to load.
     * @return The loaded FileConfiguration, or null if an error occurred.
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

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            try (InputStream defStream = plugin.getResource(filename)) {
                if (defStream != null) {
                    FileConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream, StandardCharsets.UTF_8));
                    boolean changed = mergeDefaults(config, defConfig);
                    if (changed) {
                        config.save(file);
                    }
                }
            }

            return config;
        } catch (IOException e) {
            plugin.getLogger().severe("Could not load configuration file " + filename);
            return null;
        }
    }

    private boolean mergeDefaults(FileConfiguration config, FileConfiguration defaults) {
        boolean changed = false;
        for (String key : defaults.getKeys(true)) {
            if (!config.contains(key)) {
                config.set(key, defaults.get(key));
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Check if a module is enabled in the modules.yml configuration.
     *
     * @param moduleName The name of the module to check.
     * @return True if the module is enabled, false otherwise.
     */
    public boolean isModuleEnabled(String moduleName) {
        return modulesConfig.getBoolean(moduleName + ".Enabled", false);
    }

    /**
     * Gets the FileConfiguration for SpawnerLimiter module.
     * @return The SpawnerLimiter configuration.
     */
    public FileConfiguration getSpawnerLimiterConfig() {
        return spawnerLimiterConfig;
    }
}
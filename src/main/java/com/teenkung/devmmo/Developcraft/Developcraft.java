package com.teenkung.devmmo.Developcraft;

import com.teenkung.devmmo.DevMMO;
import com.teenkung.devmmo.Developcraft.Utils.ConfigLoader;

@SuppressWarnings("FieldCanBeLocal")
public class Developcraft {

    private final DevMMO plugin;
    private ConfigLoader configLoader;

    private SpawnerLimiter spawnerLimiter;

    public Developcraft(DevMMO plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        this.configLoader = new ConfigLoader(plugin);
        spawnerLimiter = new SpawnerLimiter(this);
    }

    public void unloadAll() {

    }

    public DevMMO getPlugin() {
        return plugin;
    }

    public ConfigLoader getConfigLoader() {
        return configLoader;
    }

}

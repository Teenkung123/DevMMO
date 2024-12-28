package com.teenkung.devmmo;

import com.teenkung.devmmo.Commands.MainCommand.MainCommand;
import com.teenkung.devmmo.Commands.SpawnMythicMobs.SpawnMythicMobs;
import com.teenkung.devmmo.Commands.SpawnMythicMobs.SpawnMythicMobsTab;
import com.teenkung.devmmo.Modules.*;
import com.teenkung.devmmo.Utils.ConfigLoader;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;


public class DevMMO extends JavaPlugin {

    private ConfigLoader configLoader;

    // Module references
    private HealthModule healthModule;
    private StaminaModule staminaModule;
    private FireworkBlocker fireworkBlocker;
    private DamageTracker damageTracker;
    private MobXPModule mobXPModule;
    private EXPShareModule expShareModule;
    private RegionLevelModule regionLevelModule;

    @Override
    public void onEnable() {
        // Initial load
        loadAll();

        Objects.requireNonNull(getCommand("spawn-mythicmobs")).setExecutor(new SpawnMythicMobs());
        Objects.requireNonNull(getCommand("spawn-mythicmobs")).setTabCompleter(new SpawnMythicMobsTab());
        new MainCommand(this);
    }

    @Override
    public void onDisable() {
        // If you have any cleanup, do it here
        unloadAll();
    }

    /**
     * Loads (or reloads) all configurations and modules.
     */
    public void loadAll() {
        // 1. Load config(s)
        this.configLoader = new ConfigLoader(this);

        // 2. Instantiate modules
        this.healthModule = new HealthModule(this);
        this.staminaModule = new StaminaModule(this);
        this.fireworkBlocker = new FireworkBlocker(this);
        this.damageTracker = new DamageTracker(this);
        this.mobXPModule = new MobXPModule(this, damageTracker);
        this.expShareModule = new EXPShareModule(this, mobXPModule);
        this.regionLevelModule = new RegionLevelModule(this);

        // If your stamina module, for example, creates sprint tasks for each online player:
        getServer().getOnlinePlayers().forEach(player -> {
            staminaModule.addSprintTaskPlayer(player);
        });
    }

    /**
     * Optional: a cleanup method for manual unregistration/cancellation
     * if you need to re-load modules without fully unloading the plugin.
     */
    public void unloadAll() {
        HandlerList.unregisterAll(this);
        HandlerList.unregisterAll(healthModule);
        HandlerList.unregisterAll(staminaModule);
        HandlerList.unregisterAll(fireworkBlocker);
        HandlerList.unregisterAll(damageTracker);
        HandlerList.unregisterAll(mobXPModule);
        HandlerList.unregisterAll(expShareModule);
        HandlerList.unregisterAll(regionLevelModule);
        staminaModule.shutdown();
    }

    /**
     * Public method to "reload" everything
     * - Unregister events, cancel tasks, etc.
     * - Reload config
     * - Re-instantiate modules
     */
    public void reloadAll() {
        // 1. Unload existing modules
        unloadAll();

        // 2. loadAll() again
        loadAll();

        // 3. Possibly send a message to console or user
        getLogger().info("DevMMO has been reloaded!");
    }

    public ConfigLoader getConfigLoader() {
        return configLoader;
    }
}

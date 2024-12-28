package com.teenkung.devmmo.Modules;

import com.teenkung.devmmo.DevMMO;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles storing and restoring player health across logins.
 * Uses the config from HealthModule.yml (if there's any specific setting).
 */
public class HealthModule implements Listener {

    private final DevMMO plugin;
    private final Map<UUID, Double> healthCache = new HashMap<>();

    public HealthModule(DevMMO plugin) {
        this.plugin = plugin;

        // Check modules.yml to see if enabled
        if (plugin.getConfigLoader().isModuleEnabled("HealthModule")) {
            // Register events
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!healthCache.containsKey(player.getUniqueId())) {
            return;
        }
        // Possibly check config for "RestoreHealthOnJoin"
        boolean restoreHealth = plugin.getConfigLoader().getHealthConfig()
                .getBoolean("HealthModule.RestoreHealthOnJoin", true);
        if (!restoreHealth) {
            return;
        }

        Double storedHP = healthCache.get(player.getUniqueId());
        if (storedHP != null && player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            double maxHP = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            player.setHealth(Math.min(storedHP, maxHP));
        }
        healthCache.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        healthCache.put(event.getPlayer().getUniqueId(), event.getPlayer().getHealth());
    }
}

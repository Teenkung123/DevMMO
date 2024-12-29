package com.teenkung.devmmo.Modules;

import com.teenkung.devmmo.DevMMO;
import com.teenkung.devmmo.Utils.PlayerDamage;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DamageTracker implements Listener {

    private final DevMMO plugin;
    private final Map<UUID, PlayerDamage> damages = new HashMap<>();

    /**
     * Tracks how much damage each player deals to a MythicMob.
     *
     * @param plugin The DevMMO plugin instance.
     */
    public DamageTracker(DevMMO plugin) {
        this.plugin = plugin;
        if (plugin.getConfigLoader().isModuleEnabled("DamageTracker")) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    /**
     * Tracks how much damage each player deals to a MythicMob.
     *
     * @param event The EntityDamageByEntityEvent fired by Bukkit.
     */
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getConfigLoader().isModuleEnabled("DamageTracker")) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!MythicBukkit.inst().getMobManager().isMythicMob(event.getEntity())) return;

        PlayerDamage damageRecord = damages.getOrDefault(event.getEntity().getUniqueId(), new PlayerDamage());
        double oldDmg = damageRecord.getDamage(player.getUniqueId());
        damageRecord.setDamage(player.getUniqueId(), oldDmg + event.getDamage());
        damages.put(event.getEntity().getUniqueId(), damageRecord);
    }

    /**
     * Clears the damage record for a MythicMob when it despawns.
     *
     * @param event The MythicMobDespawnEvent fired by MythicMobs.
     */
    @EventHandler
    public void onDespawn(MythicMobDespawnEvent event) {
        if (!plugin.getConfigLoader().isModuleEnabled("DamageTracker")) return;
        damages.remove(event.getEntity().getUniqueId());
    }

    /**
     * Clears the damage record for a MythicMob when it dies.
     *
     * @param event The MythicMobDeathEvent fired by MythicMobs.
     */
    @EventHandler
    public void onDeath(MythicMobDeathEvent event) {
        if (!plugin.getConfigLoader().isModuleEnabled("DamageTracker")) return;
        damages.remove(event.getMob().getUniqueId());
    }

    /**
     * Gets the damage records for all MythicMobs.
     *
     * @return A map of MythicMob UUIDs to their damage records.
     */
    public Map<UUID, PlayerDamage> getDamages() {
        return damages;
    }
}

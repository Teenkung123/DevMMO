package com.teenkung.devmmo.Modules;

import com.teenkung.devmmo.DevMMO;
import com.teenkung.devmmo.Utils.PlayerDamage;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks how much damage each player deals to MythicMobs.
 * Other modules (like MobXPModule or EXPShareModule) can retrieve this map
 * to calculate how XP is awarded.
 */
public class DamageTracker implements Listener {

    private final DevMMO plugin;
    private final Map<UUID, PlayerDamage> damages = new HashMap<>();

    public DamageTracker(DevMMO plugin) {
        this.plugin = plugin;
        if (plugin.getConfigLoader().isModuleEnabled("DamageTracker")) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

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

    @EventHandler
    public void onDespawn(MythicMobDespawnEvent event) {
        if (!plugin.getConfigLoader().isModuleEnabled("DamageTracker")) return;
        damages.remove(event.getEntity().getUniqueId());
    }

    public Map<UUID, PlayerDamage> getDamages() {
        return damages;
    }
}

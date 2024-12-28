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
 * An example for a module that depends on MobXPModule.
 * If "MobXPModule" isn't enabled, this won't function properly.
 *
 * This might handle party-based XP distribution or additional XP sharing rules,
 * so you can add code as needed. We'll just show a skeleton.
 */
public class EXPShareModule implements Listener {

    private final DevMMO plugin;
    private final MobXPModule mobXPModule;
    private final HashMap<UUID, PlayerDamage> damages = new HashMap<>();

    public EXPShareModule(DevMMO plugin, MobXPModule mobXPModule) {
        this.plugin = plugin;
        this.mobXPModule = mobXPModule;

        boolean enabled = plugin.getConfigLoader().isModuleEnabled("EXPShareModule");
        boolean mobXpEnabled = plugin.getConfigLoader().isModuleEnabled("MobXPModule");
        if (enabled) {
            if (!mobXpEnabled) {
                plugin.getLogger().warning("[EXPShareModule] MobXPModule is not enabled! EXPShareModule will not function correctly.");
            } else {
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
            }
        }
    }

    /**
     * Tracks how much damage each player deals to a MythicMob.
     *
     * @param event The EntityDamageByEntityEvent fired by Bukkit.
     */
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        // Check if the damager is a player
        if (event.getDamager() instanceof Player player) {
            // Check if the entity being damaged is a MythicMob
            if (MythicBukkit.inst().getMobManager().isMythicMob(event.getEntity())) {
                // Get or create a PlayerDamage record for this mob
                PlayerDamage damageRecord = damages.getOrDefault(event.getEntity().getUniqueId(), new PlayerDamage());
                // Accumulate damage
                double oldDmg = damageRecord.getDamage(player.getUniqueId());
                damageRecord.setDamage(player.getUniqueId(), oldDmg + event.getDamage());
                damages.put(event.getEntity().getUniqueId(), damageRecord);
            }
        }
    }

    /**
     * Clears the damage record if a MythicMob despawns before dying.
     *
     * @param event MythicMobDespawnEvent from MythicMobs.
     */
    @EventHandler
    public void onDespawn(MythicMobDespawnEvent event) {
        damages.remove(event.getEntity().getUniqueId());
    }

    /**
     * Provides access to the internal damage map so other modules (e.g., AutoMobExperience)
     * can retrieve information about who dealt how much damage.
     *
     * @return The damage map.
     */
    public Map<UUID, PlayerDamage> getDamages() {
        return damages;
    }
}

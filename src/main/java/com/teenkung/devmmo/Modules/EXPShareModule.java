package com.teenkung.devmmo.Modules;

import com.teenkung.devmmo.DevMMO;
import com.teenkung.devmmo.Utils.PlayerDamage;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EXPShareModule implements Listener {

    private final DevMMO plugin;
    private final MobXPModule mobXPModule;
    private final HashMap<UUID, PlayerDamage> damages = new HashMap<>();
    private boolean debugMode;

    /**
     * Tracks how much damage each player deals to a MythicMob.
     *
     * @param plugin The DevMMO plugin instance.
     * @param mobXPModule The MobXPModule instance.
     */
    public EXPShareModule(DevMMO plugin, MobXPModule mobXPModule) {
        this.plugin = plugin;
        this.mobXPModule = mobXPModule;

        boolean enabled = plugin.getConfigLoader().isModuleEnabled("EXPShareModule");
        boolean mobXpEnabled = plugin.getConfigLoader().isModuleEnabled("MobXPModule");
        if (enabled) {
            if (!mobXpEnabled) {
                plugin.getLogger().warning("[EXPShareModule] MobXPModule is not enabled! EXPShareModule will disable itself.");
            } else {
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
                debugMode = plugin.getConfigLoader().getExpShareConfig().getBoolean("EXPShareModule.DebugMode", false);
            }
        }
    }

    /**
     * Tracks how much damage each player deals to a MythicMob.
     *
     * @param event The EntityDamageByEntityEvent fired by Bukkit.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageByEntityEvent event) {
        // Check if the damager is a player
        if (event.getDamager() instanceof Player player) {
            // Check if the entity being damaged is a MythicMob
            if (MythicBukkit.inst().getMobManager().isMythicMob(event.getEntity())) {
                // Get or create a PlayerDamage record for this mob
                PlayerDamage damageRecord = damages.getOrDefault(event.getEntity().getUniqueId(), new PlayerDamage());
                double damage = event.getFinalDamage();
                LivingEntity entity = (LivingEntity) event.getEntity();
                if (damage > entity.getHealth()) {
                    damage = entity.getHealth();
                }
                // Accumulate damage
                double oldDmg = damageRecord.getDamage(player.getUniqueId());
                damageRecord.setDamage(player.getUniqueId(), oldDmg + damage);
                damages.put(event.getEntity().getUniqueId(), damageRecord);
            }
        }
    }

    /**
     * Rewards experience to players who dealt damage to a MythicMob.
     *
     * @param event The MythicMobDeathEvent fired by MythicMobs.
     */
    @EventHandler
    public void onDeath(MythicMobDeathEvent event) {
        PlayerDamage damageRecord = damages.get(event.getMob().getUniqueId());
        if (damageRecord == null) return;
        if (debugMode) {
            damageRecord.getDamagers().forEach(uuid -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    plugin.getLogger().info("[EXPShareModule] " + player.getName() + " dealt " + damageRecord.getDamage(uuid) + " damage ( " + (damageRecord.getDamage(uuid) / damageRecord.getTotalDamage())*100 + "% ) to " + event.getMob().getUniqueId());
                }
            });
        }
        for (UUID uuid : damageRecord.getDamagers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            if (!player.isOnline()) continue;
            double percentage = damageRecord.getDamage(uuid) / damageRecord.getTotalDamage();
            double xpGain = mobXPModule.getExperienceGain(Double.valueOf(event.getMob().getLevel()).intValue()) * percentage;
            mobXPModule.rewardExperience(Bukkit.getPlayer(uuid), xpGain);
            if (debugMode) {
                plugin.getLogger().info("[EXPShareModule] " + player.getName() + " gained " + xpGain + " EXP from " + event.getMob().getUniqueId());
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

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        plugin.getConfigLoader().getExpShareConfig().set("EXPShareModule.DebugMode", debugMode);
        plugin.getConfigLoader().getExpShareConfig();
    }
}

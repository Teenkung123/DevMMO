package com.teenkung.devmmo.Modules;

import com.teenkung.devmmo.DevMMO;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles stamina drain/regen logic.
 * Uses the config from StaminaModule.yml for parameters like decrease rate, regen rate, thresholds, etc.
 */
public class StaminaModule implements Listener {

    private final DevMMO plugin;
    private final Map<Player, BukkitTask> sprintTasks = new HashMap<>();
    private final double decreaseRate;
    private final double regenRate;
    private final int lowThreshold;
    private final int exhaustedThreshold;
    private final boolean showLowTitle;
    private final int slownessDuration;
    private final int slownessLevel;
    private final boolean debugMode;

    public StaminaModule(DevMMO plugin) {
        this.plugin = plugin;
        if (plugin.getConfigLoader().isModuleEnabled("StaminaModule")) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        // Load config values
        decreaseRate = plugin.getConfigLoader().getStaminaConfig()
                .getDouble("StaminaModule.DecreaseRate", 1.0);
        regenRate = plugin.getConfigLoader().getStaminaConfig()
                .getDouble("StaminaModule.RegenRate", 1.0);
        lowThreshold = plugin.getConfigLoader().getStaminaConfig()
                .getInt("StaminaModule.LowStaminaThreshold", 5);
        exhaustedThreshold = plugin.getConfigLoader().getStaminaConfig()
                .getInt("StaminaModule.ExhaustedThreshold", 1);
        showLowTitle = plugin.getConfigLoader().getStaminaConfig()
                .getBoolean("StaminaModule.ShowLowStaminaTitle", true);
        slownessDuration = plugin.getConfigLoader().getStaminaConfig()
                .getInt("StaminaModule.ExhaustedSlownessDuration", 100);
        slownessLevel = plugin.getConfigLoader().getStaminaConfig()
                .getInt("StaminaModule.ExhaustedSlownessLevel", 2);
        debugMode = plugin.getConfigLoader().getStaminaConfig()
                .getBoolean("StaminaModule.DebugMode", false);
    }

    /**
     * Example method for creating a repeating task which drains stamina while sprinting.
     * You can call this from onEnable or onPlayerJoin, etc.
     */
    public void addSprintTaskPlayer(Player player) {
        if (!plugin.getConfigLoader().isModuleEnabled("StaminaModule")) {
            return;
        }

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                sprintTasks.remove(player);
                return;
            }
            PlayerData data = PlayerData.get(player);
            if (player.isSprinting()
                    && !player.isSneaking()
                    && !player.isFlying()
                    && player.getGameMode() != GameMode.CREATIVE
                    && player.getGameMode() != GameMode.SPECTATOR) {

                // Decrease stamina
                data.giveStamina(-decreaseRate, PlayerResourceUpdateEvent.UpdateReason.SKILL_COST);

                // Check thresholds
                if (data.getStamina() <= lowThreshold && showLowTitle) {
                    player.showTitle(Title.title(
                            Component.empty(),
                            MiniMessage.miniMessage().deserialize("<red>Low Stamina!"),
                            Title.Times.times(
                                    Duration.ofMillis(0L),
                                    Duration.ofMillis(5000L),
                                    Duration.ofMillis(100L)
                            )
                        )
                    );
                }
                if (data.getStamina() <= exhaustedThreshold) {
                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.SLOWNESS,
                            slownessDuration,
                            slownessLevel,
                            true,
                            false,
                            false
                    ));
                    player.setSprinting(false);
                }
            } else {
                // Regen stamina
                data.giveStamina(regenRate, PlayerResourceUpdateEvent.UpdateReason.REGENERATION);
            }
        }, 20, 20); // run every second

        sprintTasks.put(player, task);
    }

    public void shutdown() {
        for (BukkitTask task : sprintTasks.values()) {
            task.cancel();
        }
        sprintTasks.clear();
    }
}

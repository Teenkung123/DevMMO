package com.teenkung.devmmo.Modules;

import com.teenkung.devmmo.DevMMO;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles stamina drain/regen logic.
 * Uses the config from StaminaModule.yml for parameters like decrease rate, regen rate, thresholds, etc.
 */
public class StaminaModule implements Listener {

    private final DevMMO plugin;
    private final Map<Player, BukkitTask> sprintTasks = new HashMap<>();
    private final List<String> worlds = new ArrayList<>();
    private final long decreaseRate;
    private final int decreaseAmount;
    private final int lowThreshold;
    private final int exhaustedThreshold;
    private final boolean showLowTitle;
    private final int slownessDuration;
    private final int slownessLevel;
    private final boolean debugMode;

    private final String title;
    private final String subTitle;
    private final Long fadeIn;
    private final Long stay;
    private final Long fadeOut;

    public StaminaModule(DevMMO plugin) {
        this.plugin = plugin;
        if (plugin.getConfigLoader().isModuleEnabled("StaminaModule")) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        // Load config values
        worlds.addAll(plugin.getConfigLoader().getStaminaConfig()
                .getStringList("StaminaModule.AllowedWorlds"));
        decreaseRate = plugin.getConfigLoader().getStaminaConfig()
                .getLong("StaminaModule.DecreaseRate", 20L);
        decreaseAmount = plugin.getConfigLoader().getStaminaConfig()
                .getInt("StaminaModule.DecreaseAmount", 1);
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

        title = plugin.getConfigLoader().getStaminaConfig()
                .getString("StaminaModule.Indicator.Title", "");
        subTitle = plugin.getConfigLoader().getStaminaConfig()
                .getString("StaminaModule.Indicator.SubTitle", "<red>Low Stamina!");
        fadeIn = plugin.getConfigLoader().getStaminaConfig()
                .getLong("StaminaModule.Indicator.FadeIn", 0L);
        stay = plugin.getConfigLoader().getStaminaConfig()
                .getLong("StaminaModule.Indicator.Stay", 1200L);
        fadeOut = plugin.getConfigLoader().getStaminaConfig()
                .getLong("StaminaModule.Indicator.FadeOut", 0L);
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
            if (isRunning(player) && worlds.contains(player.getWorld().getName())) {

                // Decrease stamina
                double d = -decreaseAmount;
                data.giveStamina(d, PlayerResourceUpdateEvent.UpdateReason.SKILL_COST);

                if (debugMode) {
                    player.showTitle(Title.title(
                            MiniMessage.miniMessage().deserialize(String.valueOf(data.getStamina())),
                                    Component.text(d),
                                    Title.Times.times(
                                            Duration.ofMillis(0L),
                                            Duration.ofMillis(1200L),
                                            Duration.ofMillis(0L)
                                    )
                            )
                    );
                }

                // Check thresholds
                if (data.getStamina() <= lowThreshold && showLowTitle) {
                    player.showTitle(Title.title(
                            MiniMessage.miniMessage().deserialize(title),
                            MiniMessage.miniMessage().deserialize(subTitle),
                            Title.Times.times(
                                    Duration.ofMillis(fadeIn),
                                    Duration.ofMillis(stay),
                                    Duration.ofMillis(fadeOut)
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
            }
        }, 0, decreaseRate);

        sprintTasks.put(player, task);
    }

    public void shutdown() {
        for (BukkitTask task : sprintTasks.values()) {
            task.cancel();
        }
        sprintTasks.clear();
    }

    @EventHandler
    public void onResourceUpdate(PlayerResourceUpdateEvent event) {
        if (event.getResource() != PlayerResource.STAMINA) {
            return;
        }
        Player player = event.getPlayer();
        if (isRunning(player) && worlds.contains(player.getWorld().getName()) && event.getReason() == PlayerResourceUpdateEvent.UpdateReason.REGENERATION) {
            if (debugMode) player.sendMessage("Stamina Regeneration: Canceled | " + event.getAmount() + " | " + event.getReason());
            event.setCancelled(true);
            return;
        }
        if (debugMode) player.sendMessage("Stamina Regeneration: Not Canceled | " +  + event.getAmount() + " | " + event.getReason());
    }

    private boolean isRunning(Player player) {
        return (player.isSprinting() && !player.isSneaking() && !player.isFlying() && player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR);
    }
}

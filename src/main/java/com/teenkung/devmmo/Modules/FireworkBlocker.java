package com.teenkung.devmmo.Modules;

import com.teenkung.devmmo.DevMMO;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Blocks players from using fireworks while gliding, unless they have a bypass permission.
 * Uses FireworkBlocker.yml for configuration.
 */
public class FireworkBlocker implements Listener {

    private final DevMMO plugin;
    private final boolean blockWhenGliding;
    private final String bypassPerm;
    private final String blockedMsg;
    private final List<String> worlds;

    public FireworkBlocker(DevMMO plugin) {
        this.plugin = plugin;
        if (plugin.getConfigLoader().isModuleEnabled("FireworkBlocker")) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        blockWhenGliding = plugin.getConfigLoader().getFireworkBlockerConfig()
                .getBoolean("FireworkBlocker.BlockWhenGliding", true);

        bypassPerm = plugin.getConfigLoader().getFireworkBlockerConfig()
                .getString("FireworkBlocker.BypassPermission", "elytra.bypass");

        blockedMsg = plugin.getConfigLoader().getFireworkBlockerConfig()
                .getString("FireworkBlocker.BlockedMessage", "<red>Firework is not allowed!");

        worlds = new ArrayList<>(plugin.getConfigLoader().getFireworkBlockerConfig()
                .getStringList("FireworkBlocker.Worlds").stream().map(String::toLowerCase).toList());
    }

    @EventHandler
    public void onPlayerUseFirework(PlayerInteractEvent event) {
        if (!plugin.getConfigLoader().isModuleEnabled("FireworkBlocker")) {
            return;
        }

        if (!blockWhenGliding) return;

        Player player = event.getPlayer();
        if (!worlds.contains(player.getWorld().getName().toLowerCase())) {
            return;
        }
        if (player.isGliding()) {
            ItemStack item = event.getItem();
            if (item != null && item.getType() == Material.FIREWORK_ROCKET) {
                // If player lacks the bypass permission, block usage
                if (!player.hasPermission(bypassPerm)) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    player.sendActionBar(MiniMessage.miniMessage().deserialize(blockedMsg));
                }
            }
        }
    }
}

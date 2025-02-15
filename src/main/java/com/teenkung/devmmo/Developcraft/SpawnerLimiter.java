package com.teenkung.devmmo.Developcraft;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Arrays;

public class SpawnerLimiter implements Listener {

    @SuppressWarnings("FieldCanBeLocal")
    private final Developcraft module;
    private final int spawnerLimit;
    private final String message;

    public SpawnerLimiter(Developcraft module) {
        this.module = module;
        if (!module.getConfigLoader().isModuleEnabled("SpawnerLimiter")) {
            spawnerLimit = 0;
            message = "";
            return;
        }
        Bukkit.getPluginManager().registerEvents(this, module.getPlugin());
        spawnerLimit = module.getConfigLoader().getSpawnerLimiterConfig().getInt("SpawnerLimiter.Limit", 2);
        message = module.getConfigLoader().getSpawnerLimiterConfig().getString("SpawnerLimiter.Message", "<red>You cannot place more than 2 spawners in this chunk!");

        module.getPlugin().getLogger().info("[SpawnerLimiter] SpawnerLimiter has been enabled.");
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() != Material.SPAWNER) return;
        Location loc = event.getBlockPlaced().getLocation();
        Chunk chunk = loc.getChunk();
        if (Arrays.stream(chunk.getTileEntities()).filter(te -> te.getType() == Material.SPAWNER).count() > spawnerLimit) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize(message));
        }
    }



}

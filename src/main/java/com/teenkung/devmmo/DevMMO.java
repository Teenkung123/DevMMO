package com.teenkung.devmmo;

import com.teenkung.devmmo.Events.Events;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class DevMMO extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        Objects.requireNonNull(getCommand("spawn-mythicmobs")).setExecutor(new SpawnMythicMobs());
        Objects.requireNonNull(getCommand("spawn-mythicmobs")).setTabCompleter(new SpawnMythicMobsTab());
        Events event = new Events(this);
        Bukkit.getPluginManager().registerEvents(event, this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            event.addSprintTaskPlayer(player);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

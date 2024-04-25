package com.teenkung.devmmo;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpawnMythicMobs implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.isOp()) {
            if (sender instanceof Player player) {
                if (args.length >= 1) {
                    String mob = args[0];
                    int level = 1;
                    if (args.length >= 2) {
                        level = Integer.parseInt(args[1]);
                    }

                    MythicMob mmob = MythicBukkit.inst().getMobManager().getMythicMob(mob).orElse(null);
                    Location spawnLocation = player.getLocation();
                    if (mmob != null) {
                        // spawns mob
                        if (args.length == 3) {
                            for (int i = 0 ; i < Integer.parseInt(args[2]) ; i++) {
                                mmob.spawn(BukkitAdapter.adapt(spawnLocation), level);
                            }
                        } else {
                            mmob.spawn(BukkitAdapter.adapt(spawnLocation), level);
                        }
                    }
                }
            }
        }


        return false;
    }
}

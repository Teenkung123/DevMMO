package com.teenkung.devmmo.Commands.SpawnMythicMobs;

import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SpawnMythicMobsTab implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        ArrayList<String> result = new ArrayList<>();
        if (args.length == 1) {
            for (String mob : MythicBukkit.inst().getMobManager().getMobNames()) {
                if (mob.toLowerCase().contains(args[0].toLowerCase())) {
                    result.add(mob);
                }
            }
        } else if (args.length == 2) {
            result.add("<Mob Level>");
        }

        return result;
    }
}

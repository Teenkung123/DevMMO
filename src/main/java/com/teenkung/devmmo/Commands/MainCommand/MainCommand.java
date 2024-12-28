package com.teenkung.devmmo.Commands.MainCommand;

import com.teenkung.devmmo.DevMMO;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter {

    private final DevMMO plugin;

    public MainCommand(DevMMO plugin) {
        this.plugin = plugin;
        PluginCommand command = plugin.getCommand("devmmo");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("DevMMO plugin by TeenKung");
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("devmmo.reload")) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You do not have permission to use this command."));
                return true;
            }
            long start = System.currentTimeMillis();
            plugin.loadAll();
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>DevMMO reloaded in <gold>" + (System.currentTimeMillis() - start) + "ms<green>."));
            return true;
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

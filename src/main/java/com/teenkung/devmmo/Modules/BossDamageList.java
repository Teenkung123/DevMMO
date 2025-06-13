package com.teenkung.devmmo.Modules;

import com.teenkung.devmmo.DevMMO;
import com.teenkung.devmmo.Utils.ColorTranslator;
import io.lumine.mythic.bukkit.events.*;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BossDamageList implements Listener {

    private final DevMMO plugin;
    private final ArrayList<String> bossIds = new ArrayList<>();
    private final HashMap<UUID, HashMap<Player, Double>> damageMap = new HashMap<>();
    private final HashMap<String, HashMap<Integer, ArrayList<String>>> rewards = new HashMap<>();
    private final HashMap<String, List<String>> defaultRewards = new HashMap<>();
    private final HashMap<String, Double> damageThreshold = new HashMap<>();
    private String killMessage;
    private String leaderBoardMessage;

    @SuppressWarnings("DataFlowIssue")
    public BossDamageList(DevMMO plugin) {
        this.plugin = plugin;

        if (plugin.getConfigLoader().isModuleEnabled("BossDamageList")) {
            plugin.getLogger().info("[BossDamageList] Enabling module. . .");
            plugin.getServer().getPluginManager().registerEvents(this, plugin);

            killMessage = plugin.getConfigLoader().getBossDamageListConfig().getString("BossDamageList.KillMessage");
            leaderBoardMessage = plugin.getConfigLoader().getBossDamageListConfig().getString("BossDamageList.Leaderboard");
            Configuration config = plugin.getConfigLoader().getBossDamageListConfig();
            for (String bossId : plugin.getConfigLoader().getBossDamageListConfig().getConfigurationSection("BossDamageList.Bosses").getKeys(false)) {
                ConfigurationSection bossConfig = config.getConfigurationSection("BossDamageList.Bosses."+bossId);
                bossIds.add(bossId);
                HashMap<Integer, ArrayList<String>> rewardMap = new HashMap<>();
                for (String place : plugin.getConfigLoader().getBossDamageListConfig().getConfigurationSection("BossDamageList.Bosses."+bossId+".Rewards").getKeys(false)) {
                    ArrayList<String> rewards = new ArrayList<>(plugin.getConfigLoader().getBossDamageListConfig().getStringList("BossDamageList.Bosses."+bossId+".Rewards."+place));
                    rewardMap.put(Integer.parseInt(place), rewards);
                }
                rewards.put(bossId, rewardMap);
                defaultRewards.put(bossId, bossConfig.getStringList("DefaultRewards"));
                damageThreshold.put(bossId, bossConfig.getDouble("MinimumDamageThreshold"));
            }
            plugin.getLogger().info("[BossDamageList] Enabled module.");
        }
    }

    @EventHandler
    public void onSpawn(MythicMobSpawnEvent event) {
        HashMap<Player, Double> damageMap = new HashMap<>();
        if (!bossIds.contains(event.getMob().getType().getInternalName())) {
            return;
        }
        this.damageMap.put(event.getEntity().getUniqueId(), damageMap);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(PlayerAttackEvent event) {
        if (event.isCancelled()) return;
        if (event.getEntity().getNoDamageTicks() != 0) return;
        if (event.getEntity().isInvulnerable()) return;
        Player player = event.getAttacker().getPlayer();
        Entity target = event.getEntity();
        if (this.damageMap.containsKey(target.getUniqueId())) {
            HashMap<Player, Double> damageMap = this.damageMap.get(target.getUniqueId());
            if (damageMap.containsKey(player)) {
                double damage = damageMap.get(player);
                damage += event.getDamage().getDamage();
                damageMap.put(player, damage);
            } else {
                damageMap.put(player, event.getDamage().getDamage());
            }
        }
    }

    @EventHandler
    public void onDeath(MythicMobDeathEvent event) {
        if (this.damageMap.containsKey(event.getEntity().getUniqueId())) {
            HashMap<Player, Double> damageMap = this.damageMap.get(event.getEntity().getUniqueId());

            // Sort entries by descending damage
            List<Map.Entry<Player, Double>> sortedEntries = new ArrayList<>(damageMap.entrySet());
            sortedEntries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

            @NotNull
            Collection<Player> players = event.getEntity().getWorld().getNearbyPlayers(event.getEntity().getLocation(), 100);

            Component killmsg = MiniMessage.miniMessage().deserialize(killMessage, Placeholder.component("name", MiniMessage.miniMessage().deserialize(ColorTranslator.toMiniMessageFormat(event.getEntity().getName()))));
            List<Component> boards = new ArrayList<>();

            String bossId = event.getMob().getType().getInternalName();

            for (int i = 1 ; i <= sortedEntries.size(); i++) {
                Map.Entry<Player, Double> entry = sortedEntries.get(i-1);
                boards.add(MiniMessage.miniMessage().deserialize(
                        leaderBoardMessage,
                        Placeholder.unparsed("name", ColorTranslator.toMiniMessageFormat(entry.getKey().getName())),
                        Placeholder.unparsed("damage", String.format("%.2f", entry.getValue())),
                        Placeholder.unparsed("place", String.valueOf(i))
                ));

                if (damageThreshold.get(bossId) != null && entry.getValue() < damageThreshold.get(bossId)) {
                    continue;
                }

                if (rewards.get(event.getMob().getType().getInternalName()) != null) {
                    if (rewards.get(event.getMob().getType().getInternalName()).containsKey(i)) {
                        for (String reward : rewards.get(event.getMob().getType().getInternalName()).get(i)) {
                            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), reward.replace("<player>", entry.getKey().getName()));
                        }
                    }
                }

                for (String reward : defaultRewards.get(event.getMob().getType().getInternalName())) {
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), reward.replace("<player>", entry.getKey().getName()));
                }
            }

            for (Player player : players) {
                player.sendMessage(killmsg);
                for (Component board : boards) {
                    player.sendMessage(board);
                }
            }

            this.damageMap.remove(event.getEntity().getUniqueId());

        }
    }

    @EventHandler
    public void onMythicMobDespawn(MythicMobDespawnEvent event) {
        this.damageMap.remove(event.getEntity().getUniqueId());
    }

}

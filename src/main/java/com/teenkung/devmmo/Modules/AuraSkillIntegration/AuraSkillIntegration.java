package com.teenkung.devmmo.Modules.AuraSkillIntegration;

import com.teenkung.devmmo.DevMMO;
import com.teenkung.devmmo.Modules.AuraSkillIntegration.Traits.DamageReductionTrait;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.event.mana.ManaAbilityActivateEvent;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.registry.NamespacedRegistry;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public class AuraSkillIntegration implements Listener {

    private final DevMMO plugin;
    private final AuraSkillsApi auraSkills;
    private final String pluginKey = "DevMMO";

    private final Map<String, Integer> manaAbilityBaseCosts = new HashMap<>();
    private final Map<String, Integer> manaAbilityCostLevels = new HashMap<>();

    private final Logger logger = Logger.getLogger("AuraSkillIntegration");

    public AuraSkillIntegration(DevMMO plugin) {
        this.plugin = plugin;
        this.auraSkills = AuraSkillsApi.get();

        if (!plugin.getConfigLoader().isModuleEnabled("AuraSkillIntegration")) {
            plugin.getLogger().info("[AuraSkillIntegration] Module disabled. Not loading.");
            return;
        }

        ConfigurationSection config = plugin.getConfigLoader().getAuraSkillIntegrationConfig().getConfigurationSection("mana_abilities");
        if (config != null) {
            for (String key : config.getKeys(false)) {
                String baseCost = config.getString(key + ".base_cost");
                String costLevel = config.getString(key + ".cost_level");
                if (baseCost != null && costLevel != null) {
                    manaAbilityBaseCosts.put(key, Integer.parseInt(baseCost));
                    manaAbilityCostLevels.put(key, Integer.parseInt(costLevel));
                }
            }
            plugin.getLogger().info("[AuraSkillIntegration] Loaded " + manaAbilityBaseCosts.size() + " mana abilities.");
        }

        File statsFile = plugin.getDataFolder()
                .toPath()
                .resolve("Modules/AuraSkillIntegration/stats.yml")
                .toFile();

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(statsFile);
        CustomTraits.initStatsConfig(yaml);

        NamespacedRegistry registry = auraSkills.getNamespacedRegistry(CustomTraits.NAMESPACE);
        if (registry == null) {
            registry = auraSkills.useRegistry(CustomTraits.NAMESPACE, statsFile);
        }

        CustomTraits.registerEverything(auraSkills, registry);

        auraSkills.getHandlers().registerTraitHandler(new DamageReductionTrait());

        Bukkit.getPluginManager().registerEvents(this, plugin);
        final int batchSize    = 10;
        final int periodTicks  = 5 * 20;

        new BukkitRunnable() {
            @Override
            public void run() {
                List<Player> players = new ArrayList<>(plugin.getServer().getOnlinePlayers());
                if (players.isEmpty()) return;

                int totalPlayers = players.size();
                int numBatches   = (totalPlayers + batchSize - 1) / batchSize;
                // How many ticks between the start of each batch
                int ticksBetween = periodTicks / numBatches;

                for (int batchIndex = 0; batchIndex < numBatches; batchIndex++) {
                    int start = batchIndex * batchSize;
                    int end   = Math.min(start + batchSize, totalPlayers);
                    long delay = (long) batchIndex * ticksBetween;

                    // Schedule this batch to run after `delay` ticks
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        for (int i = start; i < end; i++) {
                            updatePlayerStats(players.get(i));
                        }
                    }, delay);
                }
            }
        }.runTaskTimer(plugin, 0L, periodTicks);
    }


    public AuraSkillsApi getAuraSkills() {
        return auraSkills;
    }

    public void shutdown() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            unregisterStatModifiers(player);
        }
    }

    public void updatePlayerStats(Player player) {
        unregisterStatModifiers(player);
        registerStatModifiers(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updatePlayerStats(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        unregisterStatModifiers(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAuraSkillManaUsed(ManaAbilityActivateEvent event) {
        Player player = event.getPlayer();
        PlayerData data = PlayerData.get(player);
        player.sendMessage("Abilityy Id Key: " + event.getManaAbility().getId().getKey());
        for (String key : manaAbilityBaseCosts.keySet()) {
            if (event.getManaAbility().getId().getKey().equals(key)) {
                int baseCost = manaAbilityBaseCosts.get(key);
                int costLevel = manaAbilityCostLevels.get(key);
                int abilityLevel = auraSkills.getUser(player.getUniqueId()).getManaAbilityLevel(event.getManaAbility());
                int totalCost = baseCost + (costLevel * abilityLevel);
                if (totalCost > data.getMana()) {
                    player.sendMessage("Not enough mana! " + totalCost + " > " + data.getMana());
                    event.setCancelled(true);
                    return;
                }
                data.setMana(data.getMana() - totalCost);
                player.sendMessage("Mana Cost: " + totalCost);
                break;
            }
        }
    }

    private void unregisterStatModifiers(Player player) {
        MMOPlayerData data = MMOPlayerData.get(player);
        StatMap statMap   = data.getStatMap();

        // Remove every modifier originating from this module in one pass
        statMap.getInstances().forEach(statInstance ->
                statInstance.getModifiers().stream()
                        .filter(m -> m.getKey().equals(pluginKey))
                        .forEach(m -> m.unregister(data)));
    }

    private void registerStatModifiers(Player player) {
        MMOPlayerData data = MMOPlayerData.get(player);

        for (DynamicStat dyn : DynamicStat.values()) {
            double value = dyn.value(player);
            StatModifier mod = new StatModifier(pluginKey, dyn.key(), value);
            mod.register(data);
        }
    }

}

package com.teenkung.devmmo.Modules;

import com.teenkung.devmmo.DevMMO;
import com.teenkung.devmmo.Utils.PlayerDamage;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.EXPSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * Awards experience to players based on their share of damage done to a MythicMob.
 * Leverages DamageTracker to see how much damage each player did.
 * The formula is read from MobXPModule.yml.
 */
public class MobXPModule implements Listener {

    private final DevMMO plugin;
    private final DamageTracker damageTracker;
    private final long fadeIn;
    private final long stay;
    private final long fadeOut;
    private final String titleMessage;
    private final String soundName;
    private final float volume;
    private final float pitch;
    private final String baseFormula;
    private final String additionalXpStatName;

    public MobXPModule(DevMMO plugin, DamageTracker damageTracker) {
        this.plugin = plugin;
        this.damageTracker = damageTracker;
        if (plugin.getConfigLoader().isModuleEnabled("MobXPModule")) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        fadeIn = plugin.getConfigLoader().getMobXPConfig().getLong("MobXPModule.TitleFadeIn", 3);
        stay = plugin.getConfigLoader().getMobXPConfig().getLong("MobXPModule.TitleStay", 10);
        fadeOut = plugin.getConfigLoader().getMobXPConfig().getLong("MobXPModule.TitleFadeOut", 3);
        titleMessage = plugin.getConfigLoader().getMobXPConfig().getString("MobXPModule.TitleMessage", "<yellow>+{exp}<green> EXP!");
        soundName = plugin.getConfigLoader().getMobXPConfig().getString("MobXPModule.AwardSound", "BLOCK_NOTE_BLOCK_BELL");
        volume = (float) plugin.getConfigLoader().getMobXPConfig().getDouble("MobXPModule.SoundVolume", 1.0);
        pitch = (float) plugin.getConfigLoader().getMobXPConfig().getDouble("MobXPModule.SoundPitch", 1.2);
        baseFormula = plugin.getConfigLoader().getMobXPConfig().getString("MobXPModule.BaseFormula", "10 * level");
        additionalXpStatName = plugin.getConfigLoader().getMobXPConfig().getString("MobXPModule.AdditionalXpStat", "mob_xp_bonus");
    }

    @EventHandler
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        if (!plugin.getConfigLoader().isModuleEnabled("MobXPModule")) return;

        // Retrieve damage info from DamageTracker
        ActiveMob mob = event.getMob();
        UUID mobUUID = mob.getUniqueId();
        PlayerDamage mobDamage = damageTracker.getDamages().get(mobUUID);
        if (mobDamage == null) return;

        // Evaluate the mob's level
        int mobLevel = (int) mob.getLevel();
        double totalDamage = mobDamage.getTotalDamage();

        // For each player who dealt damage, calculate XP
        for (Map.Entry<UUID, Double> e : mobDamage.getMap().entrySet()) {
            Player player = Bukkit.getPlayer(e.getKey());
            if (player == null) continue;

            // Player's share
            double share = (totalDamage > 0) ? (e.getValue() / totalDamage) : 0;
            // Evaluate base formula (we'll do a naive approach, or parse it via a small expression library)
            // For the sake of example, let's do a simple "replace placeholders" approach:
            double baseXP = evaluateFormula(baseFormula, mobLevel);

            double xpAmount = baseXP * share;

            // Additional XP stat
            PlayerData data = PlayerData.get(player);
            double additionalXp = data.getStats().getStat(additionalXpStatName);
            xpAmount += xpAmount * (additionalXp / 100.0);

            // Format XP
            DecimalFormat df = new DecimalFormat("#.##");
            String xpString = df.format(xpAmount);

            // Show Title
            player.showTitle(Title.title(Component.empty(), MiniMessage.miniMessage().deserialize(titleMessage.replace("{exp}", xpString)), Title.Times.times(Duration.ofMillis(fadeIn), Duration.ofMillis(stay), Duration.ofMillis(fadeOut))));

            // Play Sound
            player.playSound(player.getLocation(), Sound.valueOf(soundName), volume, pitch);

            // Actually give XP
            data.giveExperience(xpAmount, EXPSource.VANILLA);
        }

        // Cleanup
        damageTracker.getDamages().remove(mobUUID);
    }



    private double evaluateFormula(String formula, int level) {
        try {
            // Replace placeholders with variable names compatible with exp4j
            formula = formula.replace("{level}", "level");

            // Build the expression
            Expression expression = new ExpressionBuilder(formula)
                    .variables("level")
                    .build()
                    .setVariable("level", level);

            // Evaluate the expression
            return expression.evaluate();
        } catch (Exception ex) {
            // Handle exceptions appropriately
            plugin.getLogger().severe("Error evaluating formula: " + formula);
            return 0;
        }
    }

}

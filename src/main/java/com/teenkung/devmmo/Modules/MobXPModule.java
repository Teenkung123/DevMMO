package com.teenkung.devmmo.Modules;

import com.teenkung.devmmo.DevMMO;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.Indyuce.mmocore.api.event.PlayerExperienceGainEvent;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Awards experience to a player when they kill a MythicMob.
 * Uses MobXPModule.yml for configuration.
 */
public class MobXPModule implements Listener {

    private final DevMMO plugin;
    private final DecimalFormat df = new DecimalFormat("#.##");
    private final Map<Integer, Double> cahceEXP = new HashMap<>();

    private long fadeIn;
    private long stay;
    private long fadeOut;
    private String titleMessage;
    private String soundName;
    private float volume;
    private float pitch;
    private String baseFormula;
    private boolean debugMode;

    private final Map<Player, Double> experienceBuffer = new ConcurrentHashMap<>();

    public MobXPModule(DevMMO plugin) {
        this.plugin = plugin;
        fadeIn = plugin.getConfigLoader().getMobXPConfig().getLong("MobXPModule.TitleFadeIn", 3);
        stay = plugin.getConfigLoader().getMobXPConfig().getLong("MobXPModule.TitleStay", 10);
        fadeOut = plugin.getConfigLoader().getMobXPConfig().getLong("MobXPModule.TitleFadeOut", 3);
        titleMessage = plugin.getConfigLoader().getMobXPConfig().getString("MobXPModule.TitleMessage", "<yellow>+{exp}<green> EXP!");
        soundName = plugin.getConfigLoader().getMobXPConfig().getString("MobXPModule.AwardSound", "BLOCK_NOTE_BLOCK_BELL");
        volume = (float) plugin.getConfigLoader().getMobXPConfig().getDouble("MobXPModule.SoundVolume", 1.0);
        pitch = (float) plugin.getConfigLoader().getMobXPConfig().getDouble("MobXPModule.SoundPitch", 1.2);
        baseFormula = plugin.getConfigLoader().getMobXPConfig().getString("MobXPModule.BaseFormula", "10 * level");

        if (plugin.getConfigLoader().isModuleEnabled("MobXPModule")) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
        if (plugin.getConfigLoader().isModuleEnabled("EXPShareModule")) {
            plugin.getLogger().info("[MobXPModule] EXPShareModule is enabled. This will disable default EXP rewards of this module to prevent double rewards.");
        }

    }

    /**
     * Awards experience to a player when they kill a MythicMob.
     * @param event The MythicMobDeathEvent fired by MythicMobs.
     */
    @EventHandler
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        if (!plugin.getConfigLoader().isModuleEnabled("MobXPModule")) return;
        if (plugin.getConfigLoader().isModuleEnabled("EXPShareModule")) return;

        // Retrieve damage info from DamageTracker
        ActiveMob mob = event.getMob();

        // Evaluate the mob's level
        int mobLevel = (int) mob.getLevel();

        if (event.getKiller() instanceof Player player) {
            rewardExperience(player, calculateExpression(baseFormula, mobLevel));
        }
    }

    /**
     * Buffers experience gained by a player.
     * to be displayed in a title message.
     * @param event The PlayerExperienceGainEvent fired by MMOCore.
     */
    @EventHandler
    public void onExperienceGain(PlayerExperienceGainEvent event) {
        if (event.getProfession() != null) return;
        Player player = event.getPlayer();
        double exp = event.getExperience();
        experienceBuffer.merge(player, exp, Double::sum);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            double total = experienceBuffer.getOrDefault(player, 0.0);
            if (total <= 0) return;
            String str = df.format(total);
            player.showTitle(Title.title(Component.empty(), MiniMessage.miniMessage().deserialize(titleMessage.replace("{exp}", str)), Title.Times.times(Duration.ofMillis(fadeIn), Duration.ofMillis(stay), Duration.ofMillis(fadeOut))));
            player.playSound(player.getLocation(), Sound.valueOf(soundName), volume, pitch);
            experienceBuffer.remove(player);
        }, 3L);
    }

    /**
     * Rewards a player with experience.
     * @param player The player to reward.
     * @param xp The amount of experience to reward.
     */
    public void rewardExperience(Player player, double xp) {
        PlayerData data = PlayerData.get(player);
        data.giveExperience(xp, EXPSource.VANILLA);
    }

    public double getExperienceGain(int level) {
        return calculateExpression(baseFormula, level);
    }

    /**
     * Calculates the amount of experience a player should receive for killing a MythicMob.
     * @param formula The formula to use for calculating experience.
     * @param level The level of the mob.
     * @return The amount of experience to award.
     */
    private double calculateExpression(String formula, int level) {
        if (cahceEXP.containsKey(level)) {
            return cahceEXP.get(level);
        }
        try {
            formula = formula.replace("{level}", "level");

            Expression expression = new ExpressionBuilder(formula)
                    .variables("level")
                    .build()
                    .setVariable("level", level);
            double exp = expression.evaluate();
            cahceEXP.put(level, exp);
            return exp;
        } catch (Exception ex) {
            plugin.getLogger().severe("Error evaluating formula: " + formula);
            return 0;
        }
    }

    /**
     * Returns the debug mode status.
     * @return The debug mode status.
     */
    public boolean isDebugMode() {
        return debugMode;
    }

    /**
     * Sets the debug mode status.
     * @param debugMode The debug mode status.
     */
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    /**
     * Sets the base formula for calculating experience.
     * @param baseFormula The base formula for calculating experience.
     */
    public void setBaseFormula(String baseFormula) {
        this.baseFormula = baseFormula;
        this.cahceEXP.clear();
    }

    /**
     * Sets the title message for the experience reward.
     * @param titleMessage The title message for the experience reward.
     */
    public void setTitleMessage(String titleMessage) {
        this.titleMessage = titleMessage;
    }

    /**
     * Sets the sound name for the experience reward.
     * @param soundName The sound name for the experience reward.
     */
    public void setSoundName(String soundName) {
        this.soundName = soundName;
    }

    /**
     * Sets the volume for the experience reward.
     * @param volume The volume for the experience reward.
     */
    public void setVolume(float volume) {
        this.volume = volume;
    }

    /**
     * Sets the pitch for the experience reward.
     * @param pitch The pitch for the experience reward.
     */
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    /**
     * Sets the fade in time for the experience reward.
     * @param fadeIn The fade in time for the experience reward.
     */
    public void setFadeIn(long fadeIn) {
        this.fadeIn = fadeIn;
    }

    /**
     * Sets the stay time for the experience reward.
     * @param stay The stay time for the experience reward.
     */
    public void setStay(long stay) {
        this.stay = stay;
    }

    /**
     * Sets the fade out time for the experience reward.
     * @param fadeOut The fade out time for the experience reward.
     */
    public void setFadeOut(long fadeOut) {
        this.fadeOut = fadeOut;
    }



}

package com.teenkung.devmmo.Events;

import com.teenkung.devmmo.DevMMO;
import com.teenkung.devmmo.PlayerDamage;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.experience.EXPSource;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Events implements Listener {

    private final Map<Player, BukkitTask> sprintTasks = new HashMap<>();
    private final DevMMO plugin;
    private final HashMap<UUID, PlayerDamage> damages = new HashMap<>();
    private final HashMap<UUID, Double> health = new HashMap<>();

    public Events(DevMMO plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (MythicBukkit.inst().getMobManager().isMythicMob(event.getEntity())) {
                PlayerDamage damage = damages.getOrDefault(event.getEntity().getUniqueId(), new PlayerDamage());
                double oldDmg = damage.getDamage(player.getUniqueId());
                damage.setDamage(player.getUniqueId(), oldDmg + event.getDamage());
                damages.put(event.getEntity().getUniqueId(), damage);
            }
        }
    }

    @EventHandler
    public void onDespawn(MythicMobDespawnEvent event) {
        damages.remove(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onMythicMobsDead(MythicMobDeathEvent event) {
        ActiveMob mob = event.getMob();
        UUID mobUUID = mob.getUniqueId();
        PlayerDamage mobDamage = damages.get(mobUUID);

        if (mobDamage != null) {
            int level = Double.valueOf(mob.getLevel()).intValue();
            double hp = mob.getEntity().getMaxHealth();
            double baseHP = (hp - (0.15 * (Math.pow(level, 2)))) / (1 + (0.5 * level));

            // Calculate the base EXP
            double baseExp = (baseHP / 2) + (0.2 * (Math.pow(level, 2)));

            // Apply the staircase effect to the EXP gain
            double addExp = baseExp;
            for (int i = 5; i <= level; i += 5) {
                addExp += baseExp * 0.5;
            }

            // Total damage dealt to the mob
            double totalDamage = mobDamage.getTotalDamage();

            // Loop through each player who dealt damage
            for (Map.Entry<UUID, Double> entry : mobDamage.getMap().entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null) {
                    PlayerData data = PlayerData.get(player);

                    // Calculate the player's share of the total damage
                    double damageShare = entry.getValue() / totalDamage;

                    // Adjust EXP based on damage share
                    double playerExp = addExp * damageShare;

                    // Additional experience modifier
                    double additional = data.getStats().getStat("ADDITIONAL_EXPERIENCE");
                    playerExp += playerExp * (additional / 100);

                    // Format the EXP value
                    DecimalFormat decfor = new DecimalFormat("#.##");
                    String formattedPlayerExp = decfor.format(playerExp);

                    // Give the EXP to the player
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1F, 1.2F);
                    player.sendTitle("", ChatColor.YELLOW + "+" + formattedPlayerExp + ChatColor.GREEN + " EXP!", 3, 10, 3);
                    data.giveExperience(playerExp, EXPSource.VANILLA);
                }
            }

            // Remove the mob's damage record after processing
            damages.remove(mobUUID);
        }
    }




    @SuppressWarnings("DataFlowIssue")
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (health.containsKey(player.getUniqueId()) && player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            double hp = health.get(player.getUniqueId());
            //noinspection DataFlowIssue
            player.setHealth(Math.min(hp, player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
            health.remove(player.getUniqueId());
        }
        addSprintTaskPlayer(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        health.put(event.getPlayer().getUniqueId(), event.getPlayer().getHealth());
        sprintTasks.get(event.getPlayer()).cancel();
    }

    public void addSprintTaskPlayer(Player player) {
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            PlayerData data = PlayerData.get(player);
            if (player.isSprinting() && !player.isSneaking() && !player.isFlying() && !(player.getGameMode() == GameMode.CREATIVE) && !(player.getGameMode() == GameMode.SPECTATOR)) {
                if (data.getStamina() <= 5) {
                    player.sendTitle("", ChatColor.RED+"Low Stamina!", 0, 100, 2);
                }
                if (data.getStamina() <= 1) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2, true, false, false));
                    player.setSprinting(false);
                }
                data.giveStamina(-1, PlayerResourceUpdateEvent.UpdateReason.SKILL_COST);
            } else {
                data.giveStamina(1, PlayerResourceUpdateEvent.UpdateReason.REGENERATION);
            }
        }, 20, 20);
        sprintTasks.put(player, task);
    }

    @EventHandler
    public void onPlayerUseFirework(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Check if the player is flying with an elytra
        if (player.isGliding()) {
            ItemStack item = event.getItem();

            // Check if the item is a firework rocket
            if (item != null && item.getType() == Material.FIREWORK_ROCKET) {
                // Check if the player lacks the 'elytra.bypass' permission
                if (!player.hasPermission("elytra.bypass")) {
                    // Cancel the event to block the elytra boost
                    event.setCancelled(true);
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(new ComponentBuilder("Firework is not allowed in MMO world!").color(ChatColor.RED).create()));
                }
            }
        }
    }
}

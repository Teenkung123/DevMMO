package com.teenkung.devmmo.Utils;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class PlayerDamage {

    private final HashMap<UUID, Double> damages = new HashMap<>();

    public PlayerDamage() {

    }

    /**
     * Set the damage dealt by a player.
     * @param uuid The UUID of the player.
     * @param damage The amount of damage dealt.
     */
    public void setDamage(UUID uuid, Double damage) {
        this.damages.put(uuid, damage);
    }

    /**
     * Get the damage dealt by a player.
     * @param uuid The UUID of the player.
     * @return The amount of damage dealt.
     */
    public double getDamage(UUID uuid) {
        return damages.getOrDefault(uuid, 0.0);
    }

    /**
     * Get the total damage dealt by all players.
     * @return The total damage dealt.
     */
    public double getTotalDamage() {
        return damages.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    /**
     * Get the set of UUIDs of players who dealt damage.
     * @return The set of UUIDs.
     */
    public Set<UUID> getDamagers() {
        return damages.keySet();
    }

    /**
     * Get the map of UUIDs to damage dealt.
     * @return The map of UUIDs to damage dealt.
     */
    public HashMap<UUID, Double> getMap() {
        return damages;
    }

}

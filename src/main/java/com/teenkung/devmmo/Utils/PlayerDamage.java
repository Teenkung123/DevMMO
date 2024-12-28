package com.teenkung.devmmo.Utils;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class PlayerDamage {

    private HashMap<UUID, Double> damages = new HashMap<>();

    public PlayerDamage() {

    }

    public void setDamage(UUID uuid, Double damage) {
        this.damages.put(uuid, damage);
    }

    public double getDamage(UUID uuid) {
        return damages.getOrDefault(uuid, 0.0);
    }

    public double getTotalDamage() {
        return damages.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    public Set<UUID> getDamagers() {
        return damages.keySet();
    }

    public HashMap<UUID, Double> getMap() {
        return damages;
    }

}

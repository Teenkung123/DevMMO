package com.teenkung.devmmo.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class FallDamageEvent implements Listener {

    @EventHandler
    public void onFall(EntityDamageEvent event) {
        if (event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            if (event.getEntity() instanceof Player) {
                event.setDamage(event.getDamage()*5);
            }
        }
    }

}

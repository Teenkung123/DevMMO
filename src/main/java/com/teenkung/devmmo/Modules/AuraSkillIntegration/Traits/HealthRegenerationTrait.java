package com.teenkung.devmmo.Modules.AuraSkillIntegration.Traits;

import com.teenkung.devmmo.Modules.AuraSkillIntegration.CustomTraits;
import dev.aurelium.auraskills.api.bukkit.BukkitTraitHandler;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.user.SkillsUser;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Locale;

public class HealthRegenerationTrait implements BukkitTraitHandler, Listener {
    public double getBaseLevel(Player player, Trait trait) { return 0;  }

    @Override
    public void onReload(Player player, SkillsUser user, Trait trait) { }

    @Override
    public Trait[] getTraits() {
        return new Trait[] { CustomTraits.HEALTH_REGENERATION.trait() };
    }

    @Override
    public String getMenuDisplay(double value, Trait trait, Locale locale) {
        return value + "%";
    }
}

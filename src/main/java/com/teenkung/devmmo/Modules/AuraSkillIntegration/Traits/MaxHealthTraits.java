package com.teenkung.devmmo.Modules.AuraSkillIntegration.Traits;

import com.teenkung.devmmo.Modules.AuraSkillIntegration.CustomTraits;
import dev.aurelium.auraskills.api.bukkit.BukkitTraitHandler;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.user.SkillsUser;
import dev.aurelium.auraskills.api.util.NumberUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Locale;

public class MaxHealthTraits implements BukkitTraitHandler, Listener {

    @Override
    public double getBaseLevel(Player player, Trait trait) {
        return 0;   // your logic
    }

    @Override
    public void onReload(Player player, SkillsUser user, Trait trait) { }

    @Override
    public Trait[] getTraits() {
        // no API parameter needed – enum does the caching internally
        return new Trait[] { CustomTraits.MAX_HEALTH.trait() };
    }


}

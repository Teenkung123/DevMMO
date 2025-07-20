package com.teenkung.devmmo.Modules.AuraSkillIntegration.Traits;

import com.teenkung.devmmo.Modules.AuraSkillIntegration.CustomTraits;
import dev.aurelium.auraskills.api.bukkit.BukkitTraitHandler;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.user.SkillsUser;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Locale;

public class DamageReductionTrait implements BukkitTraitHandler, Listener {

    @Override
    public double getBaseLevel(Player player, Trait trait) {
        return 0;   // your logic
    }

    @Override
    public void onReload(Player player, SkillsUser user, Trait trait) { }

    @Override
    public Trait[] getTraits() {
        // no API parameter needed – enum does the caching internally
        return new Trait[] { CustomTraits.DAMAGE_REDUCTION.trait() };
    }
    //formula = (-1 * 1.00231^(-1 * 125) + 1)*100
    @Override
    public String getMenuDisplay(double value, Trait trait, Locale locale) {
        return reductionPercent(value) + "%";
    }

    /** Visible everywhere in the plugin – returns 0 – 100 (%) */
    public static double reductionPercent(double x) {
        double pct = (1 - Math.pow(1.00231, -x)) * 100;
        return Math.round(Math.max(0, Math.min(100, pct)) * 100.0) / 100.0;
    }


}

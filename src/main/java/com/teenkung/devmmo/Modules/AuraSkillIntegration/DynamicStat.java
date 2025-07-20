
package com.teenkung.devmmo.Modules.AuraSkillIntegration;

import com.teenkung.devmmo.Modules.AuraSkillIntegration.Traits.DamageReductionTrait;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.trait.Traits;
import org.bukkit.entity.Player;

import java.util.function.Function;

/** Add a new constant here → that's literally all you change. */
public enum DynamicStat {
    MAX_MANA   ("MAX_MANA",        p -> AuraSkillsApi.get().getUser(p.getUniqueId()).getMaxMana()),
    MANA_REGENERATION ("MANA_REGENERATION", p -> AuraSkillsApi.get().getUser(p.getUniqueId()).getEffectiveTraitLevel(Traits.MANA_REGEN)),
    CRITICAL_STRIKE_POWER ("CRITICAL_STRIKE_POWER", p -> AuraSkillsApi.get().getUser(p.getUniqueId()).getEffectiveTraitLevel(Traits.CRIT_DAMAGE)),
    CRITICAL_STRIKE_CHANCE ("CRITICAL_STRIKE_CHANCE", p -> AuraSkillsApi.get().getUser(p.getUniqueId()).getEffectiveTraitLevel(Traits.CRIT_CHANCE)),
    DAMAGE_REDUCTION("DAMAGE_REDUCTION", p -> {
        double traitLvl = AuraSkillsApi.get()
                .getUser(p.getUniqueId())
                .getEffectiveTraitLevel(CustomTraits.DAMAGE_REDUCTION.trait());
        return DamageReductionTrait.reductionPercent(traitLvl);   // convert to %
    }),
    MAX_HEALTH("MAX_HEALTH", p -> AuraSkillsApi.get().getUser(p.getUniqueId()).getEffectiveTraitLevel(CustomTraits.MAX_HEALTH.trait())),
    WEAPON_DAMAGE("WEAPON_DAMAGE", p -> AuraSkillsApi.get().getUser(p.getUniqueId()).getEffectiveTraitLevel(CustomTraits.WEAPON_DAMAGE.trait())),
    DODGE_RATING("DODGE_RATING", p -> AuraSkillsApi.get().getUser(p.getUniqueId()).getEffectiveTraitLevel(CustomTraits.DODGE_RATING.trait())),
    HEALTH_REGENERATION("HEALTH_REGENERATION", p -> AuraSkillsApi.get().getUser(p.getUniqueId()).getEffectiveTraitLevel(CustomTraits.HEALTH_REGENERATION.trait())),
    ADDITIONAL_EXPERIENCE("ADDITIONAL_EXPERIENCE", p -> AuraSkillsApi.get().getUser(p.getUniqueId()).getEffectiveTraitLevel(Traits.EXPERIENCE_BONUS)),
    COOLDOWN_REDUCTION("COOLDOWN_REDUCTION", p -> AuraSkillsApi.get().getUser(p.getUniqueId()).getEffectiveTraitLevel(CustomTraits.COOLDOWN_REDUCTION.trait())),
    ;

    private final String statKey;
    private final Function<Player, Double> valueSupplier;

    DynamicStat(String statKey, Function<Player, Double> valueSupplier) {
        this.statKey = statKey;
        this.valueSupplier = valueSupplier;
    }

    public String key()                  { return statKey; }
    public double value(Player player)   { return valueSupplier.apply(player); }
}

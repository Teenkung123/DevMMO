package com.teenkung.devmmo.Modules.AuraSkillIntegration;

import com.teenkung.devmmo.Modules.AuraSkillIntegration.Traits.*;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.bukkit.BukkitTraitHandler;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.registry.NamespacedRegistry;
import dev.aurelium.auraskills.api.trait.CustomTrait;
import dev.aurelium.auraskills.api.trait.Trait;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public enum CustomTraits {

    DAMAGE_REDUCTION(
            "mmo_damage_reduction",
            "Damage Reduction (Default)",
            DamageReductionTrait::new
    ),
    WEAPON_DAMAGE(
            "mmo_weapon_damage",
            "Weapon Damage (Default)",
            WeaponDamageTrait::new
    ),
    MAX_HEALTH(
            "mmo_max_health",
            "Max Health (Default)",
            MaxHealthTraits::new
    ),
    DODGE_RATING(
            "mmo_dodge_rating",
            "Dodge Rating (Default)",
            DodgeRatingTrait::new
    ),
    HEALTH_REGENERATION(
            "mmo_health_regeneration",
            "Health Regeneration (Default)",
            HealthRegenerationTrait::new
    ),
    COOLDOWN_REDUCTION(
            "mmo_cooldown_reduction",
            "Cooldown Reduction (Default)",
            CooldownReductionTrait::new
    ),
    ;

    /* ------------------------------------------------------------------ */
    /* implementation details                                             */
    public static final String NAMESPACE = "devmmo";

    private static YamlConfiguration statsConfig;

    private final String          key;
    private final String          defaultDisplay;
    private final NamespacedId    id;
    private final Supplier<? extends BukkitTraitHandler> handlerSupplier;

    private CustomTrait           cached;

    CustomTraits(String key,
                 String defaultDisplay,
                 Supplier<? extends BukkitTraitHandler> handlerSupplier) {
        this.key             = key;
        this.defaultDisplay  = defaultDisplay;
        this.id              = NamespacedId.of(NAMESPACE, key);
        this.handlerSupplier = handlerSupplier;
    }

    /* ---------- public helpers --------------------------------------- */

    public static void initStatsConfig(YamlConfiguration yaml) {
        statsConfig = yaml;
    }

    public @NotNull NamespacedId id() {
        return id;
    }

    /** Lazily build or reuse the CustomTrait instance. */
    public @NotNull CustomTrait trait() {
        AuraSkillsApi api = AuraSkillsApi.get();
        if (cached != null) return cached;

        Trait existing = api.getGlobalRegistry().getTrait(id);
        if (existing instanceof CustomTrait ct) {      // reuse
            cached = ct;
        } else {                                      // build
            String display = resolveDisplayName();
            cached = CustomTrait.builder(id)
                    .displayName(display)
                    .build();
        }
        return cached;
    }

    /** Build a fresh handler (may return null if no handler needed). */
    public BukkitTraitHandler newHandler() {
        return handlerSupplier == null ? null : handlerSupplier.get();
    }

    /* ---------- internal --------------------------------------------- */
    private String resolveDisplayName() {
        if (statsConfig == null) return defaultDisplay;

        String path = NAMESPACE + "/" + key + ".display";
        return statsConfig.getString(path, defaultDisplay);
    }

    /* ---------- bulk utilities --------------------------------------- */

    /** Register every trait & handler – call once from integration. */
    public static void registerEverything(AuraSkillsApi api,
                                          NamespacedRegistry registry) {

        for (CustomTraits ct : values()) {

            // 1. register trait if new
            if (api.getGlobalRegistry().getTrait(ct.id()) == null) {
                registry.registerTrait(ct.trait());
            }

            // 2. register handler if present
            BukkitTraitHandler handler = ct.newHandler();
            if (handler != null) {
                api.getHandlers().registerTraitHandler(handler);
            }
        }
    }
}

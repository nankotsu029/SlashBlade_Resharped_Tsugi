package mods.flammpfeil.slashblade.registry;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import mods.flammpfeil.slashblade.registry.specialeffects.WitherEdge;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.neoforged.neoforge.registries.DeferredHolder;

public class SpecialEffectsRegistry {
    public static final DeferredRegister<SpecialEffect> SPECIAL_EFFECT = DeferredRegister.create(SpecialEffect.REGISTRY_KEY,
            SlashBlade.MODID);

    public static final Registry<SpecialEffect> REGISTRY = SPECIAL_EFFECT.makeRegistry(builder -> {});
    public static final DeferredHolder<SpecialEffect, SpecialEffect> WITHER_EDGE = SPECIAL_EFFECT.register("wither_edge",
            WitherEdge::new);
}

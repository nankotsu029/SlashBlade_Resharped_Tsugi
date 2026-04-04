package mods.flammpfeil.slashblade.init;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.SlashBladeState;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * NeoForge 1.21.1 DataComponent registration for ItemStack data.
 * Replaces the old Forge Capability system for item-bound state.
 */
public class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, SlashBlade.MODID);

    /**
     * BLADE_STATE replaces the old CapabilitySlashBlade.BLADESTATE / ItemSlashBlade.BLADESTATE.
     * Access via: stack.get(ModDataComponents.BLADE_STATE.get())  (nullable)
     * Set via:    stack.set(ModDataComponents.BLADE_STATE.get(), state)
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SlashBladeState>> BLADE_STATE =
            DATA_COMPONENTS.register("blade_state", () ->
                    DataComponentType.<SlashBladeState>builder()
                            .persistent(SlashBladeState.CODEC)
                            .networkSynchronized(SlashBladeState.STREAM_CODEC)
                            .build()
            );

    public static void register(IEventBus modBus) {
        DATA_COMPONENTS.register(modBus);
    }
}
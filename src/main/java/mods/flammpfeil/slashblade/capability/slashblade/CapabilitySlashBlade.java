package mods.flammpfeil.slashblade.capability.slashblade;

import mods.flammpfeil.slashblade.init.ModDataComponents;
import net.minecraft.core.component.DataComponentType;

/**
 * NeoForge 1.21.1: BLADESTATE is now a DataComponentType, not a Forge Capability.
 *
 * Migration guide:
 *   Old: stack.getCapability(CapabilitySlashBlade.BLADESTATE).ifPresent(state -> { ... })
 *   New: var state = stack.get(CapabilitySlashBlade.BLADESTATE); if (state != null) { ... }
 *
 *   Old: stack.getCapability(CapabilitySlashBlade.BLADESTATE).map(...).orElse(def)
 *   New: Optional.ofNullable(stack.get(CapabilitySlashBlade.BLADESTATE)).map(...).orElse(def)
 */
public class CapabilitySlashBlade {

    /**
     * Use stack.get(BLADESTATE) to read (returns null if not present).
     * Use stack.set(BLADESTATE, state) to write.
     */
    public static DataComponentType<SlashBladeState> BLADESTATE =
            ModDataComponents.BLADE_STATE.get();
}

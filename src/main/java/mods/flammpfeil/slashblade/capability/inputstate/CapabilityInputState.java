package mods.flammpfeil.slashblade.capability.inputstate;

import mods.flammpfeil.slashblade.init.ModAttachments;
import net.neoforged.neoforge.attachment.AttachmentType;

/**
 * NeoForge 1.21.1: INPUT_STATE is now an AttachmentType, not a Forge Capability.
 *
 * Migration guide:
 *   Old: entity.getCapability(CapabilityInputState.INPUT_STATE).ifPresent(s -> { ... })
 *   New: entity.getData(CapabilityInputState.INPUT_STATE).doStuff()  // never null
 */
public class CapabilityInputState {

    /**
     * Use entity.getData(INPUT_STATE) to read — always returns a live instance.
     */
    public static AttachmentType<InputState> INPUT_STATE =
            ModAttachments.INPUT_STATE.get();
}

package mods.flammpfeil.slashblade.capability.mobeffect;

import mods.flammpfeil.slashblade.init.ModAttachments;
import net.neoforged.neoforge.attachment.AttachmentType;

/**
 * NeoForge 1.21.1: MOB_EFFECT is now an AttachmentType, not a Forge Capability.
 *
 * Migration guide:
 *   Old: entity.getCapability(CapabilityMobEffect.MOB_EFFECT).ifPresent(me -> { ... })
 *   New: entity.getData(CapabilityMobEffect.MOB_EFFECT).doStuff()  // never null
 */
public class CapabilityMobEffect {

    /**
     * Use entity.getData(MOB_EFFECT) to read — always returns a live instance.
     */
    public static AttachmentType<MobEffectState> MOB_EFFECT =
            ModAttachments.MOB_EFFECT.get();
}

package mods.flammpfeil.slashblade.capability.concentrationrank;

import mods.flammpfeil.slashblade.init.ModAttachments;
import net.neoforged.neoforge.attachment.AttachmentType;

/**
 * NeoForge 1.21.1: RANK_POINT is now an AttachmentType, not a Forge Capability.
 *
 * Migration guide:
 *   Old: entity.getCapability(CapabilityConcentrationRank.RANK_POINT).ifPresent(cr -> { ... })
 *   New: entity.getData(CapabilityConcentrationRank.RANK_POINT).doStuff()  // never null
 *
 *   Old: entity.getCapability(CapabilityConcentrationRank.RANK_POINT).map(...).orElse(def)
 *   New: mapper.apply(entity.getData(CapabilityConcentrationRank.RANK_POINT))
 */
public class CapabilityConcentrationRank {

    /**
     * Use entity.getData(RANK_POINT) to read — always returns a live instance.
     */
    public static AttachmentType<ConcentrationRank> RANK_POINT =
            ModAttachments.CONCENTRATION_RANK.get();
}

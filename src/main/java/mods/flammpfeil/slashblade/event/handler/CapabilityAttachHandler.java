package mods.flammpfeil.slashblade.event.handler;

/**
 * NeoForge 1.21.1: This class is no longer needed.
 *
 * In NeoForge 1.21.1, entity data (InputState, MobEffectState, ConcentrationRank) is
 * automatically initialized on first access via AttachmentType.builder(supplier).
 * There is no equivalent to AttachCapabilitiesEvent for AttachmentTypes.
 *
 * The old event registrations in SlashBlade.java have been removed.
 *
 * @deprecated Safe to delete; retained only as a documentation stub.
 */
@Deprecated
public class CapabilityAttachHandler {
    private CapabilityAttachHandler() {}
}

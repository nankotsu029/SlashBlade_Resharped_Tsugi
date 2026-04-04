package mods.flammpfeil.slashblade.init;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.concentrationrank.ConcentrationRank;
import mods.flammpfeil.slashblade.capability.inputstate.InputState;
import mods.flammpfeil.slashblade.capability.mobeffect.MobEffectState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * NeoForge 1.21.1 AttachmentType registration for entity-bound data.
 * Replaces the old Forge Capability system for entity Capabilities.
 *
 * Usage:
 *   entity.getData(ModAttachments.INPUT_STATE.get())           // always returns an instance (never null)
 *   entity.setData(ModAttachments.INPUT_STATE.get(), value)    // explicit set (optional for mutables)
 *   entity.hasData(ModAttachments.INPUT_STATE.get())           // check if already initialized
 */
public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, SlashBlade.MODID);

    /**
     * Replaces CapabilityConcentrationRank.RANK_POINT.
     * Serialized to disk (preserved across sessions).
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ConcentrationRank>> CONCENTRATION_RANK =
            ATTACHMENT_TYPES.register("concentration_rank", () ->
                    AttachmentType.<ConcentrationRank>builder(ConcentrationRank::new)
                            .serialize(ConcentrationRank.CODEC)
                            .build()
            );

    /**
     * Replaces CapabilityInputState.INPUT_STATE.
     * Transient (no serialization) — input state resets each session.
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<InputState>> INPUT_STATE =
            ATTACHMENT_TYPES.register("input_state", () ->
                    AttachmentType.<InputState>builder(InputState::new)
                            .build()
            );

    /**
     * Replaces CapabilityMobEffect.MOB_EFFECT.
     * Partially serialized (only stunTimeout).
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MobEffectState>> MOB_EFFECT =
            ATTACHMENT_TYPES.register("mob_effect", () ->
                    AttachmentType.<MobEffectState>builder(MobEffectState::new)
                            .serialize(MobEffectState.CODEC)
                            .build()
            );

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}
package mods.flammpfeil.slashblade.emi.mixin;

// TODO(neoforge-1.21.1): EMI 1.21.1 rewrote ItemEmiStackSerializer around
// DataComponentPatch-backed serialization. The old SlashBlade-specific NBT /
// capability injection no longer matches the upstream type shape, so this mixin
// is intentionally disabled until the SlashBlade item state is migrated to
// 1.21.1 data components and a new serializer hook is identified.
public final class MixinItemEmiStackSerializer {
    private MixinItemEmiStackSerializer() {
    }
}

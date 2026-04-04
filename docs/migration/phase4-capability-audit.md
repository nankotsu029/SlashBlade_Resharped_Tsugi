# Phase 4 Capability Audit

Current status for the NeoForge 1.21.1 capability migration.

## Old Capability API

Runtime usages of the old Forge capability API are gone.

- `ICapabilityProvider`: none
- `LazyOptional`: none
- `Capability<T>`: only migration guide stubs in the `capability/` package
- `AttachCapabilitiesEvent`: only documentation text in [`CapabilityAttachHandler`](../../src/main/java/mods/flammpfeil/slashblade/event/handler/CapabilityAttachHandler.java)
- `getCapability(...)`: only documentation text in the capability migration stubs
- `invalidateCaps()`: none

## Data Components

Item-bound blade state now lives in:

- [`ModDataComponents`](../../src/main/java/mods/flammpfeil/slashblade/init/ModDataComponents.java)
- [`SlashBladeState`](../../src/main/java/mods/flammpfeil/slashblade/capability/slashblade/SlashBladeState.java)
- [`ItemSlashBlade`](../../src/main/java/mods/flammpfeil/slashblade/item/ItemSlashBlade.java)

`ItemSlashBlade` now installs a default `BLADE_STATE` component and clones it per-stack in `verifyComponentsAfterLoad` to avoid sharing a mutable default instance.

Blade-state writes are now centralized through:

- `ItemSlashBlade.setBladeState(...)`
- `ItemSlashBlade.updateBladeState(...)`

This avoids mutating the shared default component instance on freshly created stacks.

## Attachments

Entity-bound data now lives in:

- [`ModAttachments`](../../src/main/java/mods/flammpfeil/slashblade/init/ModAttachments.java)
- `ConcentrationRank`
- `InputState`
- `MobEffectState`

## Remaining direct NBT/custom-data users

These are still item custom-data users and are not blade capability regressions:

- [`SlashBladeCreativeGroup`](../../src/main/java/mods/flammpfeil/slashblade/SlashBladeCreativeGroup.java)
- [`BladeStandItem`](../../src/main/java/mods/flammpfeil/slashblade/item/BladeStandItem.java)
- [`BlandStandEventHandler`](../../src/main/java/mods/flammpfeil/slashblade/event/bladestand/BlandStandEventHandler.java)
- [`AdvancementsRecipeRenderer`](../../src/main/java/mods/flammpfeil/slashblade/event/client/AdvancementsRecipeRenderer.java)

Use [`ItemStackDataCompat`](../../src/main/java/mods/flammpfeil/slashblade/util/ItemStackDataCompat.java) for any new custom item data writes.

## Follow-up

- Direct `stack.get(ItemSlashBlade.BLADESTATE)` / `stack.set(ItemSlashBlade.BLADESTATE, ...)` usages in runtime code are now removed.
- Continue converting legacy custom-data payloads that still live in `CUSTOM_DATA` where they should become first-class components.
- Rework high-frequency mutable `SlashBladeState` operations to use explicit write-back where aliasing risk remains.
- Migrate legacy advancement/resource NBT payloads that still reference `ForgeCaps`.
- Resolve the remaining compile blockers in recipe/serializer/combat code; these are no longer old Capability-system errors.

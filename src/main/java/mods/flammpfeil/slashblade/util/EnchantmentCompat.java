package mods.flammpfeil.slashblade.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.common.CommonHooks;
import org.jetbrains.annotations.Nullable;

public final class EnchantmentCompat {
    private EnchantmentCompat() {
    }

    public static Holder<Enchantment> resolve(RegistryAccess access, ResourceKey<Enchantment> key) {
        return access.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    public static Holder<Enchantment> resolve(LivingEntity entity, ResourceKey<Enchantment> key) {
        return resolve(entity.registryAccess(), key);
    }

    public static @Nullable Holder.Reference<Enchantment> resolve(ResourceLocation id) {
        HolderLookup.RegistryLookup<Enchantment> lookup = CommonHooks.resolveLookup(Registries.ENCHANTMENT);
        return lookup != null ? lookup.get(ResourceKey.create(Registries.ENCHANTMENT, id)).orElse(null) : null;
    }

    public static int getLevel(ItemStack stack, LivingEntity entity, ResourceKey<Enchantment> key) {
        return stack.getEnchantmentLevel(resolve(entity, key));
    }

    public static int getLevel(LivingEntity entity, ResourceKey<Enchantment> key) {
        return EnchantmentHelper.getEnchantmentLevel(resolve(entity, key), entity);
    }
}

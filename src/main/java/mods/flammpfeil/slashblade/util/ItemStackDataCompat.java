package mods.flammpfeil.slashblade.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public final class ItemStackDataCompat {
    private ItemStackDataCompat() {
    }

    public static @Nullable CompoundTag getCustomData(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : null;
    }

    public static @Nullable CompoundTag getCustomDataElement(ItemStack stack, String key) {
        CompoundTag tag = getCustomData(stack);
        if (tag == null || !tag.contains(key, CompoundTag.TAG_COMPOUND)) {
            return null;
        }
        return tag.getCompound(key);
    }

    public static boolean hasCustomData(ItemStack stack, String key) {
        CompoundTag tag = getCustomData(stack);
        return tag != null && tag.contains(key);
    }

    public static @Nullable String getString(ItemStack stack, String key) {
        CompoundTag tag = getCustomData(stack);
        return tag != null && tag.contains(key) ? tag.getString(key) : null;
    }

    public static void updateCustomData(ItemStack stack, Consumer<CompoundTag> updater) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, updater);
    }

    public static void putString(ItemStack stack, String key, String value) {
        updateCustomData(stack, tag -> tag.putString(key, value));
    }
}

package mods.flammpfeil.slashblade.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Map;

public class EnchantmentsHelper {
    //判断A是否含有B的附魔
    public static boolean hasEnchantmentsMatch(ItemStack stackA, ItemStack stackB) {
        Map<net.minecraft.core.Holder<Enchantment>, Integer> enchantmentsB = EnchantmentHelper.getEnchantmentsForCrafting(stackB)
                .entrySet()
                .stream()
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, entry -> entry.getIntValue()));

        // 如果B没有附魔要求，直接返回true
        if (enchantmentsB.isEmpty()) {
            return true;
        }

        Map<net.minecraft.core.Holder<Enchantment>, Integer> enchantmentsA = EnchantmentHelper.getEnchantmentsForCrafting(stackA)
                .entrySet()
                .stream()
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, entry -> entry.getIntValue()));

        for (Map.Entry<net.minecraft.core.Holder<Enchantment>, Integer> entry : enchantmentsB.entrySet()) {
            var ench = entry.getKey();
            int requiredLevel = entry.getValue();

            // 检查A是否包含该附魔且等级足够
            if (!enchantmentsA.containsKey(ench) || enchantmentsA.get(ench) < requiredLevel) {
                return false;
            }
        }
        return true;
    }
}

package mods.flammpfeil.slashblade.compat.emi;

import dev.emi.emi.api.stack.Comparison;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.world.item.ItemStack;

public class EMIUtils {

    public static Comparison SLASHBLADE_COMPARISON = Comparison.of((self, other) -> {
        ItemStack aStack = self.getItemStack();
        ItemStack bStack = other.getItemStack();
        if (aStack.getItem() != bStack.getItem()) {
            return false;
        }
        var stateA = ItemSlashBlade.getBladeState(aStack);
        var stateB = ItemSlashBlade.getBladeState(bStack);
        String keyA = stateA != null ? stateA.getTranslationKey() : "";
        String keyB = stateB != null ? stateB.getTranslationKey() : "";

        return keyB.equals(keyA);
    });
}

package mods.flammpfeil.slashblade.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.IExtendableSmithingRecipeCategory;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.recipe.SlashBladeSmithingRecipe;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class JEICompat implements IModPlugin {

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return SlashBlade.prefix(SlashBlade.MODID);
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(
                SlashBladeItems.SLASHBLADE.get(),
                new ISubtypeInterpreter<ItemStack>() {
                    @Override
                    public @NotNull Object getSubtypeData(@NotNull ItemStack stack, @NotNull UidContext context) {
                        var bs = ItemSlashBlade.getBladeState(stack);
                        return bs != null ? bs.getTranslationKey() : "";
                    }

                    @Override
                    @Deprecated(forRemoval = true)
                    public @NotNull String getLegacyStringSubtypeInfo(@NotNull ItemStack stack, @NotNull UidContext context) {
                        Object data = getSubtypeData(stack, context);
                        return String.valueOf(data);
                    }
                }
        );
    }

    public static String syncSlashBlade(ItemStack stack, UidContext context) {
        var bs = ItemSlashBlade.getBladeState(stack);
        return bs != null ? bs.getTranslationKey() : "";
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        IExtendableSmithingRecipeCategory smithingCategory = registration.getSmithingCategory();

        smithingCategory.addExtension(SlashBladeSmithingRecipe.class, new SlashBladeSmithingCategoryExtension());
    }

}

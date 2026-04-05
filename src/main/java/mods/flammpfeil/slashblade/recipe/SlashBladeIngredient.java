package mods.flammpfeil.slashblade.recipe;

import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public final class SlashBladeIngredient {
    private SlashBladeIngredient() {
    }

    public static Ingredient of(ItemLike item, RequestDefinition request) {
        return create(item, request);
    }

    public static Ingredient of(RequestDefinition request) {
        return create(SlashBladeItems.SLASHBLADE.get(), request);
    }

    public static Ingredient of(ItemLike item, ResourceLocation request) {
        return of(item, RequestDefinition.Builder.newInstance().name(request).build());
    }

    public static Ingredient of(ResourceLocation request) {
        return of(RequestDefinition.Builder.newInstance().name(request).build());
    }

    public static Ingredient blankNameless() {
        return of(RequestDefinition.Builder.newInstance().build());
    }

    private static Ingredient create(ItemLike item, RequestDefinition request) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item.asItem());
        if (itemId == null) {
            throw new IllegalArgumentException("Cannot create a SlashBladeIngredient for an unregistered item");
        }
        return new BladeIngredient(itemId, request).toVanilla();
    }
}

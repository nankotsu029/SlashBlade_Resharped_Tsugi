package mods.flammpfeil.slashblade.recipe;

import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.Set;
import java.util.stream.Stream;

public final class SlashBladeIngredient {
    private SlashBladeIngredient() {
    }

    public static Ingredient of(ItemLike item, RequestDefinition request) {
        return create(Set.of(item.asItem()), request);
    }

    public static Ingredient of(RequestDefinition request) {
        return create(Set.of(SlashBladeItems.SLASHBLADE.get()), request);
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

    private static Ingredient create(Set<Item> items, RequestDefinition request) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Cannot create a SlashBladeIngredient with no items");
        }

        // TODO(neoforge-1.21.1): Replace this exact-stack fallback with an ICustomIngredient once the
        // custom recipe JSON format is migrated. This keeps datagen and simple runtime checks compiling.
        return Ingredient.of(items.stream().flatMap(item -> Stream.of(createRequestedStack(item, request))));
    }

    private static ItemStack createRequestedStack(Item item, RequestDefinition request) {
        ItemStack stack = new ItemStack(item);
        request.initItemStack(stack);
        return stack;
    }
}

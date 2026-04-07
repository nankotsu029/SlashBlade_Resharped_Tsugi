package mods.flammpfeil.slashblade.recipe;

import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RecipeSerializerRegistry {

    private RecipeSerializerRegistry() {
    }

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, SlashBlade.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SlashBladeShapedRecipe>> SLASHBLADE_SHAPED =
            RECIPE_SERIALIZERS.register("shaped_blade", () -> SlashBladeShapedRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ProudsoulShapelessRecipe>> PROUDSOUL_RECIPE =
            RECIPE_SERIALIZERS.register("proudsoul", () -> ProudsoulShapelessRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SlashBladeSmithingRecipe>> SLASHBLADE_SMITHING =
            RECIPE_SERIALIZERS.register("slashblade_smithing", () -> SlashBladeSmithingRecipe.SERIALIZER);
}
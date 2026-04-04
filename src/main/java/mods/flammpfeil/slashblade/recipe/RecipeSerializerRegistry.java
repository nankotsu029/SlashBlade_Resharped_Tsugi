package mods.flammpfeil.slashblade.recipe;
import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RecipeSerializerRegistry {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister
            .create(Registries.RECIPE_TYPE, SlashBlade.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER = DeferredRegister
            .create(Registries.RECIPE_SERIALIZER, SlashBlade.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> SLASHBLADE_SHAPED = RECIPE_SERIALIZER
            .register("shaped_blade", () -> SlashBladeShapedRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> PROUDSOUL_RECIPE = RECIPE_SERIALIZER
            .register("proudsoul", () -> ProudsoulShapelessRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> SLASHBLADE_SMITHING = RECIPE_SERIALIZER
            .register("slashblade_smithing", () -> SlashBladeSmithingRecipe.SERIALIZER);

}

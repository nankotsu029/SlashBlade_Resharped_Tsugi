package mods.flammpfeil.slashblade.init;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.recipe.BladeIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModIngredientTypes {
    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, SlashBlade.MODID);

    public static final DeferredHolder<IngredientType<?>, IngredientType<BladeIngredient>> BLADE = INGREDIENT_TYPES
            .register("blade", () -> new IngredientType<>(BladeIngredient.CODEC, BladeIngredient.STREAM_CODEC));

    private ModIngredientTypes() {
    }
}

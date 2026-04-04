package mods.flammpfeil.slashblade.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiFunction;

public record SlashBladeShapedRecipeSerializer<T extends Recipe<?>, U extends T>(
        RecipeSerializer<T> compose,
        BiFunction<T, @Nullable ResourceLocation, U> converter
) implements RecipeSerializer<U> {
    @Override
    public @NotNull MapCodec<U> codec() {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                compose().codec().forGetter(recipe -> (T) recipe),
                ResourceLocation.CODEC.optionalFieldOf("blade").forGetter(recipe -> {
                    if (recipe instanceof SlashBladeShapedRecipe bladeRecipe) {
                        return Optional.ofNullable(bladeRecipe.getOutputBlade());
                    }
                    return Optional.empty();
                })
        ).apply(instance, (recipe, blade) -> converter().apply(recipe, blade.orElse(null))));
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, U> streamCodec() {
        return StreamCodec.composite(
                compose().streamCodec(),
                recipe -> (T) recipe,
                ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
                recipe -> {
                    if (recipe instanceof SlashBladeShapedRecipe bladeRecipe) {
                        return Optional.ofNullable(bladeRecipe.getOutputBlade());
                    }
                    return Optional.empty();
                },
                (recipe, blade) -> converter().apply(recipe, blade.orElse(null))
        );
    }
}

package mods.flammpfeil.slashblade.recipe;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ProudsoulShapelessRecipe extends ShapelessRecipe {
    public static final RecipeSerializer<ProudsoulShapelessRecipe> SERIALIZER = new Serializer();

    public ProudsoulShapelessRecipe(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients) {
        super(group, category, result, ingredients);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput container, @NotNull HolderLookup.Provider access) {
        ItemStack result = super.assemble(container, access);
        ItemEnchantments.Mutable all = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(result));

        for (int idx = 0; idx < container.size(); idx++) {
            ItemStack stack = container.getItem(idx);
            if (stack.isEmpty() || !stack.isEnchanted()) {
                continue;
            }

            var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
            for (var entry : enchantments.entrySet()) {
                all.upgrade(entry.getKey(), entry.getIntValue());
            }
        }

        EnchantmentHelper.setEnchantments(result, all.toImmutable());
        return result;
    }

    @Override
    public boolean matches(@NotNull CraftingInput container, @NotNull Level level) {
        boolean result = super.matches(container, level);

        if (result) {
            Map<Holder<Enchantment>, Integer> all = Maps.newHashMap();
            int soulCount = 0;

            for (int idx = 0; idx < container.size(); idx++) {
                ItemStack stack = container.getItem(idx);
                if (stack.isEmpty() || !stack.isEnchanted()) {
                    continue;
                }

                soulCount++;
                var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);

                for (var entry : enchantments.entrySet()) {
                    all.merge(entry.getKey(), entry.getIntValue(), Integer::sum);
                }
            }

            result = all.size() == 1 || all.isEmpty();
            if (result) {
                for (var entry : all.entrySet()) {
                    result = entry.getValue() == soulCount;
                }
            }
        }

        return result;
    }

    public static class Serializer implements RecipeSerializer<ProudsoulShapelessRecipe> {
        private static final MapCodec<ProudsoulShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(ProudsoulShapelessRecipe::getGroup),
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ProudsoulShapelessRecipe::category),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.getResultItem(null)),
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap(
                        ingredients -> {
                            Ingredient[] array = ingredients.toArray(Ingredient[]::new);
                            if (array.length == 0) {
                                return DataResult.error(() -> "No ingredients for shapeless recipe");
                            }
                            if (array.length > 3 * 3) {
                                return DataResult.error(() -> "Too many ingredients for shapeless recipe. The maximum is: "
                                        + (3 * 3));
                            }
                            return DataResult.success(NonNullList.of(Ingredient.EMPTY, array));
                        },
                        DataResult::success
                ).forGetter(ProudsoulShapelessRecipe::getIngredients)
        ).apply(instance, ProudsoulShapelessRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, ProudsoulShapelessRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork,
                Serializer::fromNetwork
        );

        @Override
        public MapCodec<ProudsoulShapelessRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ProudsoulShapelessRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static ProudsoulShapelessRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            String group = buf.readUtf();
            CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);
            int count = buf.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(count, Ingredient.EMPTY);
            ingredients.replaceAll(ignored -> Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
            ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
            return new ProudsoulShapelessRecipe(group, category, result, ingredients);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, ProudsoulShapelessRecipe recipe) {
            buf.writeUtf(recipe.getGroup());
            buf.writeEnum(recipe.category());
            buf.writeVarInt(recipe.getIngredients().size());
            for (Ingredient ingredient : recipe.getIngredients()) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
            }
            ItemStack.STREAM_CODEC.encode(buf, recipe.getResultItem(null));
        }
    }
}

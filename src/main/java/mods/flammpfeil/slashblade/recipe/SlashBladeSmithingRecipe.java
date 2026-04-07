package mods.flammpfeil.slashblade.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mods.flammpfeil.slashblade.SlashBladeConfig;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.stream.Stream;

public class SlashBladeSmithingRecipe implements SmithingRecipe {
    public static final RecipeSerializer<SlashBladeSmithingRecipe> SERIALIZER = new Serializer();

    private final ResourceLocation outputBlade;
    private final ResourceLocation id;
    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;

    public SlashBladeSmithingRecipe(ResourceLocation id, ResourceLocation outputBlade, Ingredient template, Ingredient base, Ingredient addition) {
        this.id = id;
        this.outputBlade = outputBlade;
        this.template = template;
        this.base = base;
        this.addition = addition;
    }

    public SlashBladeSmithingRecipe(ResourceLocation outputBlade, Ingredient template, Ingredient base, Ingredient addition) {
        this(outputBlade, outputBlade, template, base, addition);
    }

    public Ingredient getTemplate() {
        return template;
    }

    public Ingredient getBase() {
        return base;
    }

    public Ingredient getAddition() {
        return addition;
    }

    public ResourceLocation getOutputBlade() {
        return outputBlade;
    }

    public ResourceLocation getId() {
        return id;
    }

    private static ItemStack getResultBlade(ResourceLocation outputBlade) {
        Item bladeItem = BuiltInRegistries.ITEM.getOptional(outputBlade).orElse(SlashBladeItems.SLASHBLADE.get());
        return Objects.requireNonNullElseGet(bladeItem, SlashBladeItems.SLASHBLADE).getDefaultInstance();
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider access) {
        ItemStack result = getResultBlade(this.outputBlade);

        if (!Objects.equals(BuiltInRegistries.ITEM.getKey(result.getItem()), this.outputBlade)) {
            result = access.lookupOrThrow(SlashBladeDefinition.REGISTRY_KEY)
                    .getOrThrow(net.minecraft.resources.ResourceKey.create(SlashBladeDefinition.REGISTRY_KEY, this.outputBlade))
                    .value()
                    .getBlade();
        }

        return result;
    }

    @Override
    public boolean matches(SmithingRecipeInput input, @NotNull Level level) {
        return this.template.test(input.template())
                && this.base.test(input.base())
                && this.addition.test(input.addition());
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull SmithingRecipeInput input, @NotNull HolderLookup.Provider access) {
        ItemStack result = this.getResultItem(access);
        if (!(result.getItem() instanceof ItemSlashBlade)) {
            result = new ItemStack(SlashBladeItems.SLASHBLADE.get());
        }

        var resultState = ItemSlashBlade.getOrCreateBladeState(result);
        ItemStack stack = input.base();
        var ingredientState = ItemSlashBlade.getBladeState(stack);
        if (ingredientState == null) {
            return ItemStack.EMPTY;
        }

        resultState.setProudSoulCount(resultState.getProudSoulCount() + ingredientState.getProudSoulCount());
        resultState.setKillCount(resultState.getKillCount() + ingredientState.getKillCount());
        resultState.setRefine(
                SlashBladeConfig.DO_CRAFTING_SUM_REFINE.get()
                        ? resultState.getRefine() + ingredientState.getRefine()
                        : Math.max(resultState.getRefine(), ingredientState.getRefine())
        );

        ItemSlashBlade.setBladeState(result, resultState);
        updateEnchantment(result, stack, access);

        return result;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public boolean isIncomplete() {
        return Stream.of(this.template, this.base, this.addition).anyMatch(Ingredient::hasNoItems);
    }

    @Override
    public boolean isTemplateIngredient(@NotNull ItemStack stack) {
        return this.template.test(stack);
    }

    @Override
    public boolean isBaseIngredient(@NotNull ItemStack stack) {
        return this.base.test(stack);
    }

    @Override
    public boolean isAdditionIngredient(@NotNull ItemStack stack) {
        return this.addition.test(stack);
    }

    private static void updateEnchantment(ItemStack result, ItemStack ingredient, HolderLookup.Provider access) {
        var lookup = access.lookupOrThrow(Registries.ENCHANTMENT);
        ItemEnchantments.Mutable merged = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(result));
        var oldItemEnchants = ingredient.getAllEnchantments(lookup);

        for (var entry : oldItemEnchants.entrySet()) {
            Holder<Enchantment> enchantment = entry.getKey();
            int mergedLevel = Math.max(entry.getIntValue(), merged.getLevel(enchantment));
            mergedLevel = Math.min(mergedLevel, enchantment.value().getMaxLevel());

            boolean canApply = enchantment.value().canEnchant(result);
            if (canApply) {
                for (Holder<Enchantment> current : merged.keySet()) {
                    if (!current.equals(enchantment) && !Enchantment.areCompatible(enchantment, current)) {
                        canApply = false;
                        break;
                    }
                }
            }

            if (canApply) {
                merged.set(enchantment, mergedLevel);
            }
        }

        EnchantmentHelper.setEnchantments(result, merged.toImmutable());
    }

    public static class Serializer implements RecipeSerializer<SlashBladeSmithingRecipe> {
        private static final MapCodec<SlashBladeSmithingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("blade").forGetter(SlashBladeSmithingRecipe::getOutputBlade),
                Ingredient.CODEC.fieldOf("template").forGetter(SlashBladeSmithingRecipe::getTemplate),
                Ingredient.CODEC.fieldOf("base").forGetter(SlashBladeSmithingRecipe::getBase),
                Ingredient.CODEC.fieldOf("addition").forGetter(SlashBladeSmithingRecipe::getAddition)
        ).apply(instance, SlashBladeSmithingRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, SlashBladeSmithingRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork,
                Serializer::fromNetwork
        );

        @Override
        public MapCodec<SlashBladeSmithingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SlashBladeSmithingRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static SlashBladeSmithingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            Ingredient template = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            Ingredient base = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            Ingredient addition = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            ResourceLocation blade = buffer.readResourceLocation();
            return new SlashBladeSmithingRecipe(blade, template, base, addition);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, SlashBladeSmithingRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.template);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.base);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.addition);
            buffer.writeResourceLocation(recipe.outputBlade);
        }
    }
}

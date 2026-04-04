package mods.flammpfeil.slashblade.recipe;

import mods.flammpfeil.slashblade.SlashBladeConfig;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SlashBladeShapedRecipe extends ShapedRecipe {
    public static final RecipeSerializer<SlashBladeShapedRecipe> SERIALIZER = new SlashBladeShapedRecipeSerializer<>(
            RecipeSerializer.SHAPED_RECIPE, SlashBladeShapedRecipe::new);

    private final ResourceLocation outputBlade;

    public SlashBladeShapedRecipe(ShapedRecipe compose, ResourceLocation outputBlade) {
        super(compose.getGroup(), compose.category(), compose.pattern, getResultBlade(outputBlade), compose.showNotification());
        this.outputBlade = outputBlade;
    }

    private static ItemStack getResultBlade(ResourceLocation outputBlade) {
        Item bladeItem = BuiltInRegistries.ITEM.getOptional(outputBlade).orElse(SlashBladeItems.SLASHBLADE.get());
        return Objects.requireNonNullElseGet(bladeItem, SlashBladeItems.SLASHBLADE).getDefaultInstance();
    }

    public ResourceLocation getOutputBlade() {
        return outputBlade;
    }

    private ResourceKey<SlashBladeDefinition> getOutputBladeKey() {
        return ResourceKey.create(SlashBladeDefinition.REGISTRY_KEY, outputBlade);
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider access) {
        ItemStack result = getResultBlade(this.outputBlade);

        if (!Objects.equals(BuiltInRegistries.ITEM.getKey(result.getItem()), this.outputBlade)) {
            result = access.lookupOrThrow(SlashBladeDefinition.REGISTRY_KEY).getOrThrow(getOutputBladeKey()).value().getBlade();
        }

        return result;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput input, @NotNull HolderLookup.Provider access) {
        ItemStack result = this.getResultItem(access);
        if (!(result.getItem() instanceof ItemSlashBlade)) {
            result = new ItemStack(SlashBladeItems.SLASHBLADE.get());
        }

        var resultState = ItemSlashBlade.getOrCreateBladeState(result);
        boolean sumRefine = SlashBladeConfig.DO_CRAFTING_SUM_REFINE.get();
        int proudSoul = resultState.getProudSoulCount();
        int killCount = resultState.getKillCount();
        int refine = resultState.getRefine();

        for (var stack : input.items()) {
            if (!(stack.getItem() instanceof ItemSlashBlade)) {
                continue;
            }
            var ingredientState = ItemSlashBlade.getBladeState(stack);
            if (ingredientState == null) {
                continue;
            }

            proudSoul += ingredientState.getProudSoulCount();
            killCount += ingredientState.getKillCount();
            refine = sumRefine ? refine + ingredientState.getRefine() : Math.max(refine, ingredientState.getRefine());
            updateEnchantment(result, stack, access);
        }

        resultState.setProudSoulCount(proudSoul);
        resultState.setKillCount(killCount);
        resultState.setRefine(refine);
        ItemSlashBlade.setBladeState(result, resultState);

        return result;
    }

    private static void updateEnchantment(ItemStack result, ItemStack ingredient, HolderLookup.Provider access) {
        var lookup = access.lookupOrThrow(Registries.ENCHANTMENT);
        ItemEnchantments.Mutable merged = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(result));
        var source = ingredient.getAllEnchantments(lookup);

        for (var entry : source.entrySet()) {
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

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}

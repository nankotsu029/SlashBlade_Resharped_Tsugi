package mods.flammpfeil.slashblade.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mods.flammpfeil.slashblade.init.ModIngredientTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.Objects;
import java.util.stream.Stream;

public record BladeIngredient(ResourceLocation item, RequestDefinition request) implements ICustomIngredient {
    private static final RequestDefinition EMPTY_REQUEST = RequestDefinition.Builder.newInstance().build();

    public static final MapCodec<BladeIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("item").forGetter(BladeIngredient::item),
            RequestDefinition.CODEC.optionalFieldOf("request", EMPTY_REQUEST).forGetter(BladeIngredient::request)
    ).apply(instance, BladeIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BladeIngredient> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            BladeIngredient::item,
            RequestDefinition.STREAM_CODEC,
            BladeIngredient::request,
            BladeIngredient::new
    );

    public BladeIngredient {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(request, "request");
    }

    @Override
    public boolean test(ItemStack stack) {
        Item expectedItem = resolveItem();
        return expectedItem != null && stack.is(expectedItem) && request.test(stack);
    }

    @Override
    public Stream<ItemStack> getItems() {
        Item displayItem = resolveItem();
        if (displayItem == null) {
            return Stream.empty();
        }

        ItemStack displayStack = new ItemStack(displayItem);
        request.initItemStack(displayStack);
        return Stream.of(displayStack);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return ModIngredientTypes.BLADE.get();
    }

    private Item resolveItem() {
        return BuiltInRegistries.ITEM.getOptional(item).orElse(null);
    }
}

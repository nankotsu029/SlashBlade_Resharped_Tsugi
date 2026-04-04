package mods.flammpfeil.slashblade.registry.slashblade;

// TODO(neoforge-1.21.1): This file still uses Forge-only APIs that need a manual NeoForge rewrite.
// TODO(neoforge-1.21.1): Replace remaining ForgeRegistries references with BuiltInRegistries, Registries, or NeoForgeRegistries as appropriate.
// TODO(neoforge-1.21.1): Replace ForgeRegistries.ENCHANTMENTS with a RegistryAccess/Registries.ENCHANTMENT lookup.
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.SlashBladeCreativeGroup;
import mods.flammpfeil.slashblade.capability.slashblade.SlashBladeState;
import mods.flammpfeil.slashblade.event.SlashBladeRegistryEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.util.EnchantmentCompat;
import net.minecraft.Util;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class SlashBladeDefinition {

    public static final Codec<SlashBladeDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ResourceLocation.CODEC.optionalFieldOf("item", SlashBlade.prefix("slashblade"))
                            .forGetter(SlashBladeDefinition::getItemName),
                    ResourceLocation.CODEC.fieldOf("name").forGetter(SlashBladeDefinition::getName),
                    RenderDefinition.CODEC.fieldOf("render").forGetter(SlashBladeDefinition::getRenderDefinition),
                    PropertiesDefinition.CODEC.fieldOf("properties").forGetter(SlashBladeDefinition::getStateDefinition),
                    EnchantmentDefinition.CODEC.listOf().optionalFieldOf("enchantments", Lists.newArrayList())
                            .forGetter(SlashBladeDefinition::getEnchantments),
                    ResourceLocation.CODEC.optionalFieldOf("creativeGroup", SlashBladeCreativeGroup.SLASHBLADE_GROUP.getId())
                            .forGetter(SlashBladeDefinition::getCreativeGroup))
            .apply(instance, SlashBladeDefinition::new));

    public static final ResourceKey<Registry<SlashBladeDefinition>> REGISTRY_KEY = ResourceKey
            .createRegistryKey(SlashBlade.prefix("named_blades"));

    private final ResourceLocation item;
    private final ResourceLocation name;
    private final RenderDefinition renderDefinition;
    private final PropertiesDefinition stateDefinition;
    private final List<EnchantmentDefinition> enchantments;

    private final ResourceLocation creativeGroup;

    public SlashBladeDefinition(ResourceLocation name, RenderDefinition renderDefinition,
                                PropertiesDefinition stateDefinition, List<EnchantmentDefinition> enchantments) {
        this(SlashBlade.prefix("slashblade"), name, renderDefinition, stateDefinition, enchantments,
                SlashBladeCreativeGroup.SLASHBLADE_GROUP.getId());
    }

    public SlashBladeDefinition(ResourceLocation name, RenderDefinition renderDefinition,
                                PropertiesDefinition stateDefinition, List<EnchantmentDefinition> enchantments,
                                ResourceLocation creativeGroup) {
        this(SlashBlade.prefix("slashblade"), name, renderDefinition, stateDefinition, enchantments, creativeGroup);
    }

    public SlashBladeDefinition(ResourceLocation item, ResourceLocation name, RenderDefinition renderDefinition,
                                PropertiesDefinition stateDefinition, List<EnchantmentDefinition> enchantments) {
        this(item, name, renderDefinition, stateDefinition, enchantments,
                SlashBladeCreativeGroup.SLASHBLADE_GROUP.getId());
    }


    public SlashBladeDefinition(ResourceLocation item, ResourceLocation name, RenderDefinition renderDefinition,
                                PropertiesDefinition stateDefinition, List<EnchantmentDefinition> enchantments,
                                ResourceLocation creativeGroup) {
        this.item = item;
        this.name = name;
        this.renderDefinition = renderDefinition;
        this.stateDefinition = stateDefinition;
        this.enchantments = enchantments;
        this.creativeGroup = creativeGroup;
    }

    public ResourceLocation getItemName() {
        return item;
    }

    public ResourceLocation getName() {
        return name;
    }

    public String getTranslationKey() {
        return Util.makeDescriptionId("item", this.getName());
    }

    public RenderDefinition getRenderDefinition() {
        return renderDefinition;
    }

    public PropertiesDefinition getStateDefinition() {
        return stateDefinition;
    }

    public List<EnchantmentDefinition> getEnchantments() {
        return enchantments;
    }

    public ItemStack getBlade() {
        return getBlade(getItem());
    }

    public ItemStack getBlade(Item bladeItem) {

        var preEvent = new SlashBladeRegistryEvent.Pre(this);
        NeoForge.EVENT_BUS.post(preEvent);
        if (preEvent.isCanceled()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = new ItemStack(bladeItem);
        var state = ItemSlashBlade.getBladeState(result);
        if (state == null) state = ItemSlashBlade.getOrCreateBladeState(result);
        state.setNonEmpty();
        state.setBaseAttackModifier(this.stateDefinition.getBaseAttackModifier());
        state.setMaxDamage(this.stateDefinition.getMaxDamage());
        state.setComboRoot(this.stateDefinition.getComboRoot());
        state.setSlashArtsKey(this.stateDefinition.getSpecialAttackType());

        this.stateDefinition.getSpecialEffects().forEach(state::addSpecialEffect);

        final var bladeState = state;
        this.stateDefinition.getDefaultType().forEach(type -> {
            switch (type) {
                case BEWITCHED -> bladeState.setDefaultBewitched(true);
                case BROKEN -> {
                    bladeState.setDamage(result.getMaxDamage() - 1);
                    bladeState.setBroken(true);
                }
                case SEALED -> bladeState.setSealed(true);
                default -> {
                }
            }
        });

        state.setModel(this.renderDefinition.getModelName());
        state.setTexture(this.renderDefinition.getTextureName());
        state.setColorCode(this.renderDefinition.getSummonedSwordColor());
        state.setEffectColorInverse(this.renderDefinition.isSummonedSwordColorInverse());
        state.setCarryType(this.renderDefinition.getStandbyRenderType());
        if (!this.getName().equals(SlashBlade.prefix("none"))) {
            state.setTranslationKey(this.getTranslationKey());
        }

        ItemSlashBlade.setBladeState(result, state);

        for (var instance : this.enchantments) {
            var enchantment = EnchantmentCompat.resolve(instance.getEnchantmentID());
            if (enchantment != null) {
                result.enchant(enchantment, instance.getEnchantmentLevel());
            }

        }
        if (this.stateDefinition.isUnbreakable()) {
            result.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        }
        var postRegistry = new SlashBladeRegistryEvent.Post(this, result);
        NeoForge.EVENT_BUS.post(postRegistry);
        return postRegistry.getBlade();
    }

    public Item getItem() {
        @Nullable
        Item value = BuiltInRegistries.ITEM.getOptional(this.item).orElse(null);
        if (value == null) {
            return SlashBladeItems.SLASHBLADE.get();
        }
        return value;
    }

    public ResourceLocation getCreativeGroup() {
        return creativeGroup;
    }

    public static final BladeComparator COMPARATOR = new BladeComparator();

    public static class BladeComparator implements Comparator<Reference<SlashBladeDefinition>> {
        @Override
        public int compare(Reference<SlashBladeDefinition> left, Reference<SlashBladeDefinition> right) {

            ResourceLocation leftKey = left.key().location();
            ResourceLocation rightKey = right.key().location();
            boolean checkSame = leftKey.getNamespace().equalsIgnoreCase(rightKey.getNamespace());
            if (!checkSame) {
                if (leftKey.getNamespace().equalsIgnoreCase(SlashBlade.MODID)) {
                    return -1;
                }

                if (rightKey.getNamespace().equalsIgnoreCase(SlashBlade.MODID)) {
                    return 1;
                }
            }
            String leftName = leftKey.toString();
            String rightName = rightKey.toString();

            return leftName.compareToIgnoreCase(rightName);
        }
    }
}

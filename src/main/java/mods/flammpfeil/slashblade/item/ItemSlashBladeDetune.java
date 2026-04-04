package mods.flammpfeil.slashblade.item;

import mods.flammpfeil.slashblade.capability.slashblade.SlashBladeState;
import mods.flammpfeil.slashblade.init.DefaultResources;
import mods.flammpfeil.slashblade.init.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
/**
 * A fixed-definition SlashBlade with hardcoded model, texture, and base attack.
 *
 * NeoForge 1.21.1: initCapabilities() removed.
 * Static properties (model, texture, baseAttack) are stored on the item instance.
 * Per-stack mutable data is in the BLADE_STATE DataComponent.
 *
 * When reading model/texture from a stack, the item's own values are the canonical defaults;
 * they can be overridden by values stored in the DataComponent.
 */
public class ItemSlashBladeDetune extends ItemSlashBlade {
    private ResourceLocation model;
    private ResourceLocation texture;
    private final float baseAttack;
    private boolean isDestructable;

    public ItemSlashBladeDetune(Tier tier, int attackDamageIn, float attackSpeedIn, Properties builder) {
        super(tier, attackDamageIn, attackSpeedIn, builder);
        this.baseAttack = attackDamageIn;
        this.isDestructable = false;
        this.model = DefaultResources.resourceDefaultModel;
        this.texture = DefaultResources.resourceDefaultTexture;
    }

    public ResourceLocation getModel() {
        return model;
    }

    public ItemSlashBladeDetune setModel(ResourceLocation model) {
        this.model = model;
        return this;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public ItemSlashBladeDetune setTexture(ResourceLocation texture) {
        this.texture = texture;
        return this;
    }

    public boolean isDestructable() {
        return isDestructable;
    }

    public ItemSlashBladeDetune setDestructable() {
        this.isDestructable = true;
        return this;
    }

    @Override
    public boolean isDestructable(ItemStack stack) {
        return this.isDestructable;
    }

    /**
     * Returns the blade state for this stack, initializing it with item-level defaults if absent.
     * For ItemSlashBladeDetune, model/texture/attack override values that may not be set in NBT.
     */
    public SlashBladeState getOrInitBladeState(ItemStack stack) {
        SlashBladeState state = ItemSlashBlade.getOrCreateBladeState(stack);
        state = initializeBladeState(stack, state);
        ItemSlashBlade.setBladeState(stack, state);
        return state;
    }

    @Override
    protected SlashBladeState initializeBladeState(ItemStack stack, SlashBladeState state) {
        state = super.initializeBladeState(stack, state);
        state.setNonEmpty();
        state.setModel(this.model);
        state.setTexture(this.texture);
        state.setBaseAttackModifier(this.baseAttack);
        state.setMaxDamage(this.getTier().getUses());
        return state;
    }

    @Override
    public void appendSwordType(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        // ItemSlashBladeDetune items don't show sword type tooltip
    }
}

package mods.flammpfeil.slashblade.item;

import com.google.common.collect.*;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.SlashBladeConfig;
import mods.flammpfeil.slashblade.capability.inputstate.InputState;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.capability.slashblade.SlashBladeState;
import mods.flammpfeil.slashblade.client.renderer.SlashBladeTEISR;
import mods.flammpfeil.slashblade.data.tag.SlashBladeItemTags;
import mods.flammpfeil.slashblade.entity.BladeItemEntity;
import mods.flammpfeil.slashblade.event.SlashBladeEvent;
import mods.flammpfeil.slashblade.init.DefaultResources;
import mods.flammpfeil.slashblade.init.ModAttachments;
import mods.flammpfeil.slashblade.init.ModDataComponents;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.registry.combo.ComboState;
import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import mods.flammpfeil.slashblade.util.InputCommand;
import mods.flammpfeil.slashblade.util.EnchantmentCompat;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class ItemSlashBlade extends SwordItem {
    protected static final ResourceLocation ATTACK_DAMAGE_AMPLIFIER = SlashBlade.prefix("attack_damage_amplifier");
    // TODO(neoforge-1.21.1): Reintroduce custom reach via the 1.21 attribute/item system once the old NeoForge reach attribute usage is replaced.

    /** DataComponentType for per-stack blade state. Use stack.get(BLADESTATE) to read (nullable). */
    public static final DataComponentType<SlashBladeState> BLADESTATE = ModDataComponents.BLADE_STATE.get();
    /** AttachmentType for per-entity input state. Use entity.getData(INPUT_STATE) to read (never null). */
    public static final AttachmentType<InputState> INPUT_STATE = ModAttachments.INPUT_STATE.get();

    public static final List<ResourceKey<Enchantment>> exEnchantment = List.of(
            Enchantments.SOUL_SPEED,
            Enchantments.POWER,
            Enchantments.FEATHER_FALLING,
            Enchantments.FIRE_PROTECTION,
            Enchantments.THORNS);

    public ItemSlashBlade(Tier tier, int attackDamageIn, float attackSpeedIn, Properties builder) {
        super(tier, withDefaultBladeState(builder).attributes(SwordItem.createAttributes(tier, attackDamageIn, attackSpeedIn)));
    }

    private static Properties withDefaultBladeState(Properties builder) {
        return builder.component(ModDataComponents.BLADE_STATE.get(), new SlashBladeState());
    }

    public static @Nullable SlashBladeState getBladeState(ItemStack stack) {
        SlashBladeState state = stack.get(BLADESTATE);
        return state == null || state.isEmpty() ? null : state;
    }

    public static SlashBladeState getOrCreateBladeState(ItemStack stack) {
        SlashBladeState state = stack.get(BLADESTATE);
        if (state == null) {
            state = new SlashBladeState();
        } else {
            state = state.copy();
        }
        return state;
    }

    public static void setBladeState(ItemStack stack, SlashBladeState state) {
        state.setNonEmpty();
        if (stack.getItem() instanceof ItemSlashBlade bladeItem) {
            stack.set(BLADESTATE, bladeItem.initializeBladeState(stack, state));
            return;
        }
        stack.set(BLADESTATE, state);
    }

    public static void updateBladeState(ItemStack stack, Consumer<SlashBladeState> updater) {
        SlashBladeState state = getOrCreateBladeState(stack);
        updater.accept(state);
        setBladeState(stack, state);
    }

    protected SlashBladeState initializeBladeState(ItemStack stack, SlashBladeState state) {
        if (state.getMaxDamage() <= 0) {
            state.setMaxDamage(stack.getMaxDamage());
        }
        return state;
    }

    @Override
    public void verifyComponentsAfterLoad(ItemStack stack) {
        super.verifyComponentsAfterLoad(stack);
        SlashBladeState state = stack.get(BLADESTATE);
        if (state == null) {
            return;
        }
        setBladeState(stack, state.copy());
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        if (exEnchantment.stream().anyMatch(enchantment::is)) {
            return true;
        }
        return super.supportsEnchantment(stack, enchantment);
    }

    @Override
    public String getCreatorModId(ItemStack itemStack) {
        return this.getBladeId(itemStack).getNamespace();
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        ItemAttributeModifiers defaults = super.getDefaultAttributeModifiers(stack);
        SlashBladeState s = getBladeState(stack);
        if (s == null) {
            return defaults;
        }

        var swordType = SwordType.from(stack);
        float baseAttackModifier = s.getBaseAttackModifier();
        int refine = s.getRefine();

        float attackAmplifier;
        if (s.isBroken()) {
            attackAmplifier = -0.5F - baseAttackModifier;
        } else {
            float refineFactor = swordType.contains(SwordType.FIERCEREDGE) ? 0.1F : 0.05F;
            attackAmplifier = (1.0F - (1.0F / (1.0F + (refineFactor * refine)))) * baseAttackModifier;
        }

        double damage = (double) baseAttackModifier + attackAmplifier - 1F;

        var event = new SlashBladeEvent.UpdateAttackEvent(stack, s, damage);
        NeoForge.EVENT_BUS.post(event);

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        for (ItemAttributeModifiers.Entry entry : defaults.modifiers()) {
            if (!entry.attribute().equals(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)) {
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        }

        builder.add(
                net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_ID, event.getNewDamage(), AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
        return builder.build();
    }

    public @NotNull Rarity getRarity(@NotNull ItemStack stack) {
        EnumSet<SwordType> type = SwordType.from(stack);
        if (type.contains(SwordType.BEWITCHED)) {
            return Rarity.EPIC;
        }
        if (type.contains(SwordType.ENCHANTED)) {
            return Rarity.RARE;
        }
        return Rarity.COMMON;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 72000;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level worldIn, Player playerIn, @NotNull InteractionHand handIn) {
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        if (handIn == InteractionHand.OFF_HAND && !(playerIn.getMainHandItem().getItem() instanceof ItemSlashBlade)) {
            return InteractionResultHolder.pass(itemstack);
        }
        SlashBladeState bladeState = getBladeState(itemstack);
        boolean result = false;
        if (bladeState != null) {
            InputState inputState = playerIn.getData(INPUT_STATE);
            inputState.getCommands().add(InputCommand.R_CLICK);

            ResourceLocation combo = bladeState.progressCombo(playerIn);

            inputState.getCommands().remove(InputCommand.R_CLICK);

            if (!combo.equals(ComboStateRegistry.NONE.getId())) {
                playerIn.swing(handIn);
            }
            result = true;
        }

        playerIn.startUsingItem(handIn);
        return new InteractionResultHolder<>(result ? InteractionResult.SUCCESS : InteractionResult.FAIL, itemstack);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack itemstack, Player playerIn, Entity entity) {
        SlashBladeState bladeState = getBladeState(itemstack);
        if (bladeState == null || bladeState.onClick()) {
            return false;
        }

        InputState inputState = playerIn.getData(INPUT_STATE);
        inputState.getCommands().add(InputCommand.L_CLICK);
        bladeState.progressCombo(playerIn);
        inputState.getCommands().remove(InputCommand.L_CLICK);

        return true;
    }

    public static final String BREAK_ACTION_TIMEOUT = "BreakActionTimeout";

    @Override
    public void setDamage(ItemStack stack, int damage) {
        int maxDamage = stack.getMaxDamage();
        if (maxDamage < 0) {
            return;
        }
        var state = Objects.requireNonNull(getBladeState(stack), "ItemStack has no active BLADE_STATE component");
        if (state.isBroken()) {
            if (damage <= 0 && !state.isSealed()) {
                state.setBroken(false);
            } else if (maxDamage < damage) {
                damage = Math.min(damage, maxDamage - 1);
            }
        }
        state.setDamage(damage);
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
        if (stack.getMaxDamage() <= 0) {
            return 0;
        }

        if (amount <= 0) {
            return 0;
        }

        var cap = Objects.requireNonNull(getBladeState(stack), "ItemStack has no active BLADE_STATE component");
        boolean current = cap.isBroken();

        if (stack.getDamageValue() + amount >= stack.getMaxDamage()) {
            amount = 0;
            stack.setDamageValue(stack.getMaxDamage() - 1);
            cap.setBroken(!NeoForge.EVENT_BUS.post(new SlashBladeEvent.BreakEvent(stack, cap)).isCanceled());
        }

        if (current != cap.isBroken()) {
            onBroken.accept(stack.getItem());
            if (entity instanceof ServerPlayer player) {
                CriteriaTriggers.CONSUME_ITEM.trigger(player, stack);
            }

            if (entity instanceof Player player) {
                player.awardStat(Stats.ITEM_BROKEN.get(stack.getItem()));
            }
        }

        if (cap.isBroken() && this.isDestructable(stack)) {
            stack.shrink(1);
        }

        return amount;
    }

    public static Consumer<LivingEntity> getOnBroken(ItemStack stack) {
        return (user) -> {
            var state = Objects.requireNonNull(getBladeState(stack), "ItemStack has no active BLADE_STATE component");
            if (stack.isEnchanted()) {
                int count = state.getProudSoulCount() >= SlashBladeConfig.MAX_ENCHANTED_PROUDSOUL_DROP.get() * 100 ?
                        SlashBladeConfig.MAX_ENCHANTED_PROUDSOUL_DROP.get() : Math.max(1, state.getProudSoulCount() / 100);
                List<Holder.Reference<Enchantment>> enchantments = user.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).listElements()
                        .filter(stack::isPrimaryItemFor)
                        .filter(enchantment -> !SlashBladeConfig.NON_DROPPABLE_ENCHANTMENT.get()
                                .contains(enchantment.key().location().toString()))
                        .toList();
                for (int i = 0; i < count; i += 1) {
                    ItemStack enchanted_soul = new ItemStack(SlashBladeItems.PROUDSOUL_TINY.get());
                    Holder<Enchantment> enchant = enchantments.get(user.getRandom().nextInt(0, enchantments.size()));
                    if (enchant != null) {
                        enchanted_soul.enchant(enchant, 1);
                        ItemEntity itemEntity = new ItemEntity(user.level(), user.getX(), user.getY(), user.getZ(),
                                enchanted_soul);
                        itemEntity.setDefaultPickUpDelay();
                        user.level().addFreshEntity(itemEntity);
                    }
                    state.setProudSoulCount(state.getProudSoulCount() - 100);
                }
            }
            ItemStack soul = new ItemStack(SlashBladeItems.PROUDSOUL_TINY.get());

            int count = state.getProudSoulCount() >= SlashBladeConfig.MAX_PROUDSOUL_DROP.get() * 100 ?
                    SlashBladeConfig.MAX_PROUDSOUL_DROP.get() : Math.max(1, state.getProudSoulCount() / 100);

            soul.setCount(count);
            state.setProudSoulCount(state.getProudSoulCount() - (count * 100));

            ItemEntity itementity = new ItemEntity(user.level(), user.getX(), user.getY(), user.getZ(), soul);
            BladeItemEntity e = new BladeItemEntity(SlashBlade.RegistryEvents.BladeItem, user.level()) {
                static final String isReleased = "isReleased";

                @Override
                public boolean causeFallDamage(float distance, float damageMultiplier, @NotNull DamageSource ds) {

                    CompoundTag tag = this.getPersistentData();

                    if (!tag.getBoolean(isReleased)) {
                        this.getPersistentData().putBoolean(isReleased, true);

                        if (this.level() instanceof ServerLevel) {
                            Entity thrower = getOwner();

                            if (thrower != null) {
                                thrower.getPersistentData().remove(BREAK_ACTION_TIMEOUT);
                            }
                        }
                    }

                    return super.causeFallDamage(distance, damageMultiplier, ds);
                }
            };

            e.restoreFrom(itementity);
            e.init();
            e.push(0, 0.4, 0);

            e.setModel(state.getModel().orElse(DefaultResources.resourceDefaultModel));
            e.setTexture(state.getTexture().orElse(DefaultResources.resourceDefaultTexture));

            e.setPickUpDelay(20 * 2);
            e.setGlowingTag(true);

            e.setAirSupply(-1);

            // TODO(neoforge-1.21.1): Restore BladeItemEntity ownership metadata if pickup/credit logic still depends on it.

            user.level().addFreshEntity(e);

            user.getPersistentData().putLong(BREAK_ACTION_TIMEOUT, user.level().getGameTime() + 20 * 5);
        };
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {

        SlashBladeState state = getBladeState(stack);
        if (state != null) {
            ResourceLocation loc = state.resolvCurrentComboState(attacker);
            ComboState cs = ComboStateRegistry.REGISTRY.get(loc) != null
                    ? ComboStateRegistry.REGISTRY.get(loc)
                    : ComboStateRegistry.NONE.get();

            var hitEvent = new SlashBladeEvent.HitEvent(stack, state, target, attacker);
            NeoForge.EVENT_BUS.post(hitEvent);
            if (hitEvent.isCanceled()) {
                return true;
            }

            if (cs != null) {
                cs.hitEffect(target, attacker);
            }
            stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        }

        return true;
    }

    @Override
    public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level worldIn, BlockState state, @NotNull BlockPos pos,
                             @NotNull LivingEntity entityLiving) {

        if (state.getDestroySpeed(worldIn, pos) != 0.0F && getBladeState(stack) != null) {
            stack.hurtAndBreak(1, entityLiving, EquipmentSlot.MAINHAND);
        }

        return true;
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, Level worldIn, @NotNull LivingEntity entityLiving, int timeLeft) {
        int elapsed = this.getUseDuration(stack, entityLiving) - timeLeft;

        if (!worldIn.isClientSide()) {
            SlashBladeState state = getBladeState(stack);
            if (state != null) {
                var swordType = SwordType.from(stack);
                if (!state.isBroken() && !state.isSealed() && swordType.contains(SwordType.ENCHANTED)) {
                    ResourceLocation sa = state.doChargeAction(entityLiving, elapsed);
                    boolean isCreative = false;
                    if (!sa.equals(ComboStateRegistry.NONE.getId())) {
                        if (entityLiving instanceof Player player) {
                            isCreative = player.getAbilities().instabuild;
                        }
                        if (!isCreative) {
                            var cost = state.getSlashArts().getProudSoulCost();
                            if (state.getProudSoulCount() >= cost) {
                                state.setProudSoulCount(state.getProudSoulCount() - cost);
                            } else {
                                stack.hurtAndBreak(1, entityLiving, EquipmentSlot.MAINHAND);
                            }
                        }
                        entityLiving.swing(InteractionHand.MAIN_HAND);
                    }
                }
            }
        }
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity player, ItemStack stack, int count) {

        SlashBladeState bladeState = getBladeState(stack);
        if (bladeState != null) {
            (ComboStateRegistry.REGISTRY.get(bladeState.getComboSeq()) != null
                    ? ComboStateRegistry.REGISTRY.get(bladeState.getComboSeq())
                    : ComboStateRegistry.NONE.get()).holdAction(player);
            var swordType = SwordType.from(stack);
            if (!bladeState.isBroken() && !bladeState.isSealed() && swordType.contains(SwordType.ENCHANTED)) {
                if (!player.level().isClientSide()) {
                    int ticks = player.getTicksUsingItem();
                    int fullChargeTicks = bladeState.getFullChargeTicks(player);
                    if (0 < ticks && ticks == fullChargeTicks) {
                        Vec3 pos = player.getEyePosition(1.0f).add(player.getLookAngle());
                        ((ServerLevel) player.level()).sendParticles(ParticleTypes.PORTAL, pos.x, pos.y, pos.z, 7, 0.7,
                                0.7, 0.7, 0.02);
                    }
                }
            }
        }
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level worldIn, @NotNull Entity entityIn, int itemSlot, boolean isSelected) {
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);

        SlashBladeState state = getBladeState(stack);
        if (state != null) {
            if (NeoForge.EVENT_BUS
                    .post(new SlashBladeEvent.UpdateEvent(stack, state, worldIn, entityIn, itemSlot, isSelected)).isCanceled()) {
                return;
            }

            if (!isSelected) {
                var swordType = SwordType.from(stack);
                if (entityIn instanceof Player player) {
                    if (!SlashBladeConfig.SELF_REPAIR_ENABLE.get()) {
                        return;
                    }
                    boolean hasHunger = player.hasEffect(MobEffects.HUNGER) && SlashBladeConfig.HUNGER_CAN_REPAIR.get();
                    if (swordType.contains(SwordType.BEWITCHED) || hasHunger) {
                        if (stack.getDamageValue() > 0 && player.getFoodData().getFoodLevel() > 0) {
                            int hungerAmplifier = hasHunger ? player.getEffect(MobEffects.HUNGER).getAmplifier() : 0;
                            int level = 1 + hungerAmplifier;
                            Boolean expCostFlag = SlashBladeConfig.SELF_REPAIR_COST_EXP.get();
                            int expCost = SlashBladeConfig.BEWITCHED_EXP_COST.get() * level;

                            if (expCostFlag && player.experienceLevel < expCost) {
                                return;
                            }

                            player.giveExperiencePoints(expCostFlag ? -expCost : 0);
                            player.causeFoodExhaustion(
                                    SlashBladeConfig.BEWITCHED_HUNGER_EXHAUSTION.get().floatValue() * level);
                            stack.setDamageValue(stack.getDamageValue() - level);
                        }
                    }
                }
            }
            if (entityIn instanceof LivingEntity living) {
                living.getData(INPUT_STATE).getScheduler().onTick(living);

                ResourceLocation loc = state.resolvCurrentComboState(living);
                ComboState cs = ComboStateRegistry.REGISTRY.get(loc) != null
                        ? ComboStateRegistry.REGISTRY.get(loc)
                        : ComboStateRegistry.NONE.get();

                if (isInMainhand(stack, isSelected, living)) {
                    if (cs != null) {
                        cs.tickAction(living);
                    } else if (!loc.equals(state.getComboRoot())) {
                        state.setComboSeq(state.getComboRoot());
                    }
                }
            }
        }
    }

    public static boolean isInMainhand(ItemStack stack, boolean isSelected, LivingEntity living) {
        return isSelected && ItemStack.isSameItemSameComponents(stack, living.getMainHandItem());
    }

    // damage ----------------------------------------------------------
    // Note: getShareTag/readShareTag removed — blade state is now a DataComponent
    //       and is automatically synced by NeoForge's component sync system.

    @Override
    public int getDamage(ItemStack stack) {
        SlashBladeState s = getBladeState(stack);
        return (s != null && !s.isEmpty()) ? s.getDamage() : 0;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        SlashBladeState s = getBladeState(stack);
        return (s != null && !s.isEmpty()) ? s.getMaxDamage() : this.getTier().getUses();
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public @NotNull String getDescriptionId(ItemStack stack) {
        SlashBladeState s = getBladeState(stack);
        if (s != null && !s.getTranslationKey().isBlank()) {
            return s.getTranslationKey();
        }
        return super.getDescriptionId(stack);
    }

    public ResourceLocation getBladeId(ItemStack stack) {
        SlashBladeState s = getBladeState(stack);
        if (s != null && !s.getTranslationKey().isBlank()) {
            return parseBladeID(s.getTranslationKey());
        }
        return BuiltInRegistries.ITEM.getKey(this);
    }

    public static ResourceLocation parseBladeID(String key) {
        return ResourceLocation.tryParse(key.substring(5).replaceFirst("\\.", ":"));
    }

    public boolean isDestructable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack toRepair, @NotNull ItemStack repair) {

        if (Ingredient.of(ItemTags.STONE_TOOL_MATERIALS).test(repair)) {
            return true;
        }

        /*
         * Tag<Item> tags = ItemTags.getCollection().get(new
         * ResourceLocation("slashblade","proudsouls"));
         *
         * if(tags != null){ boolean result = Ingredient.fromTag(tags).test(repair); }
         */

        // todo: repair custom material
        if (repair.is(SlashBladeItemTags.PROUD_SOULS)) {
            return true;
        }
        return super.isValidRepairItem(toRepair, repair);
    }

    RangeMap<Comparable<?>, Object> refineColor = ImmutableRangeMap.builder()
            .put(Range.lessThan(10), ChatFormatting.GRAY).put(Range.closedOpen(10, 50), ChatFormatting.YELLOW)
            .put(Range.closedOpen(50, 100), ChatFormatting.GREEN).put(Range.closedOpen(100, 150), ChatFormatting.AQUA)
            .put(Range.closedOpen(150, 200), ChatFormatting.BLUE).put(Range.atLeast(200), ChatFormatting.LIGHT_PURPLE)
            .build();

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        SlashBladeState s = getBladeState(stack);
        if (s != null) {
            this.appendSwordType(stack, context, tooltip, flagIn);
            this.appendProudSoulCount(tooltip, stack);
            this.appendKillCount(tooltip, stack);
            this.appendSlashArt(stack, tooltip, s);
            this.appendRefineCount(tooltip, stack);
            this.appendSpecialEffects(tooltip, s);
        }
        super.appendHoverText(stack, context, tooltip, flagIn);
    }

    @OnlyIn(Dist.CLIENT)
    public void appendSlashArt(ItemStack stack, List<Component> tooltip, @NotNull ISlashBladeState s) {
        var swordType = SwordType.from(stack);
        if (swordType.contains(SwordType.BEWITCHED) && !swordType.contains(SwordType.SEALED)) {
            tooltip.add(Component.translatable("slashblade.tooltip.slash_art", s.getSlashArts().getDescription())
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void appendRefineCount(List<Component> tooltip, @NotNull ItemStack stack) {
        SlashBladeState s = getBladeState(stack);
        if (s == null) return;
        int refine = s.getRefine();
        if (refine > 0) {
            tooltip.add(Component.translatable("slashblade.tooltip.refine", refine)
                    .withStyle((ChatFormatting) refineColor.get(refine)));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void appendProudSoulCount(List<Component> tooltip, @NotNull ItemStack stack) {
        SlashBladeState s = getBladeState(stack);
        if (s == null) return;
        int proudsoul = s.getProudSoulCount();
        if (proudsoul > 0) {
            MutableComponent countComponent = Component.translatable("slashblade.tooltip.proud_soul", proudsoul)
                    .withStyle(ChatFormatting.GRAY);
            if (proudsoul > 10000) {
                countComponent = countComponent.withStyle(ChatFormatting.DARK_PURPLE);
            }
            tooltip.add(countComponent);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void appendKillCount(List<Component> tooltip, @NotNull ItemStack stack) {
        SlashBladeState s = getBladeState(stack);
        if (s == null) return;
        int killCount = s.getKillCount();
        if (killCount > 0) {
            MutableComponent killCountComponent = Component.translatable("slashblade.tooltip.killcount", killCount)
                    .withStyle(ChatFormatting.GRAY);
            if (killCount > 1000) {
                killCountComponent = killCountComponent.withStyle(ChatFormatting.DARK_PURPLE);
            }
            tooltip.add(killCountComponent);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void appendSpecialEffects(List<Component> tooltip, @NotNull ISlashBladeState s) {
        if (s.getSpecialEffects().isEmpty()) {
            return;
        }

        Minecraft mcinstance = Minecraft.getInstance();
        Player player = mcinstance.player;

        s.getSpecialEffects().forEach(se -> {

            boolean showingLevel = SpecialEffect.getRequestLevel(se) > 0;

            if (player != null) {
                tooltip.add(Component.translatable("slashblade.tooltip.special_effect", SpecialEffect.getDescription(se),
                                Component.literal(showingLevel ? String.valueOf(SpecialEffect.getRequestLevel(se)) : "")
                                        .withStyle(SpecialEffect.isEffective(se, player.experienceLevel) ? ChatFormatting.RED
                                                : ChatFormatting.DARK_GRAY))
                        .withStyle(ChatFormatting.GRAY));
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void appendSwordType(ItemStack stack, @NotNull Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        var swordType = SwordType.from(stack);
        boolean goldenFlag = swordType.containsAll(List.of(SwordType.SOULEATER, SwordType.FIERCEREDGE));
        if (swordType.contains(SwordType.SEALED)) {
            return;
        }
        if (swordType.contains(SwordType.BEWITCHED)) {
            tooltip.add(
                    Component.translatable("slashblade.sword_type.bewitched")
                            .withStyle(goldenFlag ? ChatFormatting.GOLD : ChatFormatting.DARK_PURPLE));
        } else if (swordType.contains(SwordType.ENCHANTED)) {
            tooltip.add(Component.translatable("slashblade.sword_type.enchanted").withStyle(ChatFormatting.DARK_AQUA));
        } else {
            tooltip.add(Component.translatable("slashblade.sword_type.noname").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    // initCapabilities() removed — BladeState is now a DataComponent (ModDataComponents.BLADE_STATE).
    // NeoForge 1.21.1: set default component value via Item.Properties.component() at registration time.

    @SuppressWarnings({"removal", "deprecation"})
    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        SlashBladeState state = getBladeState(stack);
        if (state == null) {
            return true;
        }
        return state.getLastActionTime() != entity.level().getGameTime();
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    /**
     * 原来的方法替换掉落实体时无法Copy假物品实体相关的NBT，因为获取物品指令是先生成的物品实体再设置的假物品
     */
    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!(entity instanceof BladeItemEntity)) {
            Level world = entity.level();
            BladeItemEntity e = new BladeItemEntity(SlashBlade.RegistryEvents.BladeItem, world);
            e.restoreFrom(entity);
            e.init();
            entity.discard();
            world.addFreshEntity(e);
        }
        return false;
    }

    @Override
    public int getEntityLifespan(ItemStack itemStack, Level world) {
        return super.getEntityLifespan(itemStack, world);// Short.MAX_VALUE;
    }

    /*@Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {

        consumer.accept(new IClientItemExtensions() {
            final BlockEntityWithoutLevelRenderer renderer = new SlashBladeTEISR(
                    Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                    Minecraft.getInstance().getEntityModels());

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        });

        super.initializeClient(consumer);
    }*/
    @SuppressWarnings({"removal", "deprecation"})
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final BlockEntityWithoutLevelRenderer renderer = new SlashBladeTEISR(
                    Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                    Minecraft.getInstance().getEntityModels()
            );

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        });
    }
}

package mods.flammpfeil.slashblade;
import com.google.common.base.CaseFormat;
import mods.flammpfeil.slashblade.ability.*;
import mods.flammpfeil.slashblade.client.renderer.layers.LayerMainBlade;
import mods.flammpfeil.slashblade.data.DataGen;
import mods.flammpfeil.slashblade.event.client.SlashBladeLayerEvent;
import mods.flammpfeil.slashblade.init.ModAttachments;
import mods.flammpfeil.slashblade.init.ModDataComponents;
import mods.flammpfeil.slashblade.init.ModIngredientTypes;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.entity.*;
import mods.flammpfeil.slashblade.event.BladeMotionEventBroadcaster;
import mods.flammpfeil.slashblade.event.handler.*;
import mods.flammpfeil.slashblade.network.NetworkManager;
import mods.flammpfeil.slashblade.recipe.RecipeSerializerRegistry;
import mods.flammpfeil.slashblade.registry.*;
import mods.flammpfeil.slashblade.registry.combo.ComboCommands;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import mods.flammpfeil.slashblade.util.TargetSelector;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SlashBlade.MODID)
public class SlashBlade {
    public static final String MODID = "slashblade";

    public static ResourceLocation prefix(String path) {
        return ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID, path);
    }

    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    private void clientSetup(final FMLClientSetupEvent event) {
        BlockPickCanceller.register(NeoForge.EVENT_BUS);
    }


    public SlashBlade(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, SlashBladeConfig.COMMON_CONFIG);

        modBus.addListener(this::setup);
        modBus.addListener(this::clientSetup);

        modBus.addListener(RegistryEvents::register);
        modBus.addListener(RegistryEvents::onEntityAttributeModificationEvent);
        modBus.addListener(RegistryHandler::onDatapackRegister);

        ModAttributes.ATTRIBUTES.register(modBus);
        ModDataComponents.DATA_COMPONENTS.register(modBus);
        ModAttachments.ATTACHMENT_TYPES.register(modBus);
        ModIngredientTypes.INGREDIENT_TYPES.register(modBus);
        modBus.addListener(NetworkManager::register);

        SlashBladeItems.ITEMS.register(modBus);
        ComboStateRegistry.COMBO_STATE.register(modBus);
        SlashArtsRegistry.SLASH_ARTS.register(modBus);
        SlashBladeCreativeGroup.CREATIVE_MODE_TABS.register(modBus);
        RecipeSerializerRegistry.RECIPE_TYPES.register(modBus);
        RecipeSerializerRegistry.RECIPE_SERIALIZER.register(modBus);
        SpecialEffectsRegistry.SPECIAL_EFFECT.register(modBus);
        modBus.addListener(DataGen::dataGen);

        modBus.addListener(SlashBladeLayerEvent::onAddLayers);
        NeoForge.EVENT_BUS.addListener(SlashBladeLayerEvent.getInstance()::onRenderHand);
    }

    private void setup(final FMLCommonSetupEvent event) {

        NeoForge.EVENT_BUS.addListener(KnockBackHandler::onLivingKnockBack);

        FallHandler.getInstance().register();
        LockOnManager.getInstance().register();
        Guard.getInstance().register();

        NeoForge.EVENT_BUS.register(new StunManager());

        RefineHandler.getInstance().register();
        KillCounter.getInstance().register();
        RankPointHandler.getInstance().register();
        AllowFlightOverrwrite.getInstance().register();
//        BlockPickCanceller.register(NeoForge.EVENT_BUS);
        BladeMotionEventBroadcaster.getInstance().register();

        NeoForge.EVENT_BUS.addListener(TargetSelector::onInputChange);
        SummonedSwordArts.getInstance().register();
        SlayerStyleArts.getInstance().register();
        Untouchable.getInstance().register();
        EnemyStep.getInstance().register();
        KickJump.getInstance().register();
        SuperSlashArts.getInstance().register();

        ComboCommands.initDefaultStandByCommands();
    }

    // You can use EventBusSubscriber to automatically subscribe events on the
    // contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    public static class RegistryEvents {

        public static final ResourceLocation BladeItemEntityLoc = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
                classToString(BladeItemEntity.class));
        public static EntityType<BladeItemEntity> BladeItem;

        public static final ResourceLocation BladeStandEntityLoc = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
                classToString(BladeStandEntity.class));
        public static EntityType<BladeStandEntity> BladeStand;

        public static final ResourceLocation SummonedSwordLoc = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
                classToString(EntityAbstractSummonedSword.class));
        public static EntityType<EntityAbstractSummonedSword> SummonedSword;
        public static final ResourceLocation SpiralSwordsLoc = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
                classToString(EntitySpiralSwords.class));
        public static EntityType<EntitySpiralSwords> SpiralSwords;

        public static final ResourceLocation StormSwordsLoc = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
                classToString(EntityStormSwords.class));
        public static EntityType<EntityStormSwords> StormSwords;
        public static final ResourceLocation BlisteringSwordsLoc = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
                classToString(EntityBlisteringSwords.class));
        public static EntityType<EntityBlisteringSwords> BlisteringSwords;
        public static final ResourceLocation HeavyRainSwordsLoc = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
                classToString(EntityHeavyRainSwords.class));
        public static EntityType<EntityHeavyRainSwords> HeavyRainSwords;

        public static final ResourceLocation JudgementCutLoc = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
                classToString(EntityJudgementCut.class));
        public static EntityType<EntityJudgementCut> JudgementCut;

        public static final ResourceLocation SlashEffectLoc = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
                classToString(EntitySlashEffect.class));
        public static EntityType<EntitySlashEffect> SlashEffect;

        public static final ResourceLocation DriveLoc = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID,
                classToString(EntityDrive.class));
        public static EntityType<EntityDrive> Drive;


        public static void register(RegisterEvent event) {
//            event.register(ForgeRegistries.Keys.ITEMS, helper -> {
//
//                helper.register(new ResourceLocation(MODID, "slashblade_wood"),
//                        (new ItemSlashBladeDetune(new ItemTierSlashBlade(60, 2F), 2, 0.0F,
//                                (new Item.Properties()))).setDestructable()
//                                .setTexture(SlashBlade.prefix("model/wood.png")));
//
//                helper.register(new ResourceLocation(MODID, "slashblade_bamboo"),
//                        (new ItemSlashBladeDetune(new ItemTierSlashBlade(70, 3F), 3, 0.0F,
//                                (new Item.Properties()))).setDestructable()
//                                .setTexture(SlashBlade.prefix("model/bamboo.png")));
//
//                helper.register(new ResourceLocation(MODID, "slashblade_silverbamboo"),
//                        (new ItemSlashBladeDetune(new ItemTierSlashBlade(40, 3F), 3, 0.0F,
//                                (new Item.Properties()))).setTexture(SlashBlade.prefix("model/silverbamboo.png")));
//
//                helper.register(new ResourceLocation(MODID, "slashblade_white"),
//                        (new ItemSlashBladeDetune(new ItemTierSlashBlade(70, 4F), 4, 0.0F,
//                                (new Item.Properties()))).setTexture(SlashBlade.prefix("model/white.png")));
//
//                helper.register(new ResourceLocation(MODID, "slashblade"),
//                        new ItemSlashBlade(new ItemTierSlashBlade(40, 4F), 4, 0.0F, (new Item.Properties())));
//
//                helper.register(new ResourceLocation(MODID, "proudsoul"), new ItemProudSoul((new Item.Properties())) {
//
//                    @Override
//                    public int getEnchantmentValue(ItemStack stack) {
//                        return 50;
//                    }
//
//                });
//
//                helper.register(new ResourceLocation(MODID, "proudsoul_ingot"), new ItemProudSoul((new Item.Properties())) {
//
//                    @Override
//                    public int getEnchantmentValue(ItemStack stack) {
//                        return 100;
//                    }
//                });
//
//                helper.register(new ResourceLocation(MODID, "proudsoul_tiny"), new ItemProudSoul((new Item.Properties())) {
//
//                    @Override
//                    public int getEnchantmentValue(ItemStack stack) {
//                        return 10;
//                    }
//                });
//
//                helper.register(new ResourceLocation(MODID, "proudsoul_sphere"),
//                        new ItemProudSoul((new Item.Properties()).rarity(Rarity.UNCOMMON)) {
//
//                            @Override
//                            public int getEnchantmentValue(ItemStack stack) {
//                                return 150;
//                            }
//
//                            @Override
//                            public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> components, @NotNull TooltipFlag flag) {
//                                if (stack.getTag() != null) {
//                                    CompoundTag tag = stack.getTag();
//                                    if (tag.contains("SpecialAttackType")) {
//                                        ResourceLocation SA = new ResourceLocation(tag.getString("SpecialAttackType"));
//                                        if (SlashArtsRegistry.REGISTRY.containsKey(SA) && !Objects.equals(SlashArtsRegistry.REGISTRY.getValue(SA), SlashArtsRegistry.NONE.get())) {
//                                            components.add(Component.translatable("slashblade.tooltip.slash_art", Objects.requireNonNull(SlashArtsRegistry.REGISTRY.getValue(SA)).getDescription()).withStyle(ChatFormatting.GRAY));
//                                        }
//                                    }
//                                }
//                                super.appendHoverText(stack, level, components, flag);
//                            }
//                        });
//
//                helper.register(new ResourceLocation(MODID, "proudsoul_crystal"),
//                        new ItemProudSoul((new Item.Properties()).rarity(Rarity.RARE)) {
//
//                            @Override
//                            public int getEnchantmentValue(ItemStack stack) {
//                                return 200;
//                            }
//
//                            @Override
//                            @OnlyIn(Dist.CLIENT)
//                            public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> components, @NotNull TooltipFlag flag) {
//                                if (stack.getTag() != null) {
//                                    CompoundTag tag = stack.getTag();
//                                    if (tag.contains("SpecialEffectType")) {
//                                        Minecraft mcinstance = Minecraft.getInstance();
//                                        Player player = mcinstance.player;
//                                        ResourceLocation se = new ResourceLocation(tag.getString("SpecialEffectType"));
//                                        if (SpecialEffectsRegistry.REGISTRY.containsKey(se)) {
//                                            if (player != null) {
//                                                components.add(Component.translatable("slashblade.tooltip.special_effect", SpecialEffect.getDescription(se),
//                                                                Component.literal(String.valueOf(SpecialEffect.getRequestLevel(se)))
//                                                                        .withStyle(SpecialEffect.isEffective(se, player.experienceLevel) ? ChatFormatting.RED
//                                                                                : ChatFormatting.DARK_GRAY))
//                                                        .withStyle(ChatFormatting.GRAY));
//                                            }
//                                        }
//                                    }
//                                }
//                                super.appendHoverText(stack, level, components, flag);
//                            }
//                        });
//
//                helper.register(new ResourceLocation(MODID, "proudsoul_trapezohedron"),
//                        new ItemProudSoul((new Item.Properties()).rarity(Rarity.EPIC)) {
//
//                            @Override
//                            public int getEnchantmentValue(ItemStack stack) {
//                                return TRAPEZOHEDRON_MAX_REFINE.get();
//                            }
//                        });
//
//                helper.register(new ResourceLocation(MODID, "bladestand_1"),
//                        new BladeStandItem((new Item.Properties()).rarity(Rarity.COMMON)));
//                helper.register(new ResourceLocation(MODID, "bladestand_2"),
//                        new BladeStandItem((new Item.Properties()).rarity(Rarity.COMMON)));
//                helper.register(new ResourceLocation(MODID, "bladestand_v"),
//                        new BladeStandItem((new Item.Properties()).rarity(Rarity.COMMON)));
//                helper.register(new ResourceLocation(MODID, "bladestand_s"),
//                        new BladeStandItem((new Item.Properties()).rarity(Rarity.COMMON)));
//                helper.register(new ResourceLocation(MODID, "bladestand_1w"),
//                        new BladeStandItem((new Item.Properties()).rarity(Rarity.COMMON), true));
//                helper.register(new ResourceLocation(MODID, "bladestand_2w"),
//                        new BladeStandItem((new Item.Properties()).rarity(Rarity.COMMON), true));
//            });

            event.register(Registries.ENTITY_TYPE, helper -> {
                {
                    EntityType<EntityAbstractSummonedSword> entity = SummonedSword = EntityType.Builder
                            .of(EntityAbstractSummonedSword::new, MobCategory.MISC).sized(0.5F, 0.5F)
                            .setTrackingRange(4).setUpdateInterval(20)
                            .build(SummonedSwordLoc.toString());
                    helper.register(SummonedSwordLoc, entity);
                }

                {
                    EntityType<EntityStormSwords> entity = StormSwords = EntityType.Builder
                            .of(EntityStormSwords::new, MobCategory.MISC).sized(0.5F, 0.5F).setTrackingRange(4)
                            .build(StormSwordsLoc.toString());
                    helper.register(StormSwordsLoc, entity);
                }

                {
                    EntityType<EntitySpiralSwords> entity = SpiralSwords = EntityType.Builder
                            .of(EntitySpiralSwords::new, MobCategory.MISC).sized(0.5F, 0.5F).setTrackingRange(4)
                            .build(SpiralSwordsLoc.toString());
                    helper.register(SpiralSwordsLoc, entity);
                }

                {
                    EntityType<EntityBlisteringSwords> entity = BlisteringSwords = EntityType.Builder
                            .of(EntityBlisteringSwords::new, MobCategory.MISC).sized(0.5F, 0.5F).setTrackingRange(4)
                            .build(BlisteringSwordsLoc.toString());
                    helper.register(BlisteringSwordsLoc, entity);
                }

                {
                    EntityType<EntityHeavyRainSwords> entity = HeavyRainSwords = EntityType.Builder
                            .of(EntityHeavyRainSwords::new, MobCategory.MISC).sized(0.5F, 0.5F).setTrackingRange(4)
                            .build(HeavyRainSwordsLoc.toString());
                    helper.register(HeavyRainSwordsLoc, entity);
                }

                {
                    EntityType<EntityJudgementCut> entity = JudgementCut = EntityType.Builder
                            .of(EntityJudgementCut::new, MobCategory.MISC).sized(2.5F, 2.5F).setTrackingRange(4)
                            .build(JudgementCutLoc.toString());
                    helper.register(JudgementCutLoc, entity);
                }

                {
                    EntityType<BladeItemEntity> entity = BladeItem = EntityType.Builder
                            .of(BladeItemEntity::new, MobCategory.MISC).sized(0.25F, 0.25F).setTrackingRange(4)
                            .build(BladeItemEntityLoc.toString());
                    helper.register(BladeItemEntityLoc, entity);
                }

                {
                    EntityType<BladeStandEntity> entity = BladeStand = EntityType.Builder
                            .of(BladeStandEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).setTrackingRange(10)
                            .setUpdateInterval(20).setShouldReceiveVelocityUpdates(false)
                            .build(BladeStandEntityLoc.toString());
                    helper.register(BladeStandEntityLoc, entity);
                }

                {
                    EntityType<EntitySlashEffect> entity = SlashEffect = EntityType.Builder
                            .of(EntitySlashEffect::new, MobCategory.MISC).sized(3.0F, 3.0F).setTrackingRange(4)
                            .build(SlashEffectLoc.toString());
                    helper.register(SlashEffectLoc, entity);
                }

                {
                    EntityType<EntityDrive> entity = Drive = EntityType.Builder.of(EntityDrive::new, MobCategory.MISC)
                            .sized(3.0F, 3.0F).setTrackingRange(4).setUpdateInterval(20)
                            .build(DriveLoc.toString());
                    helper.register(DriveLoc, entity);
                }

            });

            event.register(Registries.STAT_TYPE, helper -> SWORD_SUMMONED = registerCustomStat("sword_summoned"));

        }

        private static String classToString(Class<? extends Entity> entityClass) {
            return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entityClass.getSimpleName())
                    .replace("entity_", "");
        }

        public static void onEntityAttributeModificationEvent(final EntityAttributeModificationEvent event) {
            event.add(EntityType.PLAYER, ModAttributes.SLASHBLADE_DAMAGE);
        }

        public static ResourceLocation SWORD_SUMMONED;

        private static ResourceLocation registerCustomStat(String name) {
            ResourceLocation resourcelocation = SlashBlade.prefix(name);
            Registry.register(BuiltInRegistries.CUSTOM_STAT, name, resourcelocation);
            Stats.CUSTOM.get(resourcelocation, StatFormatter.DEFAULT);
            return resourcelocation;
        }

        /*
          /scoreboard objectives add stat minecraft.custom:slashblade.sword_summoned
          /scoreboard objectives setdisplay sidebar stat
         */
    }

    public static Registry<SlashBladeDefinition> getSlashBladeDefinitionRegistry(Level level) {
        if (level.isClientSide()) {
            return BladeModelManager.getClientSlashBladeRegistry();
        }
        return level.registryAccess().registryOrThrow(SlashBladeDefinition.REGISTRY_KEY);
    }

    public static HolderLookup.RegistryLookup<SlashBladeDefinition> getSlashBladeDefinitionRegistry(HolderLookup.Provider access) {
        return access.lookupOrThrow(SlashBladeDefinition.REGISTRY_KEY);
    }
}

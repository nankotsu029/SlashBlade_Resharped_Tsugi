package mods.flammpfeil.slashblade.client;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.client.renderer.LockonCircleRender;
import mods.flammpfeil.slashblade.client.renderer.gui.RankRenderer;
import mods.flammpfeil.slashblade.client.renderer.layers.LayerMainBlade;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModel;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.BladeMotionManager;
import mods.flammpfeil.slashblade.compat.playerAnim.PlayerAnimationOverrider;
import mods.flammpfeil.slashblade.event.client.AdvancementsRecipeRenderer;
import mods.flammpfeil.slashblade.event.client.LayerEvent;
import mods.flammpfeil.slashblade.event.client.SneakingMotionCanceller;
import mods.flammpfeil.slashblade.event.client.UserPoseOverrider;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.logging.log4j.util.LoaderUtil;

import java.util.Objects;

public class ClientHandler {
    private static final ResourceLocation USER_PROPERTY = SlashBlade.prefix("user");

    public static void doClientStuff(final FMLClientSetupEvent event) {
        SneakingMotionCanceller.getInstance().register();
        LayerEvent.getInstance().register();

        if (LoaderUtil.isClassAvailable("dev.kosmx.playerAnim.api.layered.AnimationStack")) {
            PlayerAnimationOverrider.getInstance().register();
        } else {
            UserPoseOverrider.getInstance().register();
        }
        LockonCircleRender.getInstance().register();
        AdvancementsRecipeRenderer.getInstance().register();


        RankRenderer.getInstance().register();

        ItemProperties.register(SlashBladeItems.SLASHBLADE.get(), USER_PROPERTY,
                (ClampedItemPropertyFunction) (p_174564_, p_174565_, p_174566_, p_174567_) -> {
                    BladeModel.user = p_174566_;
                    return 0;
                });

        ItemProperties.register(SlashBladeItems.SLASHBLADE_BAMBOO.get(), USER_PROPERTY,
                (ClampedItemPropertyFunction) (p_174564_, p_174565_, p_174566_, p_174567_) -> {
                    BladeModel.user = p_174566_;
                    return 0;
                });

        ItemProperties.register(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get(), USER_PROPERTY,
                (ClampedItemPropertyFunction) (p_174564_, p_174565_, p_174566_, p_174567_) -> {
                    BladeModel.user = p_174566_;
                    return 0;
                });

        ItemProperties.register(SlashBladeItems.SLASHBLADE_WHITE.get(), USER_PROPERTY,
                (ClampedItemPropertyFunction) (p_174564_, p_174565_, p_174566_, p_174567_) -> {
                    BladeModel.user = p_174566_;
                    return 0;
                });

        ItemProperties.register(SlashBladeItems.SLASHBLADE_WOOD.get(), USER_PROPERTY,
                (ClampedItemPropertyFunction) (p_174564_, p_174565_, p_174566_, p_174567_) -> {
                    BladeModel.user = p_174566_;
                    return 0;
                });

    }

    public static void onTextureAtlasStitched(TextureAtlasStitchedEvent event) {
        BladeMotionManager.getInstance().reload(event);
    }

    public static void onCreativeTagBuilding(BuildCreativeModeTabContentsEvent event) {
        HolderLookup.Provider registries = event.getParameters().holders();
        SlashBlade.getSlashBladeDefinitionRegistry(registries)
                .listElements()
                .sorted(SlashBladeDefinition.COMPARATOR).forEach(entry -> {
                    if (!event.getTabKey().location().equals(entry.value().getCreativeGroup())) {
                        return;
                    }
                    ItemStack blade = entry.value().getBlade(entry.value().getItem(), registries);
                    if (!blade.isEmpty()) {
                        event.accept(blade);
                    }
                });
    }

    public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        event.register(SlashBladeKeyMappings.KEY_SPECIAL_MOVE);
        event.register(SlashBladeKeyMappings.KEY_SUMMON_BLADE);
    }

    public static void bakeModels(final ModelEvent.ModifyBakingResult event) {
        bakeBlade(SlashBladeItems.SLASHBLADE.get(), event);
        bakeBlade(SlashBladeItems.SLASHBLADE_WHITE.get(), event);
        bakeBlade(SlashBladeItems.SLASHBLADE_WOOD.get(), event);
        bakeBlade(SlashBladeItems.SLASHBLADE_SILVERBAMBOO.get(), event);
        bakeBlade(SlashBladeItems.SLASHBLADE_BAMBOO.get(), event);
    }

    public static void bakeBlade(Item blade, final ModelEvent.ModifyBakingResult event) {
        ModelResourceLocation loc = new ModelResourceLocation(Objects.requireNonNull(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(blade)), "inventory");
        BladeModel model = new BladeModel(event.getModels().get(loc), event.getModelBakery());
        event.getModels().put(loc, model);
    }

    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            addPlayerLayer(event, skin);
        }

        for (EntityType<?> type : event.getEntityTypes()) {
            addEntityLayer(event.getRenderer(type));
        }

//        addEntityLayer(event, EntityType.ZOMBIE);
//        addEntityLayer(event, EntityType.HUSK);
//        addEntityLayer(event, EntityType.ZOMBIE_VILLAGER);
//
//        addEntityLayer(event, EntityType.WITHER_SKELETON);
//        addEntityLayer(event, EntityType.SKELETON);
//        addEntityLayer(event, EntityType.STRAY);
//
//        addEntityLayer(event, EntityType.PIGLIN);
//        addEntityLayer(event, EntityType.PIGLIN_BRUTE);
//        addEntityLayer(event, EntityType.ZOMBIFIED_PIGLIN);
    }

    @SuppressWarnings({"unchecked"})
    public static void addPlayerLayer(EntityRenderersEvent.AddLayers evt, PlayerSkin.Model skin) {
        EntityRenderer<? extends Player> renderer = evt.getSkin(skin);

        if (renderer instanceof LivingEntityRenderer livingRenderer) {
            livingRenderer.addLayer(new LayerMainBlade<>(livingRenderer));
        }
    }

    @SuppressWarnings({"unchecked"})
    private static void addEntityLayer(EntityRenderer<?> renderer) {
        if (renderer instanceof LivingEntityRenderer livingRenderer) {
            livingRenderer.addLayer(new LayerMainBlade<>(livingRenderer));
        }
    }


}

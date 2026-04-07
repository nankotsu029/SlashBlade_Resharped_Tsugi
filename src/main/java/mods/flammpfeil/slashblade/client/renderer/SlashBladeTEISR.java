package mods.flammpfeil.slashblade.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mods.flammpfeil.slashblade.client.renderer.model.BladeFirstPersonRender;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModel;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.entity.BladeStandEntity;
import mods.flammpfeil.slashblade.init.DefaultResources;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBladeDetune;
import mods.flammpfeil.slashblade.item.SwordType;
import mods.flammpfeil.slashblade.registry.SlashBladeItems;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlashBladeTEISR extends BlockEntityWithoutLevelRenderer {

    public SlashBladeTEISR(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, @NotNull ItemDisplayContext transformType,
                             @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer,
                             int light, int overlay) {
        if (!(stack.getItem() instanceof ItemSlashBlade)) {
            return;
        }

        renderBlade(stack, transformType, poseStack, buffer, light, overlay);
    }

    private static boolean isPlayerContext(ItemDisplayContext ctx) {
        return ctx == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || ctx == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || ctx == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || ctx == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                || ctx == ItemDisplayContext.NONE;
    }

    private static boolean isMainHandFirstPersonContext(LocalPlayer player, ItemDisplayContext ctx) {
        if (player == null) return false;
        return player.getMainArm() == HumanoidArm.RIGHT
                ? ctx == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                : ctx == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
    }

    public boolean renderBlade(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack,
                               MultiBufferSource buffer, int light, int overlay) {

        // 手持ち中のプレイヤー追従描画はここで打ち切る
        if (isPlayerContext(transformType)) {
            LocalPlayer player = Minecraft.getInstance().player;
            BladeModel.user = player;

            // 旧コードと同じ意味:
            // 利き手側の first person context のときだけ first-person layer を描画する
            if (isMainHandFirstPersonContext(player, transformType)) {
                try (MSAutoCloser ignored = MSAutoCloser.pushMatrix(poseStack)) {
                    BladeFirstPersonRender.getInstance()
                            .render(stack, transformType, poseStack, buffer, light);
                }
            }

            return false;
        }

        try (MSAutoCloser ignored = MSAutoCloser.pushMatrix(poseStack)) {
            poseStack.translate(0.5f, 0.5f, 0.5f);

            if (transformType == ItemDisplayContext.GROUND) {
                poseStack.translate(0, 0.15f, 0);
                renderIcon(stack, poseStack, buffer, light, 0.005f);
            } else if (transformType == ItemDisplayContext.GUI) {
                renderIcon(stack, poseStack, buffer, light, 0.008f, true);
            } else if (transformType == ItemDisplayContext.FIXED) {
                if (stack.isFramed() && stack.getFrame() instanceof BladeStandEntity) {
                    renderModel(stack, poseStack, buffer, light);
                } else {
                    poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
                    renderIcon(stack, poseStack, buffer, light, 0.0095f);
                }
            } else {
                renderIcon(stack, poseStack, buffer, light, 0.0095f);
            }
        }

        return true;
    }

    public void renderIcon(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int light, float scale) {
        renderIcon(stack, poseStack, buffer, light, scale, false);
    }

    public void renderIcon(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int light,
                           float scale, boolean renderDurability) {

        poseStack.scale(scale, scale, scale);

        EnumSet<SwordType> types = SwordType.from(stack);

        var bladeState = ItemSlashBlade.getBladeState(stack);
        ResourceLocation modelLocation = (bladeState != null && bladeState.getModel().isPresent())
                ? bladeState.getModel().get()
                : stackDefaultModel(stack);
        ResourceLocation textureLocation = (bladeState != null && bladeState.getTexture().isPresent())
                ? bladeState.getTexture().get()
                : stackDefaultTexture(stack);

        WavefrontObject model = BladeModelManager.getInstance().getModel(modelLocation);

        String renderTarget;
        if (types.contains(SwordType.BROKEN)) {
            renderTarget = "item_damaged";
        } else if (types.contains(SwordType.NOSCABBARD)) {
            renderTarget = "item_bladens";
        } else {
            renderTarget = "item_blade";
        }

        BladeRenderState.renderOverrided(stack, model, renderTarget, textureLocation, poseStack, buffer, light);
        BladeRenderState.renderOverridedLuminous(stack, model, renderTarget + "_luminous", textureLocation, poseStack, buffer, light);

        if (renderDurability) {
            WavefrontObject durabilityModel = BladeModelManager.getInstance()
                    .getModel(DefaultResources.resourceDurabilityModel);

            float durability = (float) stack.getDamageValue() / (float) stack.getMaxDamage();
            poseStack.translate(0.0F, 0.0F, 0.1f);

            Color aCol = new Color(0.25f, 0.25f, 0.25f, 1.0f);
            Color bCol = new Color(0xA52C63);
            int r = 0xFF & (int) Mth.lerp(durability, aCol.getRed(), bCol.getRed());
            int g = 0xFF & (int) Mth.lerp(durability, aCol.getGreen(), bCol.getGreen());
            int b = 0xFF & (int) Mth.lerp(durability, aCol.getBlue(), bCol.getBlue());

            BladeRenderState.setCol(new Color(r, g, b));
            BladeRenderState.renderOverrided(stack, durabilityModel, "base",
                    DefaultResources.resourceDurabilityTexture, poseStack, buffer, light);

            boolean isBroken = types.contains(SwordType.BROKEN);
            poseStack.translate(0.0F, 0.0F, -2.0f * durability);

            BladeRenderState.renderOverrided(stack, durabilityModel,
                    isBroken ? "color_r" : "color",
                    DefaultResources.resourceDurabilityTexture, poseStack, buffer, light);
        }
    }

    public ResourceLocation stackDefaultModel(ItemStack stack) {
        var bladeState = ItemSlashBlade.getBladeState(stack);
        String name = null;
        if (bladeState != null && bladeState.getModel().isPresent()) {
            name = bladeState.getModel().get().toString();
        }
        if (!(stack.getItem() instanceof ItemSlashBladeDetune)) {
            String key = bladeState != null ? bladeState.getTranslationKey() : null;
            if (key != null && !key.isBlank()) {
                ResourceLocation bladeName = ResourceLocation.tryParse(
                        key.substring(5).replaceFirst(Pattern.quote("."), Matcher.quoteReplacement(":"))
                );
                SlashBladeDefinition def = BladeModelManager.getClientSlashBladeRegistry().get(bladeName);
                if (def != null) {
                    name = def.getRenderDefinition().getModelName().toString();
                }
            }
        }
        if (name != null) {
            return !name.isBlank() ? ResourceLocation.tryParse(name) : DefaultResources.resourceDefaultModel;
        }
        return DefaultResources.resourceDefaultModel;
    }

    public ResourceLocation stackDefaultTexture(ItemStack stack) {
        var bladeState = ItemSlashBlade.getBladeState(stack);
        String name = null;
        if (bladeState != null && bladeState.getTexture().isPresent()) {
            name = bladeState.getTexture().get().toString();
        }
        if (!(stack.getItem() instanceof ItemSlashBladeDetune)) {
            String key = bladeState != null ? bladeState.getTranslationKey() : null;
            if (key != null && !key.isBlank()) {
                ResourceLocation bladeName = ResourceLocation.tryParse(
                        key.substring(5).replaceFirst(Pattern.quote("."), Matcher.quoteReplacement(":"))
                );
                SlashBladeDefinition def = BladeModelManager.getClientSlashBladeRegistry().get(bladeName);
                if (def != null) {
                    name = def.getRenderDefinition().getTextureName().toString();
                }
            }
        }
        if (name != null) {
            return !name.isBlank() ? ResourceLocation.tryParse(name) : DefaultResources.resourceDefaultTexture;
        }
        return DefaultResources.resourceDefaultTexture;
    }

    public void renderModel(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int light) {
        float scale = 0.003125f;
        poseStack.scale(scale, scale, scale);
        float defaultOffset = 130;
        poseStack.translate(defaultOffset, 0, 0);

        EnumSet<SwordType> types = SwordType.from(stack);

        var bladeState = ItemSlashBlade.getBladeState(stack);
        ResourceLocation modelLocation = (bladeState != null && bladeState.getModel().isPresent())
                ? bladeState.getModel().get()
                : stackDefaultModel(stack);
        ResourceLocation textureLocation = (bladeState != null && bladeState.getTexture().isPresent())
                ? bladeState.getTexture().get()
                : stackDefaultTexture(stack);
        WavefrontObject model = BladeModelManager.getInstance().getModel(modelLocation);

        Vec3 bladeOffset = Vec3.ZERO;
        float bladeOffsetRot = 0;
        float bladeOffsetBaseRot = -3;
        Vec3 sheathOffset = Vec3.ZERO;
        float sheathOffsetRot = 0;
        float sheathOffsetBaseRot = -3;
        boolean vFlip = false;
        boolean hFlip = false;
        boolean hasScabbard = true;

        if (stack.isFramed() && stack.getFrame() instanceof BladeStandEntity stand) {
            Item type = stand.currentType;

            Pose pose = stand.getPose();
            switch (pose.ordinal()) {
                case 1 -> vFlip = true;
                case 2 -> {
                    vFlip = true;
                    hFlip = true;
                }
                case 3 -> hFlip = true;
                case 4 -> hasScabbard = false;
                case 5 -> {
                    hFlip = true;
                    hasScabbard = false;
                }
            }

            if (type == SlashBladeItems.BLADESTAND_2.get()) {
                bladeOffset = new Vec3(0, 21.5f, 0);
                sheathOffset = hFlip ? new Vec3(-40, -27, 0) : new Vec3(40, -27, 0);
                sheathOffsetBaseRot = -4;
            } else if (type == SlashBladeItems.BLADESTAND_V.get()) {
                bladeOffset = new Vec3(-100, 230, 0);
                sheathOffset = new Vec3(-100, 230, 0);
                bladeOffsetRot = 80;
                sheathOffsetRot = 80;
            } else if (type == SlashBladeItems.BLADESTAND_S.get()) {
                bladeOffset = hFlip ? new Vec3(60, -25, 0) : new Vec3(-60, -25, 0);
                sheathOffset = bladeOffset;
            } else if (type == SlashBladeItems.BLADESTAND_2_W.get()) {
                bladeOffset = new Vec3(0, 21.5f, 0);
                sheathOffset = hFlip ? new Vec3(-40, -27, 0) : new Vec3(40, -27, 0);
                sheathOffsetBaseRot = -4;
            }
        }

        try (MSAutoCloser ignored = MSAutoCloser.pushMatrix(poseStack)) {
            String renderTarget = types.contains(SwordType.BROKEN) ? "blade_damaged" : "blade";

            poseStack.translate(bladeOffset.x, bladeOffset.y, bladeOffset.z);
            poseStack.mulPose(Axis.ZP.rotationDegrees(bladeOffsetRot));

            if (vFlip) {
                poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
                poseStack.translate(0, -15, 0);
                poseStack.translate(0, 5, 0);
            }

            if (hFlip) {
                double offset = defaultOffset;
                poseStack.translate(-offset, 0, 0);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
                poseStack.translate(offset, 0, 0);
            }

            poseStack.mulPose(Axis.ZP.rotationDegrees(bladeOffsetBaseRot));

            BladeRenderState.renderOverrided(stack, model, renderTarget, textureLocation, poseStack, buffer, light);
            BladeRenderState.renderOverridedLuminous(stack, model, renderTarget + "_luminous", textureLocation, poseStack, buffer, light);
        }

        if (hasScabbard) {
            try (MSAutoCloser ignored = MSAutoCloser.pushMatrix(poseStack)) {
                String renderTarget = "sheath";

                poseStack.translate(sheathOffset.x, sheathOffset.y, sheathOffset.z);
                poseStack.mulPose(Axis.ZP.rotationDegrees(sheathOffsetRot));

                if (vFlip) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
                    poseStack.translate(0, -15, 0);
                    poseStack.translate(0, 5, 0);
                }

                if (hFlip) {
                    double offset = defaultOffset;
                    poseStack.translate(-offset, 0, 0);
                    poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
                    poseStack.translate(offset, 0, 0);
                }

                poseStack.mulPose(Axis.ZP.rotationDegrees(sheathOffsetBaseRot));

                BladeRenderState.renderOverrided(stack, model, renderTarget, textureLocation, poseStack, buffer, light);
                BladeRenderState.renderOverridedLuminous(stack, model, renderTarget + "_luminous", textureLocation, poseStack, buffer, light);
            }
        }
    }
}
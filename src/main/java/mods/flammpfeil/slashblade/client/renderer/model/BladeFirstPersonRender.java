package mods.flammpfeil.slashblade.client.renderer.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mods.flammpfeil.slashblade.client.renderer.SlashBladeTEISR;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.SwordType;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderHandEvent;

import java.util.EnumSet;

public class BladeFirstPersonRender {
    private static final float BLADE_SCALE = 0.008f;

    private BladeFirstPersonRender() {
    }

    private static final class SingletonHolder {
        private static final BladeFirstPersonRender instance = new BladeFirstPersonRender();
    }

    public static BladeFirstPersonRender getInstance() {
        return SingletonHolder.instance;
    }

    public void render(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (mc.options.getCameraType() != CameraType.FIRST_PERSON || mc.options.hideGui) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof ItemSlashBlade)) {
            return;
        }
        if (ItemSlashBlade.getBladeState(stack) == null) {
            return;
        }
        if (!ItemStack.isSameItemSameComponents(player.getMainHandItem(), stack)) {
            return;
        }

        BladeModel.user = player;

        try (MSAutoCloser ignored = MSAutoCloser.pushMatrix(event.getPoseStack())) {
            HumanoidArm arm = player.getMainArm();
            applyItemArmTransform(event.getPoseStack(), arm, event.getEquipProgress());
            applyItemArmAttackTransform(event.getPoseStack(), arm, event.getSwingProgress());
            renderBladeInHand(stack, arm, event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
        }
    }

    private void renderBladeInHand(ItemStack stack, HumanoidArm arm, PoseStack poseStack,
                                   MultiBufferSource buffer, int light) {
        EnumSet<SwordType> types = SwordType.from(stack);
        ResourceLocation modelLocation = SlashBladeTEISR.resolveModelLocation(stack);
        ResourceLocation textureLocation = SlashBladeTEISR.resolveTextureLocation(stack);
        WavefrontObject model = BladeModelManager.getInstance().getModel(modelLocation);

        float armSign = arm == HumanoidArm.RIGHT ? 1.0f : -1.0f;

        poseStack.translate(armSign * 0.5f, 0.3f, 0.55f);
        poseStack.scale(BLADE_SCALE, BLADE_SCALE, BLADE_SCALE);
        poseStack.translate(0.0f, 0.15f, 0.0f);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0f * armSign));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0f * armSign));

        String renderTarget = types.contains(SwordType.BROKEN) ? "blade_damaged" : "blade";
        BladeRenderState.renderOverrided(stack, model, renderTarget, textureLocation, poseStack, buffer, light);
        BladeRenderState.renderOverridedLuminous(stack, model, renderTarget + "_luminous", textureLocation,
                poseStack, buffer, light);
    }

    private static void applyItemArmTransform(PoseStack poseStack, HumanoidArm arm, float equipProgress) {
        float armSign = arm == HumanoidArm.RIGHT ? 1.0f : -1.0f;
        poseStack.translate(armSign * 0.56f, -0.52f + equipProgress * -0.6f, -0.72f);
    }

    private static void applyItemArmAttackTransform(PoseStack poseStack, HumanoidArm arm, float swingProgress) {
        float armSign = arm == HumanoidArm.RIGHT ? 1.0f : -1.0f;
        float squaredSwingSin = Mth.sin(swingProgress * swingProgress * Mth.PI);
        float rootSwingSin = Mth.sin(Mth.sqrt(swingProgress) * Mth.PI);

        poseStack.mulPose(Axis.YP.rotationDegrees(armSign * (45.0f + squaredSwingSin * -20.0f)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(armSign * rootSwingSin * -20.0f));
        poseStack.mulPose(Axis.XP.rotationDegrees(rootSwingSin * -80.0f));
        poseStack.mulPose(Axis.YP.rotationDegrees(armSign * -45.0f));
    }
}

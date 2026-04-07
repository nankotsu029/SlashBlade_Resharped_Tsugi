package mods.flammpfeil.slashblade.client.renderer.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mods.flammpfeil.slashblade.client.renderer.layers.LayerMainBlade;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BladeFirstPersonRender {
    private LayerMainBlade<LocalPlayer, ?> layer = null;
    private EntityRenderer<?> currentRenderer = null;

    @SuppressWarnings({"unchecked", "rawtypes"})
    private BladeFirstPersonRender() {
        refreshLayer();
    }

    private static final class SingletonHolder {
        private static final BladeFirstPersonRender instance = new BladeFirstPersonRender();
    }

    public static BladeFirstPersonRender getInstance() {
        return SingletonHolder.instance;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void refreshLayer() {
        Minecraft mc = Minecraft.getInstance();
        EntityRenderer<?> renderer = null;
        if (mc.player != null) {
            renderer = mc.getEntityRenderDispatcher().getRenderer(mc.player);
        }
        if (renderer == currentRenderer) {
            return;
        }
        currentRenderer = renderer;
        if (renderer instanceof RenderLayerParent<?, ?> parent) {
            layer = new LayerMainBlade(parent);
        } else {
            layer = null;
        }
    }

    private static boolean isMainHandFirstPersonContext(LocalPlayer player, ItemDisplayContext ctx) {
        return player.getMainArm() == HumanoidArm.RIGHT
                ? ctx == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                : ctx == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
    }

    public void render(ItemStack renderedStack, ItemDisplayContext transformType,
                       PoseStack poseStack, MultiBufferSource buffer, int light) {
        Minecraft mc = Minecraft.getInstance();
        refreshLayer();
        if (layer == null) {
            return;
        }

        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }

        boolean sleeping = mc.getCameraEntity() instanceof LivingEntity living && living.isSleeping();
        if (mc.gameMode != null && !(mc.options.getCameraType() == CameraType.FIRST_PERSON
                && !sleeping
                && !mc.options.hideGui
                && !mc.gameMode.isAlwaysFlying())) {
            return;
        }

        if (!isMainHandFirstPersonContext(player, transformType)) {
            return;
        }

        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty()) {
            return;
        }
        if (!(mainHand.getItem() instanceof ItemSlashBlade)) {
            return;
        }
        if (ItemSlashBlade.getBladeState(mainHand) == null) {
            return;
        }

        // 今描こうとしている stack と player's main hand がズレていたら描かない
        if (!ItemStack.isSameItemSameComponents(mainHand, renderedStack)) {
            return;
        }

        BladeModel.user = player;

        try (MSAutoCloser ignored = MSAutoCloser.pushMatrix(poseStack)) {
            PoseStack.Pose last = poseStack.last();
            last.pose().identity();
            last.normal().identity();

            poseStack.translate(0.0f, 0.0f, -0.5f);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
            poseStack.scale(1.2F, 1.0F, 1.0F);

            // 旧コード準拠: カメラの pitch だけを同期
            poseStack.mulPose(Axis.XP.rotationDegrees(-player.getXRot()));

            float partialTicks = mc.getTimer().getGameTimeDeltaPartialTick(true);
            layer.render(poseStack, buffer, light, player,
                    0.0f, 0.0f, partialTicks,
                    0.0f, 0.0f, 0.0f);
        }
    }
}
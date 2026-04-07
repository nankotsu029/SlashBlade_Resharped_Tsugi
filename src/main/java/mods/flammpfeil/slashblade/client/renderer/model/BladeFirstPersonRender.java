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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Created by Furia on 2016/02/07.
 */
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
        if (renderer instanceof RenderLayerParent) {
            layer = new LayerMainBlade((RenderLayerParent) renderer);
        } else {
            layer = null;
        }
    }

    public void render(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn) {
        Minecraft mc = Minecraft.getInstance();
        refreshLayer();
        if (layer == null) {
            return;
        }

        boolean flag = mc.getCameraEntity() instanceof LivingEntity
                && ((LivingEntity) mc.getCameraEntity()).isSleeping();
        if (mc.gameMode != null && !(mc.options.getCameraType() == CameraType.FIRST_PERSON && !flag && !mc.options.hideGui
                && !mc.gameMode.isAlwaysFlying())) {
            return;
        }
        LocalPlayer player = mc.player;
        ItemStack stack = null;
        if (player != null) {
            stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        }
        if (stack != null && stack.isEmpty()) {
            return;
        }
        if (stack != null && ItemSlashBlade.getBladeState(stack) == null) {
            return;
        }
        BladeModel.user = player;

        try (MSAutoCloser msac = MSAutoCloser.pushMatrix(matrixStack)) {
            PoseStack.Pose me = matrixStack.last();
            me.pose().identity();
            me.normal().identity();

            matrixStack.translate(0.0f, 0.0f, -0.5f);
            matrixStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
            matrixStack.scale(1.2F, 1.0F, 1.0F);

            // no sync pitch
            matrixStack.mulPose(Axis.XP.rotationDegrees(-mc.player.getXRot()));

            // layer.disableOffhandRendering();
            float partialTicks = mc.getTimer().getGameTimeDeltaPartialTick(true);
            layer.render(matrixStack, bufferIn, combinedLightIn, mc.player, 0, 0, partialTicks, 0, 0, 0);
        }
    }
}

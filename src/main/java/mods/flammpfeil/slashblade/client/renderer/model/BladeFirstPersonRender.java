package mods.flammpfeil.slashblade.client.renderer.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mods.flammpfeil.slashblade.client.renderer.layers.LayerMainBlade;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderHandEvent;

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

    public void render(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        refreshLayer();
        if (layer == null) {
            return;
        }

        LocalPlayer player = mc.player;
        if (player == null || event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) {
            return;
        }

        boolean sleeping = mc.getCameraEntity() instanceof LivingEntity living && living.isSleeping();
        if (mc.options.getCameraType() != CameraType.FIRST_PERSON || mc.options.hideGui || sleeping) {
            return;
        }
        if (mc.gameMode != null && mc.gameMode.isAlwaysFlying()) {
            return;
        }

        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty() || !(mainHand.getItem() instanceof ItemSlashBlade)) {
            return;
        }
        if (ItemSlashBlade.getBladeState(mainHand) == null) {
            return;
        }
        if (!ItemStack.isSameItemSameComponents(mainHand, event.getItemStack())) {
            return;
        }

        BladeModel.user = player;

        try (MSAutoCloser ignored = MSAutoCloser.pushMatrix(event.getPoseStack())) {
            PoseStack.Pose last = event.getPoseStack().last();
            last.pose().identity();
            last.normal().identity();

            float armSign = player.getMainArm() == HumanoidArm.RIGHT ? -1.0f : 1.0f;

            event.getPoseStack().translate(armSign * 0.35f, 0.0f, -0.5f);
            event.getPoseStack().mulPose(Axis.ZP.rotationDegrees(180.0f));
            event.getPoseStack().scale(1.2F, 1.0F, 1.0F);

            // 1.20.1 互換: pitch のみ同期する。
            event.getPoseStack().mulPose(Axis.XP.rotationDegrees(-event.getInterpolatedPitch()));

            layer.renderFirstPerson(
                    event.getPoseStack(),
                    event.getMultiBufferSource(),
                    event.getPackedLight(),
                    player,
                    event.getPartialTick()
            );
        }
    }
}

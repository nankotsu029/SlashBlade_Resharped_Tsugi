package mods.flammpfeil.slashblade.event.client;

import mods.flammpfeil.slashblade.client.renderer.model.BladeFirstPersonRender;
import mods.flammpfeil.slashblade.client.renderer.layers.LayerMainBlade;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.common.NeoForge;

public class LayerEvent {
    private static final LayerEvent INSTANCE = new LayerEvent();

    public static LayerEvent getInstance() {
        return INSTANCE;
    }

    public void register() {
        NeoForge.EVENT_BUS.register(this);
    }

    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model type : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(type);
            if (renderer != null) {
                renderer.addLayer(new LayerMainBlade<>(renderer));
            }
        }
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        BladeFirstPersonRender.getInstance().render(event);
    }
}

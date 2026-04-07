package mods.flammpfeil.slashblade.event.client;

import mods.flammpfeil.slashblade.client.renderer.layers.LayerMainBlade;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class LayerEvent {
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model type : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(type);
            if (renderer != null) {
                renderer.addLayer(new LayerMainBlade<>(renderer));
            }
        }
    }
}

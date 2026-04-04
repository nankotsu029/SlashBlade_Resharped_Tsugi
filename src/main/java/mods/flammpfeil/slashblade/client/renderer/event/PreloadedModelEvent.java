package mods.flammpfeil.slashblade.client.renderer.event;

import mods.flammpfeil.slashblade.SlashBlade;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

public final class PreloadedModelEvent {
    private PreloadedModelEvent() {
    }

    public static void registerResourceLoaders(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ModelResourceLoader());
    }
}

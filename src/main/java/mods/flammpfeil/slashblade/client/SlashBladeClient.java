package mods.flammpfeil.slashblade.client;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.client.renderer.entity.BladeItemEntityRenderer;
import mods.flammpfeil.slashblade.client.renderer.entity.BladeStandEntityRenderer;
import mods.flammpfeil.slashblade.client.renderer.entity.DriveRenderer;
import mods.flammpfeil.slashblade.client.renderer.entity.JudgementCutRenderer;
import mods.flammpfeil.slashblade.client.renderer.entity.SlashEffectRenderer;
import mods.flammpfeil.slashblade.client.renderer.entity.SummonedSwordRenderer;
import mods.flammpfeil.slashblade.client.renderer.event.PreloadedModelEvent;
import mods.flammpfeil.slashblade.event.client.SlashBladeLayerEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = SlashBlade.MODID, dist = Dist.CLIENT)
public final class SlashBladeClient {

    public SlashBladeClient(final IEventBus modBus) {
        registerModBusListeners(modBus);
    }

    private static void registerModBusListeners(final IEventBus modBus) {
        modBus.addListener(ClientHandler::doClientStuff);
        modBus.addListener(ClientHandler::registerKeyMapping);
        modBus.addListener(ClientHandler::bakeModels);
        modBus.addListener(SlashBladeLayerEvent::onAddLayers);
        modBus.addListener(ClientHandler::onCreativeTagBuilding);
        modBus.addListener(ClientHandler::onTextureAtlasStitched);
        modBus.addListener(PreloadedModelEvent::registerResourceLoaders);
        modBus.addListener(SlashBladeClient::registerEntityRenderers);
    }

    private static void registerEntityRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SlashBlade.RegistryEvents.SummonedSword, SummonedSwordRenderer::new);
        event.registerEntityRenderer(SlashBlade.RegistryEvents.StormSwords, SummonedSwordRenderer::new);
        event.registerEntityRenderer(SlashBlade.RegistryEvents.SpiralSwords, SummonedSwordRenderer::new);
        event.registerEntityRenderer(SlashBlade.RegistryEvents.BlisteringSwords, SummonedSwordRenderer::new);
        event.registerEntityRenderer(SlashBlade.RegistryEvents.HeavyRainSwords, SummonedSwordRenderer::new);
        event.registerEntityRenderer(SlashBlade.RegistryEvents.JudgementCut, JudgementCutRenderer::new);
        event.registerEntityRenderer(SlashBlade.RegistryEvents.BladeItem, BladeItemEntityRenderer::new);
        event.registerEntityRenderer(SlashBlade.RegistryEvents.BladeStand, BladeStandEntityRenderer::new);
        event.registerEntityRenderer(SlashBlade.RegistryEvents.SlashEffect, SlashEffectRenderer::new);
        event.registerEntityRenderer(SlashBlade.RegistryEvents.Drive, DriveRenderer::new);
    }
}

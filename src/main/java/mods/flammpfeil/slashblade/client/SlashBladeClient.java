package mods.flammpfeil.slashblade.client;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.client.renderer.entity.BladeItemEntityRenderer;
import mods.flammpfeil.slashblade.client.renderer.entity.BladeStandEntityRenderer;
import mods.flammpfeil.slashblade.client.renderer.entity.DriveRenderer;
import mods.flammpfeil.slashblade.client.renderer.entity.JudgementCutRenderer;
import mods.flammpfeil.slashblade.client.renderer.entity.SlashEffectRenderer;
import mods.flammpfeil.slashblade.client.renderer.entity.SummonedSwordRenderer;
import mods.flammpfeil.slashblade.client.renderer.event.PreloadedModelEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = SlashBlade.MODID, dist = Dist.CLIENT)
public class SlashBladeClient {
    public SlashBladeClient(IEventBus modBus, ModContainer container) {
        modBus.addListener(ClientHandler::doClientStuff);
        modBus.addListener(ClientHandler::registerKeyMapping);
        modBus.addListener(ClientHandler::bakeModels);
        modBus.addListener(ClientHandler::addLayers);
        modBus.addListener(ClientHandler::onCreativeTagBuilding);
        modBus.addListener(PreloadedModelEvent::registerResourceLoaders);
        modBus.addListener(SlashBladeClient::registerEntityRenderers);

        NeoForge.EVENT_BUS.addListener(ClientHandler::onTextureAtlasStitched);
    }

    private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
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

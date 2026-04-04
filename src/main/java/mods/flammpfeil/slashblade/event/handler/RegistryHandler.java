package mods.flammpfeil.slashblade.event.handler;
import mods.flammpfeil.slashblade.event.drop.EntityDropEntry;
import mods.flammpfeil.slashblade.registry.slashblade.SlashBladeDefinition;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@EventBusSubscriber(modid = "slashblade")
public class RegistryHandler {

    @SubscribeEvent
    public static void onDatapackRegister(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(SlashBladeDefinition.REGISTRY_KEY, SlashBladeDefinition.CODEC,
                SlashBladeDefinition.CODEC);

        event.dataPackRegistry(EntityDropEntry.REGISTRY_KEY, EntityDropEntry.CODEC, EntityDropEntry.CODEC);
    }
}

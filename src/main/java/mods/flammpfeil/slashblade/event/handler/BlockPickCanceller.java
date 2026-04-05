package mods.flammpfeil.slashblade.event.handler;

import mods.flammpfeil.slashblade.client.SlashBladeKeyMappings;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;

public class BlockPickCanceller {
    private static final class SingletonHolder {
        private static final BlockPickCanceller instance = new BlockPickCanceller();
    }

    public static BlockPickCanceller getInstance() {
        return BlockPickCanceller.SingletonHolder.instance;
    }

    private BlockPickCanceller() {
    }

    public static void register(IEventBus forgeEventBus) {
        // InputEvent は NeoForge.EVENT_BUS に流れるので、そちらに登録する
        NeoForge.EVENT_BUS.addListener(BlockPickCanceller::onBlockPick);
    }

    @OnlyIn(Dist.CLIENT)
    public static void onBlockPick(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isPickBlock()) {
            return;
        }

        final Minecraft instance = Minecraft.getInstance();
        LocalPlayer player = instance.player;
        if (player == null) {
            return;
        }
        if (SlashBladeKeyMappings.KEY_SUMMON_BLADE.getKey() != SlashBladeKeyMappings.KEY_SUMMON_BLADE.getDefaultKey()) {
            return;
        }
        if (ItemSlashBlade.getBladeState(player.getMainHandItem()) != null) {
            event.setCanceled(true);
        }
    }
}

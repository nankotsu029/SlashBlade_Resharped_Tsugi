package mods.flammpfeil.slashblade.event;

import mods.flammpfeil.slashblade.network.MotionBroadcastMessage;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.bus.api.SubscribeEvent;

public class BladeMotionEventBroadcaster {

    private static final class SingletonHolder {
        private static final BladeMotionEventBroadcaster instance = new BladeMotionEventBroadcaster();
    }

    public static BladeMotionEventBroadcaster getInstance() {
        return BladeMotionEventBroadcaster.SingletonHolder.instance;
    }

    private BladeMotionEventBroadcaster() {
    }

    public void register() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onBladeMotion(BladeMotionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) {
            return;
        }

        MotionBroadcastMessage msg = new MotionBroadcastMessage(sp.getUUID(), event.getCombo().toString());

        PacketDistributor.sendToPlayersNear(sp.serverLevel(), null,
                sp.getX(), sp.getY(), sp.getZ(), 20, msg);
    }
}

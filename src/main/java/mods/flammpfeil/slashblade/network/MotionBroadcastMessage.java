package mods.flammpfeil.slashblade.network;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.event.BladeMotionEvent;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record MotionBroadcastMessage(UUID playerId, String combo) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MotionBroadcastMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID, "motion_broadcast"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MotionBroadcastMessage> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> {
                buf.writeUUID(msg.playerId());
                buf.writeUtf(msg.combo());
            },
            buf -> new MotionBroadcastMessage(buf.readUUID(), buf.readUtf())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @OnlyIn(Dist.CLIENT)
    public static void handle(MotionBroadcastMessage msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> setPoint(msg.playerId(), msg.combo()));
    }

    @OnlyIn(Dist.CLIENT)
    static void setPoint(UUID playerId, String combo) {
        Player target = null;
        if (Minecraft.getInstance().level != null) {
            target = Minecraft.getInstance().level.getPlayerByUUID(playerId);
        }

        if (target == null) {
            return;
        }
        if (!(target instanceof AbstractClientPlayer)) {
            return;
        }

        ResourceLocation state = ResourceLocation.tryParse(combo);
        if (state == null || !ComboStateRegistry.REGISTRY.containsKey(state)) {
            return;
        }

        NeoForge.EVENT_BUS.post(new BladeMotionEvent(target, state));
    }
}

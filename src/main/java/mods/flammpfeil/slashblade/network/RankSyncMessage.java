package mods.flammpfeil.slashblade.network;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.concentrationrank.CapabilityConcentrationRank;
import mods.flammpfeil.slashblade.capability.concentrationrank.IConcentrationRank;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RankSyncMessage(long rawPoint) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RankSyncMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID, "rank_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RankSyncMessage> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> buf.writeLong(msg.rawPoint()),
            buf -> new RankSyncMessage(buf.readLong())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @OnlyIn(Dist.CLIENT)
    public static void handle(RankSyncMessage msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> setPoint(msg.rawPoint()));
    }

    @OnlyIn(Dist.CLIENT)
    static void setPoint(long point) {
        Player pl = Minecraft.getInstance().player;
        if (pl != null) {
            var cr = pl.getData(CapabilityConcentrationRank.RANK_POINT);
            long time = pl.level().getGameTime();
            IConcentrationRank.ConcentrationRanks oldRank = cr.getRank(time);
            cr.setRawRankPoint(point);
            cr.setLastUpdte(time);
            if (oldRank.level < cr.getRank(time).level) {
                cr.setLastRankRise(time);
            }
        }
    }
}

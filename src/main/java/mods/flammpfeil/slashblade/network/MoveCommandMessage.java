package mods.flammpfeil.slashblade.network;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.inputstate.CapabilityInputState;
import mods.flammpfeil.slashblade.event.handler.InputCommandEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.EnumSetConverter;
import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.EnumSet;

public record MoveCommandMessage(int command) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MoveCommandMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID, "move_command"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MoveCommandMessage> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> buf.writeInt(msg.command()),
            buf -> new MoveCommandMessage(buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MoveCommandMessage msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sender)) {
                return;
            }

            ItemStack stack = sender.getItemInHand(InteractionHand.MAIN_HAND);
            if (stack.isEmpty()) {
                return;
            }
            if (ItemSlashBlade.getBladeState(stack) == null) {
                return;
            }

            var state = sender.getData(CapabilityInputState.INPUT_STATE);
            EnumSet<InputCommand> old = state.getCommands().clone();

            state.getCommands().clear();
            state.getCommands().addAll(EnumSetConverter.convertToEnumSet(InputCommand.class, msg.command()));

            EnumSet<InputCommand> current = state.getCommands().clone();

            long currentTime = sender.level().getGameTime();
            current.forEach(c -> {
                if (!old.contains(c)) {
                    state.getLastPressTimes().put(c, currentTime);
                }
            });

            InputCommandEvent.onInputChange(sender, state, old, current);
        });
    }
}

package mods.flammpfeil.slashblade.event.handler;

import mods.flammpfeil.slashblade.capability.inputstate.CapabilityInputState;
import mods.flammpfeil.slashblade.client.SlashBladeKeyMappings;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.network.MoveCommandMessage;
import mods.flammpfeil.slashblade.util.EnumSetConverter;
import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.EnumSet;

@EventBusSubscriber(modid = "slashblade", value = Dist.CLIENT)
public class MoveInputHandler {

    public static final String LAST_CHANGE_TIME = "SB_LAST_CHANGE_TIME";

    public static boolean checkFlag(int data, int flags) {
        return (data & flags) == flags;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onPlayerPostTick(ClientTickEvent.Post event) {

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        if (player.getMainHandItem().isEmpty() || ItemSlashBlade.getBladeState(player.getMainHandItem()) == null) {
            return;
        }

        EnumSet<InputCommand> commands = EnumSet.noneOf(InputCommand.class);

        if (player.input.up) {
            commands.add(InputCommand.FORWARD);
        }
        if (player.input.down) {
            commands.add(InputCommand.BACK);
        }
        if (player.input.left) {
            commands.add(InputCommand.LEFT);
        }
        if (player.input.right) {
            commands.add(InputCommand.RIGHT);
        }

        if (player.input.shiftKeyDown) {
            commands.add(InputCommand.SNEAK);
        }

        if (player.input.jumping) {
            commands.add(InputCommand.JUMP);
        }

        final Minecraft minecraftInstance = Minecraft.getInstance();

        if (SlashBladeKeyMappings.KEY_SPECIAL_MOVE.isDown()) {
            commands.add(InputCommand.SPRINT);
        }

        if (minecraftInstance.options.keyUse.isDown()) {
            commands.add(InputCommand.R_DOWN);
        }
        if (minecraftInstance.options.keyAttack.isDown()) {
            commands.add(InputCommand.L_DOWN);
        }

        if (SlashBladeKeyMappings.KEY_SUMMON_BLADE.isDown()) {
            commands.add(InputCommand.M_DOWN);
        }

        var inputState = player.getData(CapabilityInputState.INPUT_STATE);
        EnumSet<InputCommand> old = inputState.getCommands().clone();

        Level worldIn = player.getCommandSenderWorld();

        long currentTime = worldIn.getGameTime();
        boolean doSend = !old.equals(commands);

        if (doSend) {
            commands.forEach(c -> {
                if (!old.contains(c)) {
                    inputState.getLastPressTimes().put(c, currentTime);
                }
            });

            inputState.getCommands().clear();
            inputState.getCommands().addAll(commands);

            PacketDistributor.sendToServer(new MoveCommandMessage(EnumSetConverter.convertToInt(commands)));
        }
    }
}

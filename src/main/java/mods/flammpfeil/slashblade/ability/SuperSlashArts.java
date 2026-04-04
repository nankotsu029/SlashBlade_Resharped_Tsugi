package mods.flammpfeil.slashblade.ability;

import mods.flammpfeil.slashblade.capability.inputstate.CapabilityInputState;
import mods.flammpfeil.slashblade.event.handler.InputCommandEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.SwordType;
import mods.flammpfeil.slashblade.registry.ComboStateRegistry;
import mods.flammpfeil.slashblade.registry.combo.ComboState;
import mods.flammpfeil.slashblade.slasharts.SlashArts;
import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.EnumSet;
import java.util.Map;

public class SuperSlashArts {
    private static final class SingletonHolder {
        private static final SuperSlashArts instance = new SuperSlashArts();
    }

    public static SuperSlashArts getInstance() {
        return SuperSlashArts.SingletonHolder.instance;
    }

    private SuperSlashArts() {
    }

    public void register() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onInputChange(InputCommandEvent event) {

        EnumSet<InputCommand> old = event.getOld();
        EnumSet<InputCommand> current = event.getCurrent();
        ServerPlayer sender = event.getEntity();

        InputCommand targetCommnad = InputCommand.SPRINT;

        boolean onDown = !old.contains(targetCommnad) && current.contains(targetCommnad);
        RandomSource random = sender.getRandom();
        final long pressTime = event.getState().getLastPressTime(targetCommnad);
        if (onDown) {
            var input = sender.getData(CapabilityInputState.INPUT_STATE);
            input.getScheduler().schedule("sendPartical", pressTime + 5, (rawEntity, queue, now) -> {

                if (!(rawEntity instanceof ServerPlayer entity)) {
                    return;
                }

                InputCommand targetCommnad2 = InputCommand.SPRINT;
                var input2 = entity.getData(CapabilityInputState.INPUT_STATE);
                boolean inputSucceed = input2.getCommands().contains(targetCommnad2)
                        && (!InputCommand.anyMatch(input2.getCommands(), InputCommand.move)
                        || !input2.getCommands().contains(InputCommand.SNEAK))
                        && input2.getLastPressTime(targetCommnad2) == pressTime;
                if (!inputSucceed) {
                    return;
                }
                ItemStack mainHandItem = entity.getMainHandItem();
                var state = ItemSlashBlade.getBladeState(mainHandItem);
                if (state != null) {
                    if (!state.isBroken() && state.getDamage() == 0 && !state.isSealed()
                            && SwordType.from(mainHandItem).contains(SwordType.BEWITCHED)
                            && SwordType.from(mainHandItem).contains(SwordType.FIERCEREDGE)
                            && entity.onGround()) {
                        for (int i = 0; i < 32; ++i) {
                            double xDist = (random.nextFloat() * 2.0F - 1.0F);
                            double yDist = (random.nextFloat() * 2.0F - 1.0F);
                            double zDist = (random.nextFloat() * 2.0F - 1.0F);
                            if (!(xDist * xDist + yDist * yDist + zDist * zDist > 1.0D)) {
                                double x = sender.getX(xDist / 4.0D);
                                double y = sender.getY(0.5D + yDist / 4.0D);
                                double z = sender.getZ(zDist / 4.0D);
                                ((ServerLevel) event.getEntity().level()).sendParticles(
                                        ParticleTypes.REVERSE_PORTAL, x, y, z, 0, xDist, yDist + 0.2D, zDist, 1);
                            }
                        }
                    }
                }
            });
            input.getScheduler().schedule("chargeSuperSA", pressTime + 20, (rawEntity, queue, now) -> {
                if (!(rawEntity instanceof ServerPlayer entity)) {
                    return;
                }

                InputCommand targetCommnad1 = InputCommand.SPRINT;
                var input1 = entity.getData(CapabilityInputState.INPUT_STATE);
                boolean inputSucceed = input1.getCommands().contains(targetCommnad1)
                        && (!InputCommand.anyMatch(input1.getCommands(), InputCommand.move)
                        || !input1.getCommands().contains(InputCommand.SNEAK))
                        && input1.getLastPressTime(targetCommnad1) == pressTime;
                if (!inputSucceed) {
                    return;
                }

                releaseSSA(entity);
            });
        }
    }

    public static void releaseSSA(ServerPlayer entity) {
        ItemStack mainHandItem = entity.getMainHandItem();
        var state = ItemSlashBlade.getBladeState(mainHandItem);
        if (state != null) {
            if (state.isBroken() || state.getDamage() > 0 || state.isSealed()
                    || !SwordType.from(mainHandItem).contains(SwordType.BEWITCHED)
                    || !SwordType.from(mainHandItem).contains(SwordType.FIERCEREDGE)) {
                return;
            }

            if (!entity.onGround()) {
                return;
            }

            mainHandItem.hurtAndBreak(mainHandItem.getMaxDamage() / 2, entity, EquipmentSlot.MAINHAND);

            Map.Entry<Integer, ResourceLocation> currentloc = state.resolvCurrentComboStateTicks(entity);

            ComboState currentCS = ComboStateRegistry.REGISTRY.get(currentloc.getValue());

            ResourceLocation csloc = state.getSlashArts().doArts(SlashArts.ArtsType.Super, entity);
            ComboState cs = ComboStateRegistry.REGISTRY.get(csloc);
            if (csloc != ComboStateRegistry.NONE.getId() && !currentloc.getValue().equals(csloc)) {

                if (currentCS != null && cs != null && currentCS.getPriority() > cs.getPriority()) {
                    state.updateComboSeq(entity, csloc);
                }
            }
        }
    }
}

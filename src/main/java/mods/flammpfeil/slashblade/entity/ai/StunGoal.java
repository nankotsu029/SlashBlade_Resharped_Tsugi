package mods.flammpfeil.slashblade.entity.ai;

import mods.flammpfeil.slashblade.capability.mobeffect.CapabilityMobEffect;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class StunGoal extends Goal {
    private final PathfinderMob entity;

    public StunGoal(PathfinderMob creature) {
        this.entity = creature;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK, Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        return this.entity.getData(CapabilityMobEffect.MOB_EFFECT)
                .isStun(this.entity.level().getGameTime());
    }

    @Override
    public void stop() {
        this.entity.getData(CapabilityMobEffect.MOB_EFFECT).clearStunTimeOut();
    }
}

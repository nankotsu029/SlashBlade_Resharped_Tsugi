package mods.flammpfeil.slashblade.ability;

import mods.flammpfeil.slashblade.capability.mobeffect.CapabilityMobEffect;
import mods.flammpfeil.slashblade.entity.ai.StunGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Created by Furia on 15/06/20.
 */
public class StunManager {

    static final int DEFAULT_STUN_TICKS = 10;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityJoinWorldEvent(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof PathfinderMob entity)) {
            return;
        }

        entity.goalSelector.addGoal(-1, new StunGoal(entity));
    }

    @SubscribeEvent
    public void onEntityLivingUpdate(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }
        if (!(target instanceof PathfinderMob)) {
            return;
        }

        boolean onStun = target.getData(CapabilityMobEffect.MOB_EFFECT).isStun(target.level().getGameTime());

        if (onStun) {
            Vec3 motion = target.getDeltaMovement();
            if (5 < target.fallDistance) {
                target.setDeltaMovement(motion.x, motion.y - 2.0f, motion.z);
            } else if (motion.y < 0) {
                target.setDeltaMovement(motion.x, motion.y * 0.25f, motion.z);
            }
        }
    }

    public static void setStun(LivingEntity target, LivingEntity attacker) {
        setStun(target);
    }

    public static void setStun(LivingEntity target) {
        setStun(target, DEFAULT_STUN_TICKS);
    }

    public static void setStun(LivingEntity target, long duration) {
        if (!(target instanceof PathfinderMob)) {
            return;
        }

        target.getData(CapabilityMobEffect.MOB_EFFECT).setManagedStun(target.level().getGameTime(), duration);
    }

    public static void removeStun(LivingEntity target) {
        target.getData(CapabilityMobEffect.MOB_EFFECT).clearStunTimeOut();
    }
}

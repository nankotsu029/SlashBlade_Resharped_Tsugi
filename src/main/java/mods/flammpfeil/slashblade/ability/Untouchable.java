package mods.flammpfeil.slashblade.ability;

import mods.flammpfeil.slashblade.capability.mobeffect.CapabilityMobEffect;
import mods.flammpfeil.slashblade.capability.mobeffect.MobEffectState;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.List;

public class Untouchable {
    private static final class SingletonHolder {
        private static final Untouchable instance = new Untouchable();
    }

    public static Untouchable getInstance() {
        return Untouchable.SingletonHolder.instance;
    }

    private Untouchable() {
    }

    public void register() {
        NeoForge.EVENT_BUS.register(this);
    }

    public static void setUntouchable(LivingEntity entity, int ticks) {
        MobEffectState ef = entity.getData(CapabilityMobEffect.MOB_EFFECT);
        ef.setManagedUntouchable(entity.level().getGameTime(), ticks);
        ef.storeEffects(entity.getActiveEffectsMap().keySet());
        ef.storeHealth(entity.getHealth());
    }

    private boolean checkUntouchable(LivingEntity entity) {
        return entity.getData(CapabilityMobEffect.MOB_EFFECT)
                .isUntouchable(entity.getCommandSenderWorld().getGameTime());
    }

    private void doWitchTime(Entity entity) {
        if (entity == null) {
            return;
        }

        if (!(entity instanceof LivingEntity)) {
            return;
        }

        StunManager.setStun((LivingEntity) entity);
    }

    public boolean doUntouchable(LivingEntity self, Entity other) {
        if (checkUntouchable(self)) {
            doWitchTime(other);
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (doUntouchable(event.getEntity(), event.getSource().getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent.Pre event) {
        if (doUntouchable(event.getEntity(), event.getSource().getEntity())) {
            event.setNewDamage(0);
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (doUntouchable(entity, event.getSource().getEntity())) {
            event.setCanceled(true);

            MobEffectState ef = entity.getData(CapabilityMobEffect.MOB_EFFECT);
            if (ef.hasUntouchableWorked()) {
                List<Holder<MobEffect>> filtered = entity.getActiveEffectsMap().keySet().stream()
                        .filter(p -> !(ef.getEffectSet().contains(p) || p.value().isBeneficial())).toList();

                filtered.forEach(entity::removeEffect);

                float storedHealth = ef.getStoredHealth();
                if (ef.getStoredHealth() < storedHealth) {
                    entity.setHealth(ef.getStoredHealth());
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingTicks(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (entity.level().isClientSide()) {
            return;
        }

        MobEffectState ef = entity.getData(CapabilityMobEffect.MOB_EFFECT);
        if (ef.hasUntouchableWorked()) {
            ef.setUntouchableWorked(false);
            List<Holder<MobEffect>> filtered = entity.getActiveEffectsMap().keySet().stream()
                    .filter(p -> !(ef.getEffectSet().contains(p) || p.value().isBeneficial())).toList();

            filtered.forEach(entity::removeEffect);

            float storedHealth = ef.getStoredHealth();
            if (ef.getStoredHealth() < storedHealth) {
                entity.setHealth(ef.getStoredHealth());
            }
        }
    }

    final static int JUMP_TICKS = 10;

    @SubscribeEvent
    public void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (ItemSlashBlade.getBladeState(event.getEntity().getMainHandItem()) == null) {
            return;
        }

        Untouchable.setUntouchable(event.getEntity(), JUMP_TICKS);
    }
}

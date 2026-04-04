package mods.flammpfeil.slashblade.event.handler;

import mods.flammpfeil.slashblade.event.SlashBladeRegistryEvent;
import mods.flammpfeil.slashblade.util.EnchantmentCompat;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "slashblade")
public class SlashBladeEventHandler {

    @SubscribeEvent
    public static void onLivingOnFire(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();

        ItemStack stack = victim.getMainHandItem();
        if (EnchantmentCompat.getLevel(stack, victim, Enchantments.FIRE_PROTECTION) <= 0) {
            return;
        }
        if (!source.is(DamageTypeTags.IS_FIRE)) {
            return;
        }

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLoadingBlade(SlashBladeRegistryEvent.Pre event) {
        if (!net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(event.getSlashBladeDefinition().getItemName())) {
            event.setCanceled(true);
        }
    }

}

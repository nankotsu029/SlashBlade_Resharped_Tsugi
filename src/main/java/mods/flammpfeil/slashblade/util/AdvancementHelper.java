package mods.flammpfeil.slashblade.util;

// TODO(neoforge-1.21.1): This file still uses Forge-only APIs that need a manual NeoForge rewrite.
// TODO(neoforge-1.21.1): Replace remaining ForgeRegistries references with BuiltInRegistries, Registries, or NeoForgeRegistries as appropriate.
// TODO(neoforge-1.21.1): Replace ForgeRegistries.ENCHANTMENTS with a RegistryAccess/Registries.ENCHANTMENT lookup.
import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;

public class AdvancementHelper {

    static public final ResourceLocation ADVANCEMENT_COMBO_A = SlashBlade.prefix("arts/combo_a");
    static public final ResourceLocation ADVANCEMENT_COMBO_A_EX = SlashBlade.prefix("arts/combo_a_ex");
    static public final ResourceLocation ADVANCEMENT_COMBO_B = SlashBlade.prefix("arts/combo_b");
    static public final ResourceLocation ADVANCEMENT_COMBO_B_MAX = SlashBlade.prefix("arts/combo_b_max");
    static public final ResourceLocation ADVANCEMENT_COMBO_C = SlashBlade.prefix("arts/combo_c");
    static public final ResourceLocation ADVANCEMENT_AERIAL_A = SlashBlade.prefix("arts/aerial_a");
    static public final ResourceLocation ADVANCEMENT_AERIAL_B = SlashBlade.prefix("arts/aerial_b");
    static public final ResourceLocation ADVANCEMENT_UPPERSLASH = SlashBlade.prefix("arts/upperslash");
    static public final ResourceLocation ADVANCEMENT_UPPERSLASH_JUMP = SlashBlade.prefix("arts/upperslash_jump");
    static public final ResourceLocation ADVANCEMENT_AERIAL_CLEAVE = SlashBlade.prefix("arts/aerial_cleave");
    static public final ResourceLocation ADVANCEMENT_RISING_STAR = SlashBlade.prefix("arts/rising_star");
    static public final ResourceLocation ADVANCEMENT_RAPID_SLASH = SlashBlade.prefix("arts/rapid_slash");
    static public final ResourceLocation ADVANCEMENT_JUDGEMENT_CUT = SlashBlade.prefix("arts/judgement_cut");
    static public final ResourceLocation ADVANCEMENT_JUDGEMENT_CUT_JUST = SlashBlade.prefix("arts/judgement_cut_just");
    static public final ResourceLocation ADVANCEMENT_QUICK_CHARGE = SlashBlade.prefix("arts/quick_charge");

    public static void grantCriterion(LivingEntity entity, ResourceLocation resourcelocation) {
        if (entity instanceof ServerPlayer) {
            grantCriterion((ServerPlayer) entity, resourcelocation);
        }
    }

    public static void grantCriterion(ServerPlayer player, ResourceLocation resourcelocation) {
        MinecraftServer server = player.getServer();
        AdvancementHolder adv = null;
        if (server != null) {
            adv = server.getAdvancements().get(resourcelocation);
        }
        if (adv == null) {
            return;
        }

        AdvancementProgress advancementprogress = player.getAdvancements().getOrStartProgress(adv);
        if (advancementprogress.isDone()) {
            return;
        }

        for (String s : advancementprogress.getRemainingCriteria()) {
            player.getAdvancements().award(adv, s);
        }
    }

    static final ResourceLocation EXEFFECT_ENCHANTMENT = SlashBlade.prefix("enchantment/");

    static public void grantedIf(ResourceKey<Enchantment> enchantment, LivingEntity owner) {
        int level = EnchantmentCompat.getLevel(owner.getMainHandItem(), owner, enchantment);
        if (0 < level) {
            grantCriterion(owner, EXEFFECT_ENCHANTMENT.withSuffix("root"));
            grantCriterion(owner, EXEFFECT_ENCHANTMENT.withSuffix(enchantment.location().getPath()));
        }
    }
}

package mods.flammpfeil.slashblade.init;

import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.resources.ResourceLocation;

public interface DefaultResources {
    ResourceLocation BaseMotionLocation = SlashBlade.prefix("combostate/old_motion.vmd");
    ResourceLocation ExMotionLocation = SlashBlade.prefix("combostate/motion.vmd");

    ResourceLocation testLocation = SlashBlade.prefix("combostate/piercing.vmd");

    ResourceLocation testPLLocation = SlashBlade.prefix("combostate/piercing_pl.vmd");

    ResourceLocation resourceDefaultModel = SlashBlade.prefix("model/blade.obj");
    ResourceLocation resourceDefaultTexture = SlashBlade.prefix("model/blade.png");

    ResourceLocation resourceDurabilityModel = SlashBlade.prefix("model/util/durability.obj");
    ResourceLocation resourceDurabilityTexture = SlashBlade.prefix("model/util/durability.png");
}

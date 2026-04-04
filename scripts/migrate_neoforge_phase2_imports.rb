#!/usr/bin/env ruby
# frozen_string_literal: true

require "pathname"

ROOT = Pathname(ARGV[0] || "src/main/java")

unless ROOT.directory?
  warn "Directory not found: #{ROOT}"
  exit 1
end

TODO_NETWORK = "// TODO(neoforge-1.21.1): Rewrite this class to the NeoForge payload API; old Forge networking types remain."
TODO_ENCHANTMENTS = "// TODO(neoforge-1.21.1): Replace ForgeRegistries.ENCHANTMENTS with a RegistryAccess/Registries.ENCHANTMENT lookup."
TODO_FORGE_REGISTRIES = "// TODO(neoforge-1.21.1): Replace remaining ForgeRegistries references with BuiltInRegistries, Registries, or NeoForgeRegistries as appropriate."
TODO_TICK_EVENTS = "// TODO(neoforge-1.21.1): Replace Forge TickEvent usages with the split NeoForge tick events."
TODO_DIST_EXECUTOR = "// TODO(neoforge-1.21.1): Replace DistExecutor usage with an explicit FMLLoader.getDist() side check."
TODO_FORGE_API = "// TODO(neoforge-1.21.1): This file still uses Forge-only APIs that need a manual NeoForge rewrite."

IMPORT_REPLACEMENTS = {
  "import net.minecraftforge.api.distmarker." => "import net.neoforged.api.distmarker.",
  "import net.minecraftforge.eventbus.api." => "import net.neoforged.bus.api.",
  "import net.minecraftforge.fml.loading." => "import net.neoforged.fml.loading.",
  "import net.minecraftforge.fml.config.ModConfig;" => "import net.neoforged.fml.config.ModConfig;",
  "import net.minecraftforge.fml.event.lifecycle." => "import net.neoforged.fml.event.lifecycle.",
  "import net.minecraftforge.fml.ModLoadingContext;" => "import net.neoforged.fml.ModLoadingContext;",
  "import net.minecraftforge.fml.ModContainer;" => "import net.neoforged.fml.ModContainer;",
  "import net.minecraftforge.fml.common.Mod;" => "import net.neoforged.fml.common.Mod;",
  "import net.minecraftforge.fml.common.Mod.EventBusSubscriber;" => "import net.neoforged.fml.common.EventBusSubscriber;",
  "import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;" => "import net.neoforged.fml.common.EventBusSubscriber.Bus;",
  "import net.minecraftforge.common.MinecraftForge;" => "import net.neoforged.neoforge.common.NeoForge;",
  "import net.minecraftforge.client.event." => "import net.neoforged.neoforge.client.event.",
  "import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;" => "import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;",
  "import net.minecraftforge.event.AnvilUpdateEvent;" => "import net.neoforged.neoforge.event.AnvilUpdateEvent;",
  "import net.minecraftforge.event.entity.EntityJoinLevelEvent;" => "import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;",
  "import net.minecraftforge.event.entity.EntityAttributeModificationEvent;" => "import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;",
  "import net.minecraftforge.event.entity.living.LivingDeathEvent;" => "import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;",
  "import net.minecraftforge.event.entity.living.LivingDropsEvent;" => "import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;",
  "import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;" => "import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;",
  "import net.minecraftforge.event.entity.living.LivingFallEvent;" => "import net.neoforged.neoforge.event.entity.living.LivingFallEvent;",
  "import net.minecraftforge.event.entity.living.LivingKnockBackEvent;" => "import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;",
  "import net.minecraftforge.event.entity.living.MobSpawnEvent;" => "import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;",
  "import net.minecraftforge.event.entity.player.AnvilRepairEvent;" => "import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;",
  "import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;" => "import net.neoforged.neoforge.event.entity.player.PlayerFlyableFallEvent;",
  "import net.minecraftforge.event.server.ServerAboutToStartEvent;" => "import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;",
  "import net.minecraftforge.registries.RegisterEvent;" => "import net.neoforged.neoforge.registries.RegisterEvent;",
  "import net.minecraftforge.registries.DataPackRegistryEvent;" => "import net.neoforged.neoforge.registries.DataPackRegistryEvent;",
  "import net.minecraftforge.common.crafting." => "import net.neoforged.neoforge.common.crafting.",
  "import net.minecraftforge.common.data." => "import net.neoforged.neoforge.common.data.",
  "import net.minecraftforge.data.event." => "import net.neoforged.neoforge.data.event.",
  "import net.minecraftforge.client.settings." => "import net.neoforged.neoforge.client.settings.",
  "import net.minecraftforge.common.Tags;" => "import net.neoforged.neoforge.common.Tags;",
  "import net.minecraftforge.client.extensions.common.IClientItemExtensions;" => "import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;"
}.freeze

TEXT_REPLACEMENTS = {
  "MinecraftForge.EVENT_BUS" => "NeoForge.EVENT_BUS",
  "@Mod.EventBusSubscriber" => "@EventBusSubscriber",
  "Mod.EventBusSubscriber.Bus." => "EventBusSubscriber.Bus.",
  "ForgeRegistries.Keys.ENTITY_TYPES" => "net.minecraft.core.registries.Registries.ENTITY_TYPE",
  "ForgeRegistries.Keys.STAT_TYPES" => "net.minecraft.core.registries.Registries.STAT_TYPE",
  "ForgeRegistries.Keys.RECIPE_SERIALIZERS" => "net.minecraft.core.registries.Registries.RECIPE_SERIALIZER",
  "ForgeRegistries.ITEMS" => "net.minecraft.core.registries.BuiltInRegistries.ITEM",
  "ForgeRegistries.ENTITY_TYPES" => "net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE",
  "ForgeRegistries.ATTRIBUTES" => "net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE",
  "ForgeRegistries.RECIPE_TYPES" => "net.minecraft.core.registries.BuiltInRegistries.RECIPE_TYPE",
  "ForgeRegistries.RECIPE_SERIALIZERS" => "net.minecraft.core.registries.BuiltInRegistries.RECIPE_SERIALIZER"
}.freeze

def add_todo(text, todo)
  return text if text.include?(todo)

  text.sub(/^(package [^;]+;\n\n)/, "\\1#{todo}\n")
end

changed = []

ROOT.glob("**/*.java").sort.each do |path|
  original = path.read
  text = original.dup

  IMPORT_REPLACEMENTS.each do |from, to|
    text.gsub!(from, to)
  end

  TEXT_REPLACEMENTS.each do |from, to|
    text.gsub!(from, to)
  end

  text.gsub!(/^@EventBusSubscriber\s*$/, '@EventBusSubscriber(modid = "slashblade")')
  text.gsub!("@EventBusSubscriber(value = Dist.CLIENT)", '@EventBusSubscriber(modid = "slashblade", value = Dist.CLIENT)')
  text.gsub!("@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)", '@EventBusSubscriber(modid = "slashblade", bus = EventBusSubscriber.Bus.MOD)')
  text.gsub!("@EventBusSubscriber(bus = Bus.MOD)", '@EventBusSubscriber(modid = "slashblade", bus = EventBusSubscriber.Bus.MOD)')
  text.gsub!(
    "@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)",
    '@EventBusSubscriber(modid = "slashblade", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)'
  )

  if text.include?("@EventBusSubscriber") && !text.include?("import net.neoforged.fml.common.EventBusSubscriber;")
    if text.include?("import net.neoforged.fml.common.Mod;")
      text.sub!("import net.neoforged.fml.common.Mod;\n", "import net.neoforged.fml.common.Mod;\nimport net.neoforged.fml.common.EventBusSubscriber;\n")
    else
      text = add_todo(text, "// TODO(neoforge-1.21.1): Add the EventBusSubscriber import after cleaning up this file's imports.")
    end
  end

  text = add_todo(text, TODO_NETWORK) if text.include?("net.minecraftforge.network.") || text.include?("DistExecutor") || text.include?("NetworkEvent")
  text = add_todo(text, TODO_ENCHANTMENTS) if text.include?("ForgeRegistries.ENCHANTMENTS")
  text = add_todo(text, TODO_FORGE_REGISTRIES) if text.include?("ForgeRegistries")
  text = add_todo(text, TODO_TICK_EVENTS) if text.include?("import net.minecraftforge.event.TickEvent;") || text.include?("TickEvent.")
  text = add_todo(text, TODO_DIST_EXECUTOR) if text.include?("DistExecutor")
  text = add_todo(text, TODO_FORGE_API) if text.include?("import net.minecraftforge.")

  if !text.include?("ForgeRegistries")
    text.gsub!("import net.minecraftforge.registries.ForgeRegistries;\n", "")
    text.gsub!("#{TODO_FORGE_REGISTRIES}\n", "")
  end

  next if text == original

  path.write(text)
  changed << path
end

puts "Updated #{changed.length} files"
changed.each { |path| puts path }

#!/usr/bin/env ruby
# frozen_string_literal: true

require "pathname"

ROOT = Pathname(ARGV[0] || "src/main/java")

unless ROOT.directory?
  warn "Directory not found: #{ROOT}"
  exit 1
end

def replace_all(text, replacements)
  replacements.each do |from, to|
    text.gsub!(from, to)
  end
  text
end

changed = []

ROOT.glob("**/*.java").sort.each do |path|
  original = path.read
  text = original.dup

  replace_all(text, {
    "import net.minecraftforge.registries.DeferredRegister;" => "import net.neoforged.neoforge.registries.DeferredRegister;",
    "import net.minecraftforge.registries.RegistryObject;" => "import net.neoforged.neoforge.registries.DeferredHolder;",
    "import net.minecraftforge.registries.RegistryBuilder;" => "import net.neoforged.neoforge.registries.RegistryBuilder;",
    "import net.minecraftforge.registries.IForgeRegistry;" => "import net.minecraft.core.Registry;",
    "DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.ITEM, " => "DeferredRegister.create(Registries.ITEM, ",
    "DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE, " => "DeferredRegister.create(Registries.ATTRIBUTE, ",
    "DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.RECIPE_TYPE, " => "DeferredRegister.create(Registries.RECIPE_TYPE, ",
    "DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.RECIPE_SERIALIZER, " => "DeferredRegister.create(Registries.RECIPE_SERIALIZER, ",
    "RegistryObject<Item>" => "DeferredHolder<Item, Item>",
    "RegistryObject<Attribute>" => "DeferredHolder<Attribute, Attribute>",
    "RegistryObject<CreativeModeTab>" => "DeferredHolder<CreativeModeTab, CreativeModeTab>",
    "RegistryObject<SlashArts>" => "DeferredHolder<SlashArts, SlashArts>",
    "RegistryObject<SpecialEffect>" => "DeferredHolder<SpecialEffect, SpecialEffect>",
    "RegistryObject<ComboState>" => "DeferredHolder<ComboState, ComboState>",
    "RegistryObject<RecipeSerializer<?>>" => "DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>>",
    "Supplier<IForgeRegistry<SlashArts>> REGISTRY" => "Registry<SlashArts> REGISTRY",
    "Supplier<IForgeRegistry<SpecialEffect>> REGISTRY" => "Registry<SpecialEffect> REGISTRY",
    "Supplier<IForgeRegistry<ComboState>> REGISTRY" => "Registry<ComboState> REGISTRY",
    "SlashArtsRegistry.REGISTRY.get()." => "SlashArtsRegistry.REGISTRY.",
    "SpecialEffectsRegistry.REGISTRY.get()." => "SpecialEffectsRegistry.REGISTRY.",
    "ComboStateRegistry.REGISTRY.get()." => "ComboStateRegistry.REGISTRY."
  })

  if text.include?("DeferredRegister.create(Registries.") && !text.include?("import net.minecraft.core.registries.Registries;")
    text.sub!(/(import .*?;\n)/, "\\1import net.minecraft.core.registries.Registries;\n")
  end

  if text.include?("DeferredHolder<") && !text.include?("import net.neoforged.neoforge.registries.DeferredHolder;")
    text.sub!(/(import .*?;\n)/, "\\1import net.neoforged.neoforge.registries.DeferredHolder;\n")
  end

  if text.include?("RegistryBuilder::new") && !text.include?("import net.neoforged.neoforge.registries.RegistryBuilder;")
    text.sub!(/(import .*?;\n)/, "\\1import net.neoforged.neoforge.registries.RegistryBuilder;\n")
  end

  if text.include?(" REGISTRY = ") && text.include?("RegistryBuilder::new") && !text.include?("import net.minecraft.core.Registry;")
    text.sub!(/(import .*?;\n)/, "\\1import net.minecraft.core.Registry;\n")
  end

  text.gsub!("// TODO(neoforge-1.21.1): Replace remaining ForgeRegistries references with BuiltInRegistries, Registries, or NeoForgeRegistries as appropriate.\n", "") unless text.include?("ForgeRegistries.")
  text.gsub!("// TODO(neoforge-1.21.1): This file still uses Forge-only APIs that need a manual NeoForge rewrite.\n", "") unless text.include?("import net.minecraftforge.")

  next if text == original

  path.write(text)
  changed << path
end

puts "Updated #{changed.length} files"
changed.each { |path| puts path }

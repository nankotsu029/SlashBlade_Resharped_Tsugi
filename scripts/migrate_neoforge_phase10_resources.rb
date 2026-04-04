#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "json"
require "pathname"

ROOT = Pathname.new(__dir__).join("..").expand_path
RESOURCE_ROOTS = [
  ROOT.join("src/main/resources"),
  ROOT.join("src/generated/resources")
].freeze

REPLACEMENTS = {
  "\"forge:conditions\"" => "\"neoforge:conditions\"",
  "\"type\": \"forge:mod_loaded\"" => "\"type\": \"neoforge:mod_loaded\"",
  "\"trigger\": \"inventory_changed\"" => "\"trigger\": \"minecraft:inventory_changed\"",
  "\"trigger\": \"recipe_unlocked\"" => "\"trigger\": \"minecraft:recipe_unlocked\""
}.freeze

def migrate_common_tag_paths
  ROOT.glob("src/{main,generated}/resources/data/forge/tags/**/*.json").each do |source|
    relative = source.relative_path_from(ROOT.join("src"))
    destination = ROOT.join("src", relative.to_s.sub(%r{\A(?:main|generated)/resources/data/forge/tags/}, "#{relative.each_filename.first}/resources/data/c/tags/"))
    FileUtils.mkdir_p(destination.dirname)
    FileUtils.mv(source, destination)
  end
end

def rewrite_json(path)
  original = path.read
  updated = original.dup

  REPLACEMENTS.each do |before, after|
    updated.gsub!(before, after)
  end

  updated.gsub!(/("tag"\s*:\s*")forge:/, '\1c:')
  return false if updated == original

  JSON.parse(updated)
  path.write(updated)
  true
end

migrate_common_tag_paths

changed = []
RESOURCE_ROOTS.each do |root|
  root.glob("**/*.json").sort.each do |path|
    changed << path if rewrite_json(path)
  end
end

puts "Updated #{changed.size} JSON files"

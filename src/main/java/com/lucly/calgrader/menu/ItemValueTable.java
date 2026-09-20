package com.lucly.calgrader.menu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

final class ItemValueTable {
    private ItemValueTable() {
    }

    static int get(Item item) {
        ItemStack stack = item.getDefaultInstance();
        String path = BuiltInRegistries.ITEM.getKey(item).getPath();

        int explicitValue = explicitValue(path);
        if (explicitValue > 0) {
            return explicitValue;
        }

        int equipmentValue = equipmentValue(path);
        if (equipmentValue > 0) {
            return equipmentValue;
        }

        int familyValue = familyValue(path);
        if (familyValue > 0) {
            return familyValue;
        }

        int rarityValue = switch (stack.getRarity()) {
            case EPIC -> 750;
            case RARE -> 180;
            case UNCOMMON -> 40;
            default -> 1;
        };
        if (rarityValue > 1) {
            return rarityValue;
        }
        if (stack.getMaxStackSize() == 1) {
            return 15;
        }
        if (stack.getMaxStackSize() <= 16) {
            return 3;
        }
        return 1;
    }

    private static int explicitValue(String path) {
        return switch (path) {
            // Basic resources and storage blocks.
            case "coal", "charcoal" -> 4;
            case "coal_block" -> 36;
            case "copper_ingot", "raw_copper" -> 8;
            case "raw_copper_block", "copper_block" -> 72;
            case "iron_ingot", "raw_iron" -> 20;
            case "raw_iron_block", "iron_block" -> 180;
            case "gold_ingot", "raw_gold" -> 25;
            case "raw_gold_block", "gold_block" -> 225;
            case "redstone" -> 3;
            case "redstone_block" -> 27;
            case "lapis_lazuli", "quartz" -> 5;
            case "lapis_block", "quartz_block" -> 45;
            case "amethyst_shard" -> 15;
            case "amethyst_block" -> 60;
            case "diamond" -> 250;
            case "diamond_block" -> 2_250;
            case "emerald" -> 180;
            case "emerald_block" -> 1_620;
            case "ancient_debris" -> 320;
            case "netherite_scrap" -> 350;
            case "netherite_ingot" -> 1_800;
            case "netherite_block" -> 16_200;

            // Ores are valued close to their expected primary drop.
            case "coal_ore", "deepslate_coal_ore" -> 4;
            case "copper_ore", "deepslate_copper_ore" -> 8;
            case "iron_ore", "deepslate_iron_ore" -> 20;
            case "gold_ore", "deepslate_gold_ore", "nether_gold_ore" -> 25;
            case "redstone_ore", "deepslate_redstone_ore" -> 12;
            case "lapis_ore", "deepslate_lapis_ore" -> 20;
            case "diamond_ore", "deepslate_diamond_ore" -> 250;
            case "emerald_ore", "deepslate_emerald_ore" -> 180;
            case "nether_quartz_ore" -> 5;

            // Rare exploration and progression items.
            case "echo_shard" -> 90;
            case "recovery_compass" -> 850;
            case "compass" -> 90;
            case "clock" -> 110;
            case "lodestone" -> 1_900;
            case "elytra" -> 2_500;
            case "dragon_egg" -> 10_000;
            case "nether_star" -> 1_100;
            case "totem_of_undying" -> 700;
            case "trident" -> 900;
            case "mace" -> 2_500;
            case "heavy_core" -> 1_600;
            case "heart_of_the_sea" -> 300;
            case "nautilus_shell" -> 45;
            case "conduit" -> 650;
            case "beacon" -> 1_500;
            case "shulker_shell" -> 100;
            case "shulker_box" -> 220;
            case "trial_key" -> 180;
            case "ominous_trial_key" -> 450;
            case "ominous_bottle" -> 60;
            case "breeze_rod" -> 35;
            case "wind_charge" -> 8;
            case "wither_skeleton_skull" -> 350;
            case "enchanted_golden_apple" -> 1_500;
            case "golden_apple" -> 220;
            case "golden_carrot" -> 35;

            // Mob drops and brewing components.
            case "ender_pearl" -> 30;
            case "ender_eye" -> 55;
            case "blaze_rod" -> 30;
            case "blaze_powder" -> 15;
            case "ghast_tear" -> 50;
            case "slime_ball" -> 8;
            case "magma_cream" -> 18;
            case "phantom_membrane" -> 25;
            case "rabbit_foot" -> 20;
            case "prismarine_shard" -> 3;
            case "prismarine_crystals" -> 8;
            case "scute" -> 10;
            case "armadillo_scute" -> 5;
            case "string", "feather", "flint", "bone" -> 2;
            case "gunpowder" -> 6;
            case "glowstone_dust" -> 5;
            case "spider_eye" -> 3;
            case "fermented_spider_eye" -> 8;

            // Redstone, workstations and utility blocks.
            case "redstone_torch" -> 5;
            case "repeater" -> 12;
            case "comparator" -> 30;
            case "observer" -> 25;
            case "piston" -> 25;
            case "sticky_piston" -> 35;
            case "dispenser" -> 25;
            case "dropper" -> 18;
            case "tnt" -> 30;
            case "redstone_lamp" -> 25;
            case "daylight_detector" -> 35;
            case "target" -> 15;
            case "hopper" -> 120;
            case "crafter" -> 120;
            case "chest" -> 8;
            case "trapped_chest" -> 12;
            case "barrel" -> 6;
            case "crafting_table" -> 4;
            case "furnace" -> 8;
            case "blast_furnace" -> 85;
            case "smoker" -> 20;
            case "stonecutter" -> 22;
            case "smithing_table" -> 50;
            case "grindstone" -> 20;
            case "brewing_stand" -> 65;
            case "enchanting_table" -> 650;
            case "anvil", "chipped_anvil", "damaged_anvil" -> 620;
            case "cauldron" -> 140;
            case "iron_bars" -> 5;
            case "chain" -> 22;
            case "iron_door" -> 40;
            case "iron_trapdoor" -> 80;
            case "heavy_weighted_pressure_plate" -> 40;
            case "light_weighted_pressure_plate" -> 50;
            case "ender_chest" -> 300;
            case "respawn_anchor" -> 500;
            case "end_crystal" -> 300;
            case "item_upgrader" -> 850;

            // Transport and frequently crafted equipment.
            case "minecart" -> 100;
            case "chest_minecart" -> 110;
            case "hopper_minecart" -> 220;
            case "furnace_minecart" -> 110;
            case "tnt_minecart" -> 130;
            case "rail" -> 8;
            case "powered_rail" -> 28;
            case "detector_rail", "activator_rail" -> 20;
            case "bucket" -> 60;
            case "water_bucket", "powder_snow_bucket", "milk_bucket" -> 65;
            case "lava_bucket" -> 75;
            case "shears" -> 40;
            case "flint_and_steel" -> 25;
            case "brush" -> 12;
            case "spyglass" -> 35;
            case "shield" -> 35;
            case "bow" -> 12;
            case "crossbow" -> 35;
            case "fishing_rod" -> 8;
            case "saddle" -> 80;
            case "name_tag" -> 50;
            case "lead" -> 15;
            case "turtle_helmet" -> 60;
            case "wolf_armor" -> 35;
            case "leather_horse_armor" -> 25;
            case "iron_horse_armor" -> 120;
            case "golden_horse_armor" -> 150;
            case "diamond_horse_armor" -> 1_200;

            // Books, maps and enchantment-related items.
            case "paper" -> 1;
            case "book" -> 4;
            case "bookshelf" -> 18;
            case "lectern" -> 25;
            case "map" -> 10;
            case "filled_map" -> 15;
            case "enchanted_book" -> 120;
            case "experience_bottle" -> 30;
            case "sculk_sensor" -> 40;
            case "calibrated_sculk_sensor" -> 55;
            case "sculk_catalyst" -> 60;
            case "sculk_shrieker" -> 80;
            default -> -1;
        };
    }

    private static int equipmentValue(String path) {
        String material;
        int unitValue;
        if (path.startsWith("wooden_")) {
            material = "wooden_";
            unitValue = 1;
        } else if (path.startsWith("stone_")) {
            material = "stone_";
            unitValue = 3;
        } else if (path.startsWith("iron_")) {
            material = "iron_";
            unitValue = 20;
        } else if (path.startsWith("golden_")) {
            material = "golden_";
            unitValue = 25;
        } else if (path.startsWith("diamond_")) {
            material = "diamond_";
            unitValue = 250;
        } else if (path.startsWith("leather_")) {
            material = "leather_";
            unitValue = 5;
        } else if (path.startsWith("chainmail_")) {
            material = "chainmail_";
            unitValue = 20;
        } else if (path.startsWith("netherite_")) {
            String diamondPath = "diamond_" + path.substring("netherite_".length());
            int diamondValue = equipmentValue(diamondPath);
            return diamondValue > 0 ? diamondValue + 1_800 : -1;
        } else {
            return -1;
        }

        String part = path.substring(material.length());
        int materialCount = switch (part) {
            case "shovel" -> 1;
            case "sword", "hoe" -> 2;
            case "pickaxe", "axe" -> 3;
            case "boots" -> 4;
            case "helmet" -> 5;
            case "leggings" -> 7;
            case "chestplate" -> 8;
            default -> -1;
        };
        if (materialCount < 0) {
            return -1;
        }

        boolean tool = part.equals("shovel") || part.equals("sword") || part.equals("hoe")
                || part.equals("pickaxe") || part.equals("axe");
        return materialCount * unitValue + (tool ? 3 : 0);
    }

    private static int familyValue(String path) {
        if (path.contains("copper")) {
            if (path.endsWith("_slab")) return 36;
            if (path.endsWith("_door") || path.endsWith("_trapdoor") || path.endsWith("_grate")) return 24;
            return 72;
        }
        if (path.endsWith("_log") || path.endsWith("_wood") || path.endsWith("_stem")
                || path.endsWith("_hyphae") || path.startsWith("stripped_")) {
            return 1;
        }
        if (path.endsWith("_planks") || path.endsWith("_leaves") || path.endsWith("_sapling")) {
            return 1;
        }
        if (path.endsWith("_wool")) return 3;
        if (path.endsWith("_carpet")) return 1;
        if (path.endsWith("_terracotta") || path.endsWith("_concrete")) return 2;
        if (path.endsWith("_concrete_powder")) return 1;
        if (path.endsWith("_stained_glass")) return 2;
        if (path.endsWith("_stained_glass_pane")) return 1;
        if (path.startsWith("music_disc_")) return 150;
        if (path.endsWith("_pottery_sherd")) return 30;
        if (path.endsWith("_banner_pattern")) return 80;
        if (path.endsWith("_smithing_template")) return 400;
        if (path.endsWith("_chest_boat") || path.endsWith("_chest_raft")) return 15;
        if (path.endsWith("_boat") || path.endsWith("_raft")) return 8;
        if (path.startsWith("cooked_")) return 5;
        return -1;
    }
}

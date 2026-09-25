package com.lucly.calgrader.menu;

import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class RewardPolicy {
    private static final Set<String> UNOBTAINABLE = Set.of(
            "air", "barrier", "bedrock", "budding_amethyst", "chain_command_block",
            "chorus_plant", "command_block", "command_block_minecart", "debug_stick", "end_portal_frame",
            "frogspawn", "jigsaw", "knowledge_book", "light", "petrified_oak_slab", "player_head",
            "reinforced_deepslate", "repeating_command_block", "spawner", "structure_block",
            "structure_void", "suspicious_gravel", "suspicious_sand", "test_block",
            "test_instance_block", "trial_spawner", "vault");

    private RewardPolicy() {
    }

    public static boolean isAllowed(Item item) {
        return item != Items.AIR && isAllowedId(BuiltInRegistries.ITEM.getKey(item));
    }

    static boolean isAllowedId(ResourceLocation id) {
        String path = id.getPath();
        return id.getNamespace().equals("minecraft")
                && !UNOBTAINABLE.contains(path)
                && !path.startsWith("infested_")
                && !path.endsWith("_spawn_egg");
    }
}

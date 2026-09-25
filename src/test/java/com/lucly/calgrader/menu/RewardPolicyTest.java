package com.lucly.calgrader.menu;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class RewardPolicyTest {
    @Test
    void acceptsNormalVanillaRewards() {
        assertTrue(RewardPolicy.isAllowedId(ResourceLocation.parse("minecraft:diamond")));
        assertTrue(RewardPolicy.isAllowedId(ResourceLocation.parse("minecraft:red_bed")));
    }

    @Test
    void rejectsHiddenAndModdedRewards() {
        assertFalse(RewardPolicy.isAllowedId(ResourceLocation.parse("minecraft:command_block")));
        assertFalse(RewardPolicy.isAllowedId(ResourceLocation.parse("minecraft:barrier")));
        assertFalse(RewardPolicy.isAllowedId(ResourceLocation.parse("minecraft:zombie_spawn_egg")));
        assertFalse(RewardPolicy.isAllowedId(ResourceLocation.parse("calgrader:item_upgrader")));
        assertFalse(RewardPolicy.isAllowedId(ResourceLocation.parse("some_mod:diamond")));
    }
}

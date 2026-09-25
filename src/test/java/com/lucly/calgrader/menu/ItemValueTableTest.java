package com.lucly.calgrader.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class ItemValueTableTest {
    @Test
    void usesFullItemIdForPrices() {
        assertEquals(250, ItemValueTable.get(ResourceLocation.parse("minecraft:diamond"), Items.DIAMOND.getDefaultInstance()));
        assertEquals(850, ItemValueTable.get(ResourceLocation.parse("minecraft:recovery_compass"), Items.RECOVERY_COMPASS.getDefaultInstance()));
        assertEquals(0, ItemValueTable.get(ResourceLocation.parse("some_mod:diamond"), Items.DIAMOND.getDefaultInstance()));
    }
}

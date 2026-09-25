package com.lucly.calgrader.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class UpgraderMenuTest {
    @Test
    void selectionButtonsRejectForbiddenRewards() {
        assertEquals(Items.DIAMOND, UpgraderMenu.selectableReward(buttonFor(Items.DIAMOND)));
        assertEquals(Items.AIR, UpgraderMenu.selectableReward(buttonFor(Items.COMMAND_BLOCK)));
        assertEquals(Items.AIR, UpgraderMenu.selectableReward(buttonFor(Items.BARRIER)));
        assertEquals(Items.AIR, UpgraderMenu.selectableReward(UpgraderMenu.BUTTON_SELECT_ITEM_BASE - 1));
        assertEquals(Items.AIR, UpgraderMenu.selectableReward(UpgraderMenu.BUTTON_AMOUNT_BASE));
    }

    private static int buttonFor(Item item) {
        return UpgraderMenu.BUTTON_SELECT_ITEM_BASE + BuiltInRegistries.ITEM.getId(item);
    }
}

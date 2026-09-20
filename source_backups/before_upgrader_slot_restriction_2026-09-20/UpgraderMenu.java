package com.lucly.calgrader.menu;

import com.lucly.calgrader.block.ItemUpgraderBlockEntity;
import com.lucly.calgrader.calgrader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class UpgraderMenu extends AbstractContainerMenu {
    public static final int BUTTON_UPGRADE = 0;
    public static final int BUTTON_FINISH_ROLL = 1;
    public static final int BUTTON_SELECT_ITEM_BASE = 10_000;
    public static final int BUTTON_AMOUNT_BASE = 20_000;

    private static final int DATA_SELECTED_ITEM = 0;
    private static final int DATA_AMOUNT = 1;
    private static final int DATA_ROLL_POSITION = 2;
    private static final int DATA_ROLL_NONCE = 3;
    private static final int DATA_PENDING = 4;
    private static final int DATA_DISPLAY_CHANCE = 5;

    private final Container upgrader;
    private final ContainerData data;
    private boolean pendingSuccess;
    private Item pendingReward = Items.AIR;
    private int pendingRewardAmount;

    public UpgraderMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(2), new SimpleContainerData(6));
    }

    public UpgraderMenu(int containerId, Inventory playerInventory, Container upgrader) {
        this(containerId, playerInventory, upgrader, new SimpleContainerData(6));
    }

    private UpgraderMenu(int containerId, Inventory playerInventory, Container upgrader, ContainerData data) {
        super(calgrader.UPGRADER_MENU.get(), containerId);
        checkContainerSize(upgrader, 2);
        checkContainerDataCount(data, 6);
        this.upgrader = upgrader;
        this.data = data;

        this.addSlot(new Slot(upgrader, ItemUpgraderBlockEntity.TARGET_SLOT, 47, 62));

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 89 + col * 18, 181 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 89 + col * 18, 239));
        }

        this.data.set(DATA_SELECTED_ITEM, BuiltInRegistries.ITEM.getId(Items.DIAMOND));
        this.data.set(DATA_AMOUNT, 1);
        this.data.set(DATA_ROLL_POSITION, 0);
        this.data.set(DATA_ROLL_NONCE, 0);
        this.data.set(DATA_PENDING, 0);
        this.data.set(DATA_DISPLAY_CHANCE, 0);
        this.addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return upgrader.stillValid(player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_UPGRADE) {
            startUpgrade(player, player.getRandom());
            return true;
        }

        if (id == BUTTON_FINISH_ROLL) {
            finishPendingRoll(player);
            return true;
        }

        if (hasPendingRoll()) {
            return false;
        }

        if (id > BUTTON_AMOUNT_BASE && id <= BUTTON_AMOUNT_BASE + 64) {
            data.set(DATA_AMOUNT, id - BUTTON_AMOUNT_BASE);
            return true;
        }

        if (id >= BUTTON_SELECT_ITEM_BASE && id < BUTTON_AMOUNT_BASE) {
            Item item = BuiltInRegistries.ITEM.byId(id - BUTTON_SELECT_ITEM_BASE);
            if (item != Items.AIR) {
                data.set(DATA_SELECTED_ITEM, BuiltInRegistries.ITEM.getId(item));
                return true;
            }
        }

        return false;
    }

    public Item getSelectedItem() {
        Item item = BuiltInRegistries.ITEM.byId(data.get(DATA_SELECTED_ITEM));
        return item == Items.AIR ? Items.DIAMOND : item;
    }

    public ItemStack getSelectedItemStack() {
        return new ItemStack(getSelectedItem());
    }

    public int getSelectedAmount() {
        return Math.clamp(data.get(DATA_AMOUNT), 1, 64);
    }

    public int getRollPosition() {
        return Math.clamp(data.get(DATA_ROLL_POSITION), 0, 9_999);
    }

    public int getRollNonce() {
        return data.get(DATA_ROLL_NONCE);
    }

    public boolean hasPendingRoll() {
        return data.get(DATA_PENDING) != 0;
    }

    public int getOfferValue() {
        ItemStack offer = upgrader.getItem(ItemUpgraderBlockEntity.TARGET_SLOT);
        return offer.isEmpty() ? 0 : getItemValue(offer.getItem()) * offer.getCount();
    }

    public int getRewardTotalValue() {
        return getRewardUnitValue() * getSelectedAmount();
    }

    public int getRewardUnitValue() {
        return getItemValue(getSelectedItem());
    }

    /** Returns the real roll chance in hundredths of a percent (0..10000). */
    public int getSuccessChanceBasisPoints() {
        int rewardValue = getRewardTotalValue();
        if (rewardValue <= 0) {
            return 0;
        }

        long chance = (long) getOfferValue() * 10_000L / rewardValue;
        return (int) Math.clamp(chance, 0L, 9_500L);
    }

    public int getDisplayedChanceBasisPoints() {
        if (hasPendingRoll() || getOfferValue() == 0) {
            return Math.clamp(data.get(DATA_DISPLAY_CHANCE), 0, 9_500);
        }
        return getSuccessChanceBasisPoints();
    }

    public void setLocalSelectedItem(Item item) {
        data.set(DATA_SELECTED_ITEM, BuiltInRegistries.ITEM.getId(item));
    }

    public void setLocalAmount(int amount) {
        data.set(DATA_AMOUNT, Math.clamp(amount, 1, 64));
    }

    private void startUpgrade(Player player, RandomSource random) {
        if (hasPendingRoll()) {
            return;
        }

        ItemStack offer = upgrader.getItem(ItemUpgraderBlockEntity.TARGET_SLOT);
        int chance = getSuccessChanceBasisPoints();
        if (offer.isEmpty() || chance <= 0) {
            return;
        }

        int offeredCount = offer.getCount();
        upgrader.removeItem(ItemUpgraderBlockEntity.TARGET_SLOT, offeredCount);

        int rollPosition = random.nextInt(10_000);
        pendingSuccess = rollPosition < chance;
        pendingReward = getSelectedItem();
        pendingRewardAmount = getSelectedAmount();
        data.set(DATA_DISPLAY_CHANCE, chance);
        data.set(DATA_ROLL_POSITION, rollPosition);
        data.set(DATA_ROLL_NONCE, data.get(DATA_ROLL_NONCE) + 1);
        data.set(DATA_PENDING, 1);

        upgrader.setChanged();
    }

    private void finishPendingRoll(Player player) {
        if (!hasPendingRoll()) {
            return;
        }

        if (pendingSuccess && pendingReward != Items.AIR && pendingRewardAmount > 0) {
            giveReward(player, pendingReward, pendingRewardAmount);
            player.getInventory().setChanged();
        }

        pendingSuccess = false;
        pendingReward = Items.AIR;
        pendingRewardAmount = 0;
        data.set(DATA_PENDING, 0);
    }

    @Override
    public void removed(Player player) {
        if (!player.level().isClientSide()) {
            finishPendingRoll(player);
        }
        super.removed(player);
    }

    private static void giveReward(Player player, Item item, int amount) {
        int remaining = amount;
        int maxStackSize = item.getDefaultMaxStackSize();
        while (remaining > 0) {
            ItemStack reward = new ItemStack(item, Math.min(remaining, maxStackSize));
            remaining -= reward.getCount();
            if (!player.addItem(reward) && !reward.isEmpty()) {
                player.drop(reward, false);
            }
        }
    }

    private static int getItemValue(Item item) {
        return ItemValueTable.get(item);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copied = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copied = stack.copy();
            if (index == 0) {
                if (!this.moveItemStackTo(stack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return copied;
    }
}

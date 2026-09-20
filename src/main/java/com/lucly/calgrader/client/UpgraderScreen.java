package com.lucly.calgrader.client;

import com.mojang.math.Axis;
import com.lucly.calgrader.calgrader;
import com.lucly.calgrader.menu.UpgraderMenu;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class UpgraderScreen extends AbstractContainerScreen<UpgraderMenu> {
    private static final int SELECTOR_X = 8;
    private static final int SELECTOR_Y = 22;
    private static final int SELECTOR_WIDTH = 324;
    private static final int SELECTOR_HEIGHT = 216;
    private static final int SELECTOR_GRID_X = 22;
    private static final int SELECTOR_GRID_Y = 58;
    private static final int SELECTOR_COLUMNS = 15;
    private static final int SELECTOR_ROWS = 7;
    private static final int SPIN_DURATION_TICKS = 80;
    private static final double FULL_TURN = Math.PI * 2.0D;
    private static final Set<String> NON_SURVIVAL_ITEMS = Set.of(
            "air", "barrier", "bedrock", "budding_amethyst", "chain_command_block",
            "chorus_plant", "command_block", "command_block_minecart", "debug_stick", "end_portal_frame",
            "frogspawn", "jigsaw", "knowledge_book", "light", "petrified_oak_slab", "player_head",
            "reinforced_deepslate",
            "repeating_command_block", "spawner", "structure_block", "structure_void",
            "suspicious_gravel", "suspicious_sand", "test_block", "test_instance_block",
            "trial_spawner", "vault");
    private static final int PANEL = 0xFFC6C6C6;
    private static final int PANEL_DARK = 0xFF555555;
    private static final int SLOT = 0xFF373737;
    private static final int SLOT_INNER = 0xFF1D1D1D;
    private static final int GOLD = 0xFFFFC22E;
    private static final int ORANGE = 0xFFFF8A16;

    private final List<Item> allItemChoices = new ArrayList<>();
    private final List<Item> itemChoices = new ArrayList<>();
    private boolean selectorOpen;
    private int selectorScroll;
    private Button upgradeButton;
    private EditBox searchBox;
    private int observedRollNonce;
    private int spinTicks;
    private boolean spinning;
    private boolean finishSent;
    private double cursorAngle = -Math.PI / 2.0D;
    private double spinStartAngle;
    private double spinTargetAngle;
    private int lastSoundSegment;
    private boolean currentRollWon;

    public UpgraderScreen(UpgraderMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 340;
        this.imageHeight = 262;
        this.inventoryLabelX = 89;
        this.inventoryLabelY = 168;
    }

    @Override
    protected void init() {
        super.init();
        allItemChoices.clear();
        BuiltInRegistries.ITEM.stream()
                .filter(UpgraderScreen::isAvailableInSurvival)
                .sorted(Comparator.comparing(item -> item.getDescription().getString()))
                .forEach(allItemChoices::add);
        applySearch("");

        observedRollNonce = menu.getRollNonce();
        upgradeButton = addRenderableWidget(Button.builder(Component.translatable("button.calgrader.upgrade"), button -> sendButton(UpgraderMenu.BUTTON_UPGRADE))
                .bounds(this.leftPos + 104, this.topPos + 132, 132, 24)
                .build());

        searchBox = addRenderableWidget(new EditBox(
                this.font,
                this.leftPos + 18,
                this.topPos + 30,
                304,
                18,
                Component.translatable("label.calgrader.search")));
        searchBox.setMaxLength(64);
        searchBox.setHint(Component.translatable("label.calgrader.search"));
        searchBox.setResponder(this::applySearch);
        searchBox.setVisible(false);
    }

    private void applySearch(String text) {
        String query = text.strip().toLowerCase(Locale.ROOT);
        itemChoices.clear();
        for (Item item : allItemChoices) {
            String name = item.getDescription().getString().toLowerCase(Locale.ROOT);
            String itemId = BuiltInRegistries.ITEM.getKey(item).toString().toLowerCase(Locale.ROOT);
            if (query.isEmpty() || name.contains(query) || itemId.contains(query)) {
                itemChoices.add(item);
            }
        }
        selectorScroll = 0;
    }

    private void setSelectorOpen(boolean open) {
        selectorOpen = open;
        if (searchBox != null) {
            searchBox.setVisible(open);
            searchBox.setFocused(open);
            this.setFocused(open ? searchBox : null);
        }
    }

    private static boolean isAvailableInSurvival(Item item) {
        String itemId = BuiltInRegistries.ITEM.getKey(item).getPath();
        return item != Items.AIR
                && item != calgrader.ITEM_UPGRADER_ITEM.get()
                && !NON_SURVIVAL_ITEMS.contains(itemId)
                && !itemId.startsWith("infested_")
                && !itemId.endsWith("_spawn_egg");
    }

    private void sendButton(int buttonId) {
        if (Minecraft.getInstance().gameMode != null) {
            Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        drawPanel(graphics, left, top, imageWidth, imageHeight);
        drawSection(graphics, left + 12, top + 32, 88, 96);
        drawSection(graphics, left + 240, top + 32, 88, 96);

        drawSlot(graphics, left + 46, top + 61);
        drawSelectorSlot(graphics, left + 276, top + 49);
        graphics.renderItem(menu.getSelectedItemStack(), left + 277, top + 50);

        drawSlider(graphics, left + 250, top + 82, 68);
        drawWheel(graphics, left + 170, top + 74, partialTick);

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                drawSlot(graphics, left + 88 + col * 18, top + 180 + row * 18);
            }
        }
        for (int col = 0; col < 9; ++col) {
            drawSlot(graphics, left + 88 + col * 18, top + 238);
        }

    }

    private static void drawPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xFF202020);
        graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, 0xFFFFFFFF);
        graphics.fill(x + 5, y + 5, x + width - 5, y + height - 5, PANEL_DARK);
        graphics.fill(x + 7, y + 7, x + width - 7, y + height - 7, PANEL);
    }

    private static void drawSection(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xFF777777);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFFD8D8D8);
        graphics.fill(x + 3, y + 3, x + width - 3, y + height - 3, 0xFFBEBEBE);
    }

    private void drawWheel(GuiGraphics graphics, int centerX, int centerY, float partialTick) {
        int chance = menu.getDisplayedChanceBasisPoints();

        fillCircle(graphics, centerX, centerY, 47, 0xFF171717);
        fillCircle(graphics, centerX, centerY, 39, 0xFF242424);

        for (int i = 0; i < 200; i++) {
            double angle = -Math.PI / 2.0D + i * FULL_TURN / 200.0D;
            int x = centerX + (int) Math.round(Math.cos(angle) * 52.0D);
            int y = centerY + (int) Math.round(Math.sin(angle) * 52.0D);
            int color = i * 10_000 / 200 < chance ? ORANGE : 0xFF5A5A5A;
            graphics.fill(x - 2, y - 2, x + 3, y + 3, color);
        }

        double cursor = getRenderedCursorAngle(partialTick);
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY, 0.0F);
        graphics.pose().mulPose(Axis.ZP.rotation((float) cursor));
        for (int step = 0; step <= 12; step++) {
            int halfHeight = step / 2;
            graphics.fill(49 + step, -halfHeight, 50 + step, halfHeight + 1, 0xFF202020);
        }
        for (int step = 0; step <= 9; step++) {
            int halfHeight = step / 2;
            graphics.fill(52 + step, -halfHeight, 53 + step, halfHeight + 1, 0xFFFFFFFF);
        }
        graphics.pose().popPose();
    }

    private double getRenderedCursorAngle(float partialTick) {
        if (!spinning) {
            return cursorAngle;
        }

        double progress = Math.clamp((spinTicks + partialTick) / SPIN_DURATION_TICKS, 0.0D, 1.0D);
        double eased = 1.0D - Math.pow(1.0D - progress, 3.0D);
        return spinStartAngle + (spinTargetAngle - spinStartAngle) * eased;
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        int nonce = menu.getRollNonce();
        if (nonce != observedRollNonce) {
            observedRollNonce = nonce;
            beginSpin(menu.getRollPosition());
        }

        if (spinning) {
            spinTicks++;
            if (spinTicks < SPIN_DURATION_TICKS) {
                int soundSegment = (int) Math.floor(getRenderedCursorAngle(0.0F) * 48.0D / FULL_TURN);
                if (soundSegment != lastSoundSegment) {
                    lastSoundSegment = soundSegment;
                    playSpinTick();
                }
            } else {
                cursorAngle = normalizeAngle(spinTargetAngle);
                spinning = false;
                if (!finishSent) {
                    finishSent = true;
                    currentRollWon = menu.getRollPosition() < menu.getDisplayedChanceBasisPoints();
                    playResultSound();
                    sendButton(UpgraderMenu.BUTTON_FINISH_ROLL);
                }
            }
        }

        if (upgradeButton != null) {
            upgradeButton.active = !menu.hasPendingRoll() && menu.getOfferValue() > 0;
        }
    }

    private void beginSpin(int rollPosition) {
        double target = -Math.PI / 2.0D + rollPosition * FULL_TURN / 10_000.0D;
        double current = normalizeAngle(cursorAngle);
        double distance = normalizeAngle(target - current);
        spinStartAngle = current;
        spinTargetAngle = current + FULL_TURN * 5.0D + distance;
        spinTicks = 0;
        spinning = true;
        finishSent = false;
        lastSoundSegment = (int) Math.floor(current * 48.0D / FULL_TURN);
        setSelectorOpen(false);
    }

    private void playSpinTick() {
        float progress = Math.clamp((float) spinTicks / SPIN_DURATION_TICKS, 0.0F, 1.0F);
        float pitch = 0.85F + progress * 0.45F;
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_HAT.value(), pitch, 0.22F));
    }

    private void playResultSound() {
        if (currentRollWon) {
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.0F, 0.8F));
        } else {
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 0.9F, 0.8F));
        }
    }

    private static double normalizeAngle(double angle) {
        double normalized = angle % FULL_TURN;
        return normalized < 0.0D ? normalized + FULL_TURN : normalized;
    }

    private static void fillCircle(GuiGraphics graphics, int centerX, int centerY, int radius, int color) {
        for (int y = -radius; y <= radius; y++) {
            int halfWidth = (int) Math.sqrt(radius * radius - y * y);
            graphics.fill(centerX - halfWidth, centerY + y, centerX + halfWidth + 1, centerY + y + 1, color);
        }
    }

    private void drawSlider(GuiGraphics graphics, int x, int y, int width) {
        graphics.fill(x, y, x + width, y + 4, 0xFF555555);
        int knobX = x + (menu.getSelectedAmount() - 1) * width / 63;
        graphics.fill(knobX - 2, y - 4, knobX + 3, y + 8, GOLD);
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, SLOT);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, PANEL_DARK);
        graphics.fill(x + 2, y + 2, x + 16, y + 16, SLOT);
    }

    private static void drawSelectorSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 2, y - 2, x + 20, y + 20, 0xFF282828);
        graphics.fill(x, y, x + 18, y + 18, SLOT_INNER);
    }

    private void drawSelector(GuiGraphics graphics) {
        int x = this.leftPos + SELECTOR_X;
        int y = this.topPos + SELECTOR_Y;
        graphics.fill(x, y, x + SELECTOR_WIDTH, y + SELECTOR_HEIGHT, 0xFF202020);
        graphics.fill(x + 2, y + 2, x + SELECTOR_WIDTH - 2, y + SELECTOR_HEIGHT - 2, 0xFFE0E0E0);
        graphics.fill(x + 6, y + 32, x + SELECTOR_WIDTH - 6, y + 174, 0xFF777777);

        int visibleItems = SELECTOR_COLUMNS * SELECTOR_ROWS;
        int start = selectorScroll * SELECTOR_COLUMNS;
        for (int i = 0; i < visibleItems && start + i < itemChoices.size(); i++) {
            int col = i % SELECTOR_COLUMNS;
            int row = i / SELECTOR_COLUMNS;
            int slotX = this.leftPos + SELECTOR_GRID_X + col * 18;
            int slotY = this.topPos + SELECTOR_GRID_Y + row * 19;
            Item item = itemChoices.get(start + i);
            graphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF202020);
            graphics.renderItem(new ItemStack(item), slotX + 1, slotY + 1);
            if (item == menu.getSelectedItem()) {
                graphics.renderOutline(slotX, slotY, 18, 18, GOLD);
            }
        }
        if (itemChoices.isEmpty()) {
            Component emptyText = Component.translatable("label.calgrader.nothing_found");
            graphics.drawString(this.font, emptyText, x + SELECTOR_WIDTH / 2 - this.font.width(emptyText) / 2, y + 96, 0xFFE0E0E0, false);
        }

        int totalRows = Math.max(1, (itemChoices.size() + SELECTOR_COLUMNS - 1) / SELECTOR_COLUMNS);
        int maxScroll = Math.max(0, totalRows - SELECTOR_ROWS);
        int trackX = x + SELECTOR_WIDTH - 12;
        int trackY = this.topPos + SELECTOR_GRID_Y - 2;
        int trackHeight = SELECTOR_ROWS * 19;
        if (maxScroll > 0) {
            graphics.fill(trackX, trackY, trackX + 5, trackY + trackHeight, 0xFF4A4A4A);
            int thumbHeight = Math.min(trackHeight, Math.max(14, trackHeight * SELECTOR_ROWS / totalRows));
            int thumbY = trackY + selectorScroll * (trackHeight - thumbHeight) / maxScroll;
            graphics.fill(trackX, thumbY, trackX + 4, thumbY + thumbHeight, GOLD);
        }

        Item selected = menu.getSelectedItem();
        int footerY = y + 181;
        graphics.fill(x + 6, footerY, x + SELECTOR_WIDTH - 6, y + SELECTOR_HEIGHT - 6, 0xFFBEBEBE);
        drawSelectorSlot(graphics, x + 12, footerY + 5);
        graphics.renderItem(new ItemStack(selected), x + 13, footerY + 6);
        String selectedName = selected.getDescription().getString();
        String shortName = this.font.plainSubstrByWidth(selectedName, 135);
        if (!shortName.equals(selectedName)) {
            shortName += "...";
        }
        graphics.drawString(this.font, shortName, x + 40, footerY + 5, 0xFF303030, false);
        Component count = Component.translatable("label.calgrader.item_count", itemChoices.size(), allItemChoices.size());
        graphics.drawString(this.font, count, x + SELECTOR_WIDTH - 10 - this.font.width(count), footerY + 5, 0xFF505050, false);
        graphics.drawString(this.font, Component.translatable("label.calgrader.selected_value", menu.getRewardUnitValue()), x + 40, footerY + 16, 0xFF505050, false);
    }

    private Item getHoveredSelectorItem(double mouseX, double mouseY) {
        int gridX = this.leftPos + SELECTOR_GRID_X;
        int gridY = this.topPos + SELECTOR_GRID_Y;
        int gridWidth = SELECTOR_COLUMNS * 18;
        int gridHeight = SELECTOR_ROWS * 19;
        if (!selectorOpen || !isInside(mouseX, mouseY, gridX, gridY, gridWidth, gridHeight)) {
            return null;
        }

        int col = ((int) mouseX - gridX) / 18;
        int row = ((int) mouseY - gridY) / 19;
        int index = selectorScroll * SELECTOR_COLUMNS + row * SELECTOR_COLUMNS + col;
        return col < SELECTOR_COLUMNS && row < SELECTOR_ROWS && index < itemChoices.size() ? itemChoices.get(index) : null;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 10, 11, 0x404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);

        String chance = String.format(Locale.ROOT, "%.2f%%", menu.getDisplayedChanceBasisPoints() / 100.0D);
        graphics.pose().pushPose();
        graphics.pose().scale(2.0F, 2.0F, 1.0F);
        graphics.drawString(this.font, chance, 85 - this.font.width(chance) / 2, 31, GOLD, false);
        graphics.pose().popPose();
        drawCentered(graphics, Component.translatable("label.calgrader.win_chance").getString(), 170, 82, GOLD);

        drawCentered(graphics, Component.translatable("label.calgrader.offer").getString(), 56, 41, 0x404040);
        drawCentered(graphics, Component.translatable("label.calgrader.value", menu.getOfferValue()).getString(), 56, 91, 0x404040);

        drawCentered(graphics, Component.translatable("label.calgrader.reward").getString(), 284, 37, 0x404040);
        drawCentered(graphics, Component.translatable("label.calgrader.amount", menu.getSelectedAmount()).getString(), 284, 96, 0x404040);
        drawCentered(graphics, Component.translatable("label.calgrader.unit_value", menu.getRewardUnitValue()).getString(), 284, 107, 0x404040);
        drawCentered(graphics, Component.translatable("label.calgrader.total_value", menu.getRewardTotalValue()).getString(), 284, 118, 0x404040);
    }

    private void drawCentered(GuiGraphics graphics, String text, int x, int y, int color) {
        graphics.drawString(this.font, text, x - this.font.width(text) / 2, y, color, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int left = this.leftPos;
        int top = this.topPos;
        if (button == 0) {
            if (menu.hasPendingRoll()) {
                setSelectorOpen(false);
                return super.mouseClicked(mouseX, mouseY, button);
            }

            if (selectorOpen && isInside(mouseX, mouseY, left + 18, top + 30, 304, 18)) {
                return super.mouseClicked(mouseX, mouseY, button);
            }

            int gridWidth = SELECTOR_COLUMNS * 18;
            int gridHeight = SELECTOR_ROWS * 19;
            if (selectorOpen && isInside(mouseX, mouseY, left + SELECTOR_GRID_X, top + SELECTOR_GRID_Y, gridWidth, gridHeight)) {
                int col = ((int) mouseX - (left + SELECTOR_GRID_X)) / 18;
                int row = ((int) mouseY - (top + SELECTOR_GRID_Y)) / 19;
                int index = selectorScroll * SELECTOR_COLUMNS + row * SELECTOR_COLUMNS + col;
                if (col >= 0 && col < SELECTOR_COLUMNS && row >= 0 && row < SELECTOR_ROWS && index >= 0 && index < itemChoices.size()) {
                    Item item = itemChoices.get(index);
                    menu.setLocalSelectedItem(item);
                    sendButton(UpgraderMenu.BUTTON_SELECT_ITEM_BASE + BuiltInRegistries.ITEM.getId(item));
                    return true;
                }
            }

            if (selectorOpen && isInside(mouseX, mouseY, left + SELECTOR_X, top + SELECTOR_Y, SELECTOR_WIDTH, SELECTOR_HEIGHT)) {
                return true;
            }

            if (isInside(mouseX, mouseY, left + 276, top + 49, 18, 18)) {
                setSelectorOpen(!selectorOpen);
                return true;
            }

            if (isInside(mouseX, mouseY, left + 246, top + 74, 76, 20)) {
                updateAmount(mouseX, left + 250, 68);
                return true;
            }

            if (selectorOpen) {
                setSelectorOpen(false);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!menu.hasPendingRoll() && button == 0 && isInside(mouseX, mouseY, this.leftPos + 246, this.topPos + 72, 76, 24)) {
            updateAmount(mouseX, this.leftPos + 250, 68);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void updateAmount(double mouseX, int sliderX, int sliderWidth) {
        int amount = 1 + (int) Math.round(Math.clamp(mouseX - sliderX, 0.0D, sliderWidth) * 63.0D / sliderWidth);
        if (amount != menu.getSelectedAmount()) {
            menu.setLocalAmount(amount);
            sendButton(UpgraderMenu.BUTTON_AMOUNT_BASE + amount);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (selectorOpen) {
            int totalRows = (itemChoices.size() + SELECTOR_COLUMNS - 1) / SELECTOR_COLUMNS;
            int maxScroll = Math.max(0, totalRows - SELECTOR_ROWS);
            selectorScroll = Math.clamp(selectorScroll - (int) Math.signum(scrollY), 0, maxScroll);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (selectorOpen) {
            if (keyCode == 256) {
                setSelectorOpen(false);
                return true;
            }
            searchBox.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (selectorOpen) {
            return searchBox.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (selectorOpen) {
            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, 0.0F, 400.0F);
            drawSelector(graphics);
            searchBox.render(graphics, mouseX, mouseY, partialTick);
            Item hoveredItem = getHoveredSelectorItem(mouseX, mouseY);
            if (hoveredItem != null) {
                graphics.renderTooltip(this.font, new ItemStack(hoveredItem), mouseX, mouseY);
            }
            graphics.pose().popPose();
        } else {
            this.renderTooltip(graphics, mouseX, mouseY);
        }
    }
}

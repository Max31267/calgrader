package com.lucly.calgrader;

import com.lucly.calgrader.block.ItemUpgraderBlock;
import com.lucly.calgrader.block.ItemUpgraderBlockEntity;
import com.lucly.calgrader.menu.UpgraderMenu;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(Calgrader.MODID)
public class Calgrader {
    public static final String MODID = "calgrader";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredBlock<ItemUpgraderBlock> ITEM_UPGRADER = BLOCKS.registerBlock(
            "item_upgrader",
            ItemUpgraderBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5F)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredItem<BlockItem> ITEM_UPGRADER_ITEM = ITEMS.registerSimpleBlockItem("item_upgrader", ITEM_UPGRADER);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemUpgraderBlockEntity>> ITEM_UPGRADER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("item_upgrader", () -> BlockEntityType.Builder.of(ItemUpgraderBlockEntity::new, ITEM_UPGRADER.get()).build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<UpgraderMenu>> UPGRADER_MENU =
            MENUS.register("item_upgrader", () -> new MenuType<>(UpgraderMenu::new, net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CALGRADER_TAB = CREATIVE_MODE_TABS.register("calgrader_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.calgrader"))
            .withTabsBefore(CreativeModeTabs.FUNCTIONAL_BLOCKS)
            .icon(() -> ITEM_UPGRADER_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> output.accept(ITEM_UPGRADER_ITEM.get()))
            .build());

    public Calgrader(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ITEM_UPGRADER_ITEM);
        }
    }
}

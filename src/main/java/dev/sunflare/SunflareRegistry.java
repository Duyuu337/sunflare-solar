package dev.sunflare;

import dev.sunflare.block.FluxGovernorBlock;
import dev.sunflare.block.FluxGovernorBlockEntity;
import dev.sunflare.block.PriorityBusBlock;
import dev.sunflare.block.PriorityBusBlockEntity;
import dev.sunflare.block.TesseractAnchorBlock;
import dev.sunflare.compat.CuriosCompat;
import dev.sunflare.item.SubspaceRelayBeaconItem;
import dev.sunflare.item.TesseractCouplerItem;
import dev.sunflare.block.SolarPanelBlock;
import dev.sunflare.block.SolarPanelBlockEntity;
import dev.sunflare.item.SolarPanelItem;
import dev.sunflare.menu.FluxGovernorMenu;
import dev.sunflare.menu.PriorityBusMenu;
import dev.sunflare.menu.SolarPanelMenu;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SunflareRegistry {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Sunflare.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Sunflare.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Sunflare.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Sunflare.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Sunflare.MOD_ID);

    // ---------------- 菜单 ----------------

    public static final DeferredHolder<MenuType<?>, MenuType<SolarPanelMenu>> SOLAR_PANEL_MENU =
            MENUS.register("solar_panel", () -> IMenuTypeExtension.create(
                    (containerId, inventory, buffer) -> new SolarPanelMenu(containerId, inventory, buffer)));

    public static final DeferredHolder<MenuType<?>, MenuType<FluxGovernorMenu>> FLUX_GOVERNOR_MENU =
            MENUS.register("flux_governor", () -> IMenuTypeExtension.create(
                    (containerId, inventory, buffer) -> new FluxGovernorMenu(containerId, inventory, buffer)));

    public static final DeferredHolder<MenuType<?>, MenuType<PriorityBusMenu>> PRIORITY_BUS_MENU =
            MENUS.register("priority_bus_node", () -> IMenuTypeExtension.create(
                    (containerId, inventory, buffer) -> new PriorityBusMenu(containerId, inventory, buffer)));

    // ---------------- 太阳能板 ----------------

    public static final DeferredBlock<SolarPanelBlock> SOLAR_PANEL_T1 = registerPanel("solar_panel_t1", SolarTier.T1);
    public static final DeferredBlock<SolarPanelBlock> SOLAR_PANEL_T2 = registerPanel("solar_panel_t2", SolarTier.T2);
    public static final DeferredBlock<SolarPanelBlock> SOLAR_PANEL_T3 = registerPanel("solar_panel_t3", SolarTier.T3);
    public static final DeferredBlock<SolarPanelBlock> SOLAR_PANEL_T4 = registerPanel("solar_panel_t4", SolarTier.T4);
    public static final DeferredBlock<SolarPanelBlock> SOLAR_PANEL_T5 = registerPanel("solar_panel_t5", SolarTier.T5);
    public static final DeferredBlock<SolarPanelBlock> SOLAR_PANEL_T6 = registerPanel("solar_panel_t6", SolarTier.T6);
    public static final DeferredBlock<SolarPanelBlock> SOLAR_PANEL_T7 = registerPanel("solar_panel_t7", SolarTier.T7);
    public static final DeferredBlock<SolarPanelBlock> SOLAR_PANEL_T8 = registerPanel("solar_panel_t8", SolarTier.T8);

    public static final DeferredItem<Item> SOLAR_CORE_T1 = registerCore("solar_core_t1");
    public static final DeferredItem<Item> SOLAR_CORE_T2 = registerCore("solar_core_t2");
    public static final DeferredItem<Item> SOLAR_CORE_T3 = registerCore("solar_core_t3");
    public static final DeferredItem<Item> SOLAR_CORE_T4 = registerCore("solar_core_t4");
    public static final DeferredItem<Item> SOLAR_CORE_T5 = registerCore("solar_core_t5");
    public static final DeferredItem<Item> SOLAR_CORE_T6 = registerCore("solar_core_t6");
    public static final DeferredItem<Item> SOLAR_CORE_T7 = registerCore("solar_core_t7");
    public static final DeferredItem<Item> SOLAR_CORE_T8 = registerCore("solar_core_t8");

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SolarPanelBlockEntity>> SOLAR_PANEL_BE =
            BLOCK_ENTITIES.register("solar_panel", () -> BlockEntityType.Builder
                    .of(SolarPanelBlockEntity::new,
                            SOLAR_PANEL_T1.get(), SOLAR_PANEL_T2.get(), SOLAR_PANEL_T3.get(),
                            SOLAR_PANEL_T4.get(), SOLAR_PANEL_T5.get(), SOLAR_PANEL_T6.get(),
                            SOLAR_PANEL_T7.get(), SOLAR_PANEL_T8.get())
                    .build(null));

    // ---------------- 通量调控阵列 ----------------

    public static final DeferredBlock<FluxGovernorBlock> FLUX_GOVERNOR = registerDevice("flux_governor",
            () -> new FluxGovernorBlock(antennaProperties()));
    public static final DeferredBlock<PriorityBusBlock> PRIORITY_BUS = registerDevice("priority_bus_node",
            () -> new PriorityBusBlock(antennaProperties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluxGovernorBlockEntity>> FLUX_GOVERNOR_BE =
            BLOCK_ENTITIES.register("flux_governor", () -> BlockEntityType.Builder
                    .of(FluxGovernorBlockEntity::new, FLUX_GOVERNOR.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PriorityBusBlockEntity>> PRIORITY_BUS_BE =
            BLOCK_ENTITIES.register("priority_bus_node", () -> BlockEntityType.Builder
                    .of(PriorityBusBlockEntity::new, PRIORITY_BUS.get())
                    .build(null));

    // ---------------- 亚空间中继系统 ----------------

    public static final DeferredItem<SubspaceRelayBeaconItem> RELAY_BEACON = ITEMS.register("subspace_relay_beacon",
            () -> new SubspaceRelayBeaconItem(new Item.Properties()));
    public static final DeferredItem<TesseractCouplerItem> TESSERACT_COUPLER = ITEMS.register("tesseract_coupler",
            () -> new TesseractCouplerItem(new Item.Properties()));
    public static final DeferredBlock<TesseractAnchorBlock> TESSERACT_ANCHOR = registerDevice("tesseract_anchor",
            () -> new TesseractAnchorBlock(deviceProperties()));

    public static final DeferredItem<Item> RESONANCE_CRYSTAL = registerCore("subspace_resonance_crystal");

    // ---------------- 创造模式标签页 ----------------

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.sunflare"))
                    .icon(() -> new ItemStack(SOLAR_PANEL_T6.get()))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .displayItems((parameters, output) -> {
                        output.accept(SOLAR_CORE_T1.get());
                        output.accept(SOLAR_CORE_T2.get());
                        output.accept(SOLAR_CORE_T3.get());
                        output.accept(SOLAR_CORE_T4.get());
                        output.accept(SOLAR_CORE_T5.get());
                        output.accept(SOLAR_CORE_T6.get());
                        output.accept(SOLAR_CORE_T7.get());
                        output.accept(SOLAR_CORE_T8.get());
                        output.accept(SOLAR_PANEL_T1.get());
                        output.accept(SOLAR_PANEL_T2.get());
                        output.accept(SOLAR_PANEL_T3.get());
                        output.accept(SOLAR_PANEL_T4.get());
                        output.accept(SOLAR_PANEL_T5.get());
                        output.accept(SOLAR_PANEL_T6.get());
                        output.accept(SOLAR_PANEL_T7.get());
                        output.accept(SOLAR_PANEL_T8.get());
                        output.accept(FLUX_GOVERNOR.get());
                        output.accept(PRIORITY_BUS.get());
                        output.accept(RESONANCE_CRYSTAL.get());
                        output.accept(RELAY_BEACON.get());
                        output.accept(TESSERACT_COUPLER.get());
                        output.accept(TESSERACT_ANCHOR.get());
                    })
                    .build());

    private SunflareRegistry() {
    }

    /** 天线类方块：薄模型，不遮挡相邻面。 */
    private static BlockBehaviour.Properties antennaProperties() {
        return deviceProperties().noOcclusion();
    }

    private static BlockBehaviour.Properties deviceProperties() {
        return BlockBehaviour.Properties.of()
                .strength(4.0F, 8.0F)
                .sound(SoundType.METAL);
    }

    private static <B extends Block> DeferredBlock<B> registerDevice(String name, Supplier<B> factory) {
        DeferredBlock<B> block = BLOCKS.register(name, factory);
        ITEMS.register(name, () -> new dev.sunflare.item.DescribedBlockItem(block.get(), new Item.Properties()));
        return block;
    }

    private static DeferredBlock<SolarPanelBlock> registerPanel(String name, SolarTier tier) {
        DeferredBlock<SolarPanelBlock> block = BLOCKS.register(name,
                () -> new SolarPanelBlock(BlockBehaviour.Properties.of()
                        .strength(3.0F, 6.0F)
                        .sound(SoundType.METAL)
                        // 3px 薄板：关掉遮挡，否则会把相邻方块的面剔掉，天花板/墙边会出现透光洞
                        .noOcclusion(), tier));
        ITEMS.register(name, () -> new SolarPanelItem(block.get(), new Item.Properties(), tier));
        return block;
    }

    private static DeferredItem<Item> registerCore(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    /**
     * 兜底：面板进「功能方块」，核心进「原材料」，调控阵列进「功能方块」。
     */
    public static void onBuildTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
            event.accept(SOLAR_PANEL_T1.get());
            event.accept(SOLAR_PANEL_T2.get());
            event.accept(SOLAR_PANEL_T3.get());
            event.accept(SOLAR_PANEL_T4.get());
            event.accept(SOLAR_PANEL_T5.get());
            event.accept(SOLAR_PANEL_T6.get());
            event.accept(SOLAR_PANEL_T7.get());
            event.accept(SOLAR_PANEL_T8.get());
            event.accept(FLUX_GOVERNOR.get());
            event.accept(PRIORITY_BUS.get());
        } else if (event.getTabKey().equals(CreativeModeTabs.INGREDIENTS)) {
            event.accept(SOLAR_CORE_T1.get());
            event.accept(SOLAR_CORE_T2.get());
            event.accept(SOLAR_CORE_T3.get());
            event.accept(SOLAR_CORE_T4.get());
            event.accept(SOLAR_CORE_T5.get());
            event.accept(SOLAR_CORE_T6.get());
            event.accept(SOLAR_CORE_T7.get());
            event.accept(SOLAR_CORE_T8.get());
        }
    }

    /**
     * 只注册 NeoForge 能量能力，不关心背后是 FE / AE / J。
     */
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, SOLAR_PANEL_BE.get(),
                (panel, side) -> (IEnergyStorage) panel);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, PRIORITY_BUS_BE.get(),
                (bus, side) -> (IEnergyStorage) bus);
        event.registerItem(Capabilities.EnergyStorage.ITEM,
                (stack, context) -> new SubspaceRelayBeaconItem.Storage(stack),
                RELAY_BEACON.get());
        CuriosCompat.registerCapabilities(event);
    }
}

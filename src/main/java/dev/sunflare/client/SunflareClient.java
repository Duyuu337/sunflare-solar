package dev.sunflare.client;

import dev.sunflare.Sunflare;
import dev.sunflare.SunflareRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@EventBusSubscriber(modid = Sunflare.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class SunflareClient {

    private SunflareClient() {
    }

    @SubscribeEvent
    public static void registerConfigScreen(FMLClientSetupEvent event) {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(RangePreview::onRenderLevel);
        ModList.get().getModContainerById(Sunflare.MOD_ID).ifPresent(container ->
                container.registerExtensionPoint(IConfigScreenFactory.class,
                        (minecraft, parent) -> new ConfigurationScreen(container, parent)));
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(SunflareRegistry.SOLAR_PANEL_MENU.get(), SolarPanelScreen::new);
        event.register(SunflareRegistry.FLUX_GOVERNOR_MENU.get(), FluxGovernorScreen::new);
        event.register(SunflareRegistry.PRIORITY_BUS_MENU.get(), PriorityBusScreen::new);
    }
}

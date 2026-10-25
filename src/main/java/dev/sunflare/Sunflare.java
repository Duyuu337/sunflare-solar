package dev.sunflare;

import com.mojang.logging.LogUtils;
import dev.sunflare.block.TesseractAnchorBlock;
import dev.sunflare.logic.RelayLogic;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;

@Mod(Sunflare.MOD_ID)
public final class Sunflare {

    public static final String MOD_ID = "sunflare";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Sunflare(IEventBus modBus, ModContainer container) {
        SunflareRegistry.BLOCKS.register(modBus);
        SunflareRegistry.ITEMS.register(modBus);
        SunflareRegistry.BLOCK_ENTITIES.register(modBus);
        SunflareRegistry.TABS.register(modBus);
        SunflareRegistry.MENUS.register(modBus);
        dev.sunflare.advancement.ModTriggers.TRIGGERS.register(modBus);
        modBus.addListener(SunflareRegistry::registerCapabilities);
        modBus.addListener(SunflareRegistry::onBuildTabContents);
        container.registerConfig(ModConfig.Type.COMMON, SunflareConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, SunflareConfig.CLIENT_SPEC);
        modBus.addListener((RegisterTicketControllersEvent event) -> event.register(TesseractAnchorBlock.CONTROLLER));
        NeoForge.EVENT_BUS.addListener((PlayerTickEvent.Post event) -> RelayLogic.onPlayerTick(event.getEntity()));
        LOGGER.info("[Sunflare] solar panels online - output is Forge Energy, see README for AE/J conversions");
    }
}

package dev.sunflare.compat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Curios 可选集成：未安装时全部退化为空实现，且不会加载 Curios 的类。
 */
public final class CuriosCompat {

    private static final boolean AVAILABLE = ModList.get().isLoaded("curios");

    private CuriosCompat() {
    }

    public static boolean available() {
        return AVAILABLE;
    }

    public static ItemStack findEquipped(Player player) {
        return AVAILABLE ? CuriosLookup.find(player) : ItemStack.EMPTY;
    }

    public static ItemStack findWornCard(Player player) {
        return AVAILABLE ? CuriosLookup.findWornCard(player) : ItemStack.EMPTY;
    }

    public static int charge(Player player, int amount) {
        return AVAILABLE ? CuriosLookup.charge(player, amount) : 0;
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        if (AVAILABLE) {
            CuriosLookup.registerCapabilities(event);
        }
    }
}

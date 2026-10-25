package dev.sunflare.compat;

import dev.sunflare.SunflareRegistry;
import dev.sunflare.item.SubspaceRelayBeaconItem;
import dev.sunflare.logic.RelayLogic;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

/**
 * 只在 Curios 已安装时加载的类，其他类不得直接引用。
 */
final class CuriosLookup {

    private CuriosLookup() {
    }

    static ItemStack find(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> {
                    for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                        var stacks = stacksHandler.getStacks();
                        for (int i = 0; i < stacks.getSlots(); i++) {
                            ItemStack stack = stacks.getStackInSlot(i);
                            if (stack.getItem() instanceof SubspaceRelayBeaconItem) {
                                return stack;
                            }
                        }
                    }
                    return ItemStack.EMPTY;
                })
                .orElse(ItemStack.EMPTY);
    }

    static ItemStack findWornCard(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> {
                    for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                        var stacks = stacksHandler.getStacks();
                        for (int i = 0; i < stacks.getSlots(); i++) {
                            ItemStack stack = stacks.getStackInSlot(i);
                            if (stack.getItem() instanceof dev.sunflare.item.TesseractCouplerItem) {
                                return stack;
                            }
                        }
                    }
                    return ItemStack.EMPTY;
                })
                .orElse(ItemStack.EMPTY);
    }

    static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(CuriosCapability.ITEM,
                (stack, context) -> new CurioRelay(stack),
                SunflareRegistry.RELAY_BEACON.get());
        event.registerItem(CuriosCapability.ITEM,
                (stack, context) -> new CurioRelay(stack),
                SunflareRegistry.TESSERACT_COUPLER.get());
    }

    /** ICurio 实现：只重写 getStack 与 curioTick，其余走默认。 */
    public static final class CurioRelay implements ICurio {
        private final ItemStack stack;

        CurioRelay(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public ItemStack getStack() {
            return this.stack;
        }

        @Override
        public void curioTick(SlotContext slotContext) {
            RelayLogic.onCurioTick(slotContext.entity(), this.stack);
        }
    }
}

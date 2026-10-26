package dev.sunflare.menu;

import dev.sunflare.SunflareRegistry;
import dev.sunflare.item.SubspaceRelayBeaconItem;
import dev.sunflare.util.ItemEnergy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;

/**
 * 中继信标界面：显示缓冲、链接状态，并提供插入 / 取出耦合卡。
 */
public class SubspaceRelayMenu extends AbstractContainerMenu {

    public static final int BUTTON_INSERT = 0;
    public static final int BUTTON_EJECT = 1;

    private final ContainerData data;
    private net.minecraft.world.entity.player.Player menuPlayer;
    private final Inventory playerInventory;

    public SubspaceRelayMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, inventory);
    }

    public SubspaceRelayMenu(int containerId, Inventory inventory) {
        super(SunflareRegistry.SUBSPACE_RELAY_MENU.get(), containerId);
        this.playerInventory = inventory;
        this.menuPlayer = inventory.player;
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                ItemStack beacon = held();
                var p2 = menuPlayer;
                return switch (index) {
                    case 0 -> ItemEnergy.get(beacon);
                    case 1 -> SubspaceRelayBeaconItem.capacity();
                    case 2 -> ItemEnergy.hasLink(beacon) ? 1 : 0;
                    case 3 -> p2 == null || dev.sunflare.compat.CuriosCompat.findEquipped(p2).isEmpty() ? 0 : 1;
                    case 4 -> evalLink();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                // 客户端只读
            }

            @Override
            public int getCount() {
                return 5;
            }
        };
        this.addDataSlots(this.data);
    }

    /** 服务端按主手 → 背包顺序找第一个信标。 */
    private ItemStack held() {
        Player player = this.playerInventory.player;
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof SubspaceRelayBeaconItem) {
            return main;
        }
        for (int i = 0; i < this.playerInventory.getContainerSize(); i++) {
            ItemStack stack = this.playerInventory.getItem(i);
            if (stack.getItem() instanceof SubspaceRelayBeaconItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public int energy() {
        return this.data.get(0);
    }

    public int capacity() {
        return Math.max(1, this.data.get(1));
    }

    public int hasCard() {
        return this.data.get(2);
    }

    public int equipped() {
        return this.data.get(3);
    }

    public int linkStatus() {
        return this.data.get(4);
    }

    /** 0=正常 1=本地模式 2=区块未加载 3=链接失效 4=面板没电 */
    private int evalLink() {
        ItemStack beacon = held();
        if (beacon.isEmpty() || !ItemEnergy.hasLink(beacon)) {
            return 1;
        }
        var player = this.menuPlayer;
        if (player == null || player.getServer() == null) {
            return 0;
        }
        net.minecraft.server.level.ServerLevel level;
        net.minecraft.core.BlockPos pos;
        try {
            var key = net.minecraft.resources.ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION,
                    net.minecraft.resources.ResourceLocation.parse(ItemEnergy.linkDim(beacon)));
            level = player.getServer().getLevel(key);
            pos = net.minecraft.core.BlockPos.of(ItemEnergy.linkPos(beacon));
        } catch (RuntimeException exception) {
            return 3;
        }
        if (level == null) {
            return 3;
        }
        if (!level.isLoaded(pos)) {
            return 2;
        }
        if (!(level.getBlockEntity(pos) instanceof dev.sunflare.block.SolarPanelBlockEntity panel)) {
            return 3;
        }
        return panel.getEnergyStored() > 0 ? 0 : 4;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        ItemStack beacon = held();
        if (beacon.isEmpty()) {
            return false;
        }
        if (id == BUTTON_INSERT) {
            if (ItemEnergy.hasLink(beacon)) {
                return false;
            }
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack candidate = player.getInventory().getItem(i);
                if (candidate.getItem() instanceof dev.sunflare.item.TesseractCouplerItem && ItemEnergy.hasLink(candidate)) {
                    ItemEnergy.setLink(beacon, ItemEnergy.linkDim(candidate), ItemEnergy.linkPos(candidate), ItemEnergy.linkOwner(candidate));
                    candidate.shrink(1);
                    return true;
                }
            }
            return false;
        }
        if (id == BUTTON_EJECT) {
            if (!ItemEnergy.hasLink(beacon)) {
                return false;
            }
            ItemStack card = new ItemStack(SunflareRegistry.TESSERACT_COUPLER.get());
            ItemEnergy.setLink(card, ItemEnergy.linkDim(beacon), ItemEnergy.linkPos(beacon), ItemEnergy.linkOwner(beacon));
            ItemEnergy.clearLink(beacon);
            if (!player.getInventory().add(card)) {
                player.drop(card, false);
            }
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return !held().isEmpty();
    }
}

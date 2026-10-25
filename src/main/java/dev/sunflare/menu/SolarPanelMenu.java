package dev.sunflare.menu;

import dev.sunflare.SunflareRegistry;
import dev.sunflare.block.SolarPanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 只读信息界面：能量、容量、当前输出。
 *
 * <p>数据通过 {@link ContainerData} 同步：服务端绑定方块实体，客户端拿到一份占位数组，
 * 由原版的数据槽机制灌入。</p>
 */
public class SolarPanelMenu extends AbstractContainerMenu {

    private final ContainerData data;
    private final BlockPos pos;

    public SolarPanelMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        // open_screen 包可能不带额外数据：buffer 为 null，或存在但字节不够读一个 BlockPos
        this(containerId, inventory, buffer == null || buffer.readableBytes() < Long.BYTES
                ? BlockPos.ZERO
                : buffer.readBlockPos());
    }

    public SolarPanelMenu(int containerId, Inventory inventory, BlockPos pos) {
        super(SunflareRegistry.SOLAR_PANEL_MENU.get(), containerId);
        this.pos = pos;
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        this.data = blockEntity instanceof SolarPanelBlockEntity panel ? panel.data() : new SimpleData();
        this.addDataSlots(this.data);
    }

    public int energy() {
        return this.data.get(0);
    }

    public int capacity() {
        return Math.max(1, this.data.get(1));
    }

    public int output() {
        return this.data.get(2);
    }

    public int tierIndex() {
        return this.data.get(3);
    }

    public int rated() {
        return this.data.get(4);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockEntity(this.pos) instanceof SolarPanelBlockEntity;
    }

    private static final class SimpleData implements ContainerData {
        private final int[] values = new int[4];

        @Override
        public int get(int index) {
            return this.values[index];
        }

        @Override
        public void set(int index, int value) {
            this.values[index] = value;
        }

        @Override
        public int getCount() {
            return this.values.length;
        }
    }
}

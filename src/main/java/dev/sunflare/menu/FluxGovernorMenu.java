package dev.sunflare.menu;

import dev.sunflare.SunflareRegistry;
import dev.sunflare.block.FluxGovernorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 通量调控器的只读界面。
 */
public class FluxGovernorMenu extends AbstractContainerMenu {

    private final ContainerData data;
    private final BlockPos pos;

    public FluxGovernorMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, inventory, buffer == null || buffer.readableBytes() < Long.BYTES
                ? BlockPos.ZERO
                : buffer.readBlockPos());
    }

    public FluxGovernorMenu(int containerId, Inventory inventory, BlockPos pos) {
        super(SunflareRegistry.FLUX_GOVERNOR_MENU.get(), containerId);
        this.pos = pos;
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        this.data = blockEntity instanceof FluxGovernorBlockEntity governor ? governor.data() : new SimpleData(5);
        this.addDataSlots(this.data);
    }

    public int panelCount() {
        return this.data.get(0);
    }

    public int averagePermille() {
        return this.data.get(1);
    }

    public int scalePercent() {
        return this.data.get(2);
    }

    public int suspendedCount() {
        return this.data.get(3);
    }

    public int networkFull() {
        return this.data.get(4);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockEntity(this.pos) instanceof FluxGovernorBlockEntity;
    }

    static final class SimpleData implements ContainerData {
        private final int[] values;

        SimpleData(int size) {
            this.values = new int[size];
        }

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

package dev.sunflare.menu;

import dev.sunflare.SunflareRegistry;
import dev.sunflare.block.PriorityBusBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 优先级总线节点界面：优先级 ±1、强制通断开关。
 */
public class PriorityBusMenu extends AbstractContainerMenu {

    public static final int BUTTON_PRIORITY_DOWN = 0;
    public static final int BUTTON_PRIORITY_UP = 1;
    public static final int BUTTON_TOGGLE = 2;

    private final ContainerData data;
    private final BlockPos pos;

    public PriorityBusMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, inventory, buffer == null || buffer.readableBytes() < Long.BYTES
                ? BlockPos.ZERO
                : buffer.readBlockPos());
    }

    public PriorityBusMenu(int containerId, Inventory inventory, BlockPos pos) {
        super(SunflareRegistry.PRIORITY_BUS_MENU.get(), containerId);
        this.pos = pos;
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        this.data = blockEntity instanceof PriorityBusBlockEntity bus ? bus.data() : new SharedData(6);
        this.addDataSlots(this.data);
    }

    public int priority() {
        return this.data.get(0);
    }

    public int forcedOff() {
        return this.data.get(1);
    }

    public int suspended() {
        return this.data.get(2);
    }

    public int energy() {
        return this.data.get(3);
    }

    public int capacity() {
        return Math.max(1, this.data.get(4));
    }

    public int throughput() {
        return this.data.get(5);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player.level().getBlockEntity(this.pos) instanceof PriorityBusBlockEntity bus)) {
            return false;
        }
        switch (id) {
            case BUTTON_PRIORITY_DOWN -> bus.setPriority(bus.getPriority() - 1);
            case BUTTON_PRIORITY_UP -> bus.setPriority(bus.getPriority() + 1);
            case BUTTON_TOGGLE -> bus.setForcedOff(!bus.isForcedOff());
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockEntity(this.pos) instanceof PriorityBusBlockEntity;
    }

    static final class SharedData implements ContainerData {
        private final int[] values;

        SharedData(int size) {
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

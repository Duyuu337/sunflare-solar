package dev.sunflare.block;

import dev.sunflare.SunflareRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * 优先级总线节点：本身是一段带缓冲的 FE 通道。
 *
 * <p>优先级 1（最低）~ 5（最高）。调控器在低电量时会把低优先级的节点置为
 * {@code suspended}，此时它既不接收也不输出，负载自然被切走；
 * 玩家还可以用 {@code forcedOff} 手动断开。</p>
 */
public class PriorityBusBlockEntity extends BlockEntity implements IEnergyStorage {

    public static final int CAPACITY = 200_000;
    public static final int MAX_TRANSFER = 20_000;
    public static final int MIN_PRIORITY = 1;
    public static final int MAX_PRIORITY = 5;

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final String TAG_ENERGY = "Energy";
    private static final String TAG_PRIORITY = "Priority";
    private static final String TAG_FORCED_OFF = "ForcedOff";

    private int energy;
    private int priority = 3;
    private boolean forcedOff;
    private boolean suspended;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> priority;
                case 1 -> forcedOff ? 1 : 0;
                case 2 -> suspended ? 1 : 0;
                case 3 -> energy;
                case 4 -> CAPACITY;
                case 5 -> MAX_TRANSFER;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // 客户端同步写入
            switch (index) {
                case 0 -> priority = value;
                case 1 -> forcedOff = value != 0;
                case 2 -> suspended = value != 0;
                case 3 -> energy = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 6;
        }
    };

    public PriorityBusBlockEntity(BlockPos pos, BlockState state) {
        super(SunflareRegistry.PRIORITY_BUS_BE.get(), pos, state);
    }

    public ContainerData data() {
        return this.dataAccess;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = Math.max(MIN_PRIORITY, Math.min(MAX_PRIORITY, priority));
        this.setChanged();
    }

    public boolean isForcedOff() {
        return this.forcedOff;
    }

    public void setForcedOff(boolean forcedOff) {
        this.forcedOff = forcedOff;
        this.setChanged();
    }

    public boolean isSuspended() {
        return this.suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    /** 当前是否允许通过能量。 */
    public boolean isConducting() {
        return !this.forcedOff && !this.suspended;
    }

    public void serverTick() {
        Level level = this.level;
        if (level == null || level.isClientSide || this.energy <= 0) {
            return;
        }
        if (!this.isConducting()) {
            return;
        }
        int budget = Math.min(this.energy, MAX_TRANSFER);
        for (Direction direction : DIRECTIONS) {
            if (budget <= 0) {
                break;
            }
            int moved;
            try {
                IEnergyStorage target = level.getCapability(Capabilities.EnergyStorage.BLOCK,
                        this.worldPosition.relative(direction), direction.getOpposite());
                if (target == null || target == this) {
                    continue;
                }
                moved = target.receiveEnergy(budget, false);
            } catch (RuntimeException exception) {
                continue;
            }
            if (moved > 0) {
                this.energy -= moved;
                budget -= moved;
            }
        }
    }

    // --- IEnergyStorage ---

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        if (!this.isConducting() || toReceive <= 0) {
            return 0;
        }
        int accepted = Math.min(toReceive, CAPACITY - this.energy);
        if (!simulate) {
            this.energy += accepted;
        }
        return accepted;
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        if (!this.isConducting() || toExtract <= 0) {
            return 0;
        }
        int drained = Math.min(toExtract, this.energy);
        if (!simulate) {
            this.energy -= drained;
        }
        return drained;
    }

    @Override
    public int getEnergyStored() {
        return this.energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return CAPACITY;
    }

    @Override
    public boolean canExtract() {
        return this.isConducting();
    }

    @Override
    public boolean canReceive() {
        return this.isConducting();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(TAG_ENERGY, this.energy);
        tag.putInt(TAG_PRIORITY, this.priority);
        tag.putBoolean(TAG_FORCED_OFF, this.forcedOff);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.energy = tag.getInt(TAG_ENERGY);
        this.priority = tag.contains(TAG_PRIORITY) ? tag.getInt(TAG_PRIORITY) : 3;
        this.forcedOff = tag.getBoolean(TAG_FORCED_OFF);
        this.suspended = false;
    }
}

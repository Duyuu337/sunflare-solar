package dev.sunflare.block;

import dev.sunflare.SunflareConfig;
import dev.sunflare.SunflareRegistry;
import java.util.ArrayList;
import java.util.List;
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
 * 通量调控器方块实体。
 *
 * <p>每 {@code intervalTicks} 扫描一次配置半径内的面板与总线节点：</p>
 * <ul>
 *   <li>面板平均缓冲 &gt; 高阈值 → 输出倍率 0.5，连续三次仍满 → 0.0；</li>
 *   <li>平均缓冲 &lt; 低阈值 → 每次恢复 0.25，直到 1.0；</li>
 *   <li>总线网络电量 &lt; 低阈值 → 每轮断开一条最低优先级节点；</li>
 *   <li>网络电量 &gt; 高阈值 → 每轮恢复一条最高优先级节点。</li>
 * </ul>
 */
public class FluxGovernorBlockEntity extends BlockEntity {

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final double SCALE_STEP = 0.25D;
    private static final int SATURATED_SCANS_FOR_ZERO = 3;

    private int ticks;
    private int saturatedScans;
    private float appliedScale = 1.0F;

    private int panelCount;
    private int avgPermille;
    private int scalePercent = 100;
    private int suspendedCount;
    private int networkFull;
    private boolean onlineAwarded;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> panelCount;
                case 1 -> avgPermille;
                case 2 -> scalePercent;
                case 3 -> suspendedCount;
                case 4 -> networkFull;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // 客户端同步写入
            switch (index) {
                case 0 -> panelCount = value;
                case 1 -> avgPermille = value;
                case 2 -> scalePercent = value;
                case 3 -> suspendedCount = value;
                case 4 -> networkFull = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public FluxGovernorBlockEntity(BlockPos pos, BlockState state) {
        super(SunflareRegistry.FLUX_GOVERNOR_BE.get(), pos, state);
    }

    public ContainerData data() {
        return this.dataAccess;
    }

    public void serverTick() {
        Level level = this.level;
        if (level == null || level.isClientSide) {
            return;
        }
        int interval = SunflareConfig.GOVERNOR_INTERVAL.get();
        if (++this.ticks < interval) {
            return;
        }
        this.ticks = 0;
        this.scan(level);
    }

    private void scan(Level level) {
        int radius = SunflareConfig.GOVERNOR_RADIUS.get();
        BlockPos min = this.worldPosition.offset(-radius, -radius, -radius);
        BlockPos max = this.worldPosition.offset(radius, radius, radius);

        List<SolarPanelBlockEntity> panels = new ArrayList<>();
        List<PriorityBusBlockEntity> buses = new ArrayList<>();
        double bufferSum = 0.0D;

        for (BlockPos cursor : BlockPos.betweenClosed(min, max)) {
            if (!level.isLoaded(cursor)) {
                continue;
            }
            BlockEntity blockEntity = level.getBlockEntity(cursor);
            if (blockEntity instanceof SolarPanelBlockEntity panel) {
                panels.add(panel);
                bufferSum += (double) panel.getEnergyStored() / Math.max(1, panel.getMaxEnergyStored());
            } else if (blockEntity instanceof PriorityBusBlockEntity bus) {
                buses.add(bus);
            }
        }

        this.panelCount = panels.size();
        double average = panels.isEmpty() ? 0.0D : bufferSum / panels.size();
        this.avgPermille = (int) Math.round(average * 1000.0D);

        this.adjustPanels(panels, average, level);
        this.dispatchBuses(buses);
        if (!this.onlineAwarded && !panels.isEmpty() && !buses.isEmpty()) {
            this.onlineAwarded = true;
            award(level, dev.sunflare.advancement.SunflareTrigger.Codes.GOVERNOR_ONLINE);
        }
    }

    private void award(Level level, String code) {
        if (level.getNearestPlayer(this.worldPosition.getX(), this.worldPosition.getY(),
                this.worldPosition.getZ(), 24.0D, false) instanceof net.minecraft.server.level.ServerPlayer player) {
            dev.sunflare.advancement.Achievements.fire(player, code);
        }
    }

    private void adjustPanels(List<SolarPanelBlockEntity> panels, double average, Level level) {
        if (panels.isEmpty()) {
            return;
        }
        double high = SunflareConfig.HIGH_BUFFER.get();
        double low = SunflareConfig.LOW_BUFFER.get();

        boolean stuckAtZero = this.appliedScale <= 0.0F && average < high;
        if (average > high) {
            this.saturatedScans++;
            this.appliedScale = this.saturatedScans >= SATURATED_SCANS_FOR_ZERO ? 0.0F : 0.5F;
        } else if (average < low || stuckAtZero) {
            this.saturatedScans = 0;
            this.appliedScale = (float) Math.min(1.0D, this.appliedScale + SCALE_STEP);
        }

        for (SolarPanelBlockEntity panel : panels) {
            panel.setOutputScale(this.appliedScale);
        }
        this.scalePercent = Math.round(this.appliedScale * 100.0F);
        if (this.appliedScale <= 0.0F) {
            award(level, dev.sunflare.advancement.SunflareTrigger.Codes.GOVERNOR_ZERO);
        }
    }

    /**
     * 附近没有任何可接收 FE 的方块，才算“网络已满”；这样避免误判空网络。
     */
    private boolean isNetworkFull(Level level) {
        boolean found = false;
        for (Direction direction : DIRECTIONS) {
            IEnergyStorage handler = level.getCapability(Capabilities.EnergyStorage.BLOCK,
                    this.worldPosition.relative(direction), direction.getOpposite());
            if (handler == null) {
                continue;
            }
            found = true;
            try {
                if (handler.receiveEnergy(1000, true) > 0) {
                    return false;
                }
            } catch (RuntimeException exception) {
                return false;
            }
        }
        return found;
    }

    private void dispatchBuses(List<PriorityBusBlockEntity> buses) {
        if (buses.isEmpty()) {
            this.suspendedCount = 0;
            return;
        }

        long stored = 0;
        long capacity = 0;
        for (PriorityBusBlockEntity bus : buses) {
            stored += bus.getEnergyStored();
            capacity += bus.getMaxEnergyStored();
        }
        double ratio = capacity == 0 ? 0.0D : (double) stored / capacity;

        if (ratio < SunflareConfig.BUS_LOW.get()) {
            // 从最低优先级开始断
            buses.stream()
                    .filter(bus -> !bus.isSuspended() && !bus.isForcedOff())
                    .min((a, b) -> Integer.compare(a.getPriority(), b.getPriority()))
                    .ifPresent(bus -> {
                        bus.setSuspended(true);
                        award(this.level, dev.sunflare.advancement.SunflareTrigger.Codes.BUS_CUT);
                    });
        } else if (ratio > SunflareConfig.BUS_HIGH.get()) {
            // 从最高优先级开始恢复
            buses.stream()
                    .filter(PriorityBusBlockEntity::isSuspended)
                    .max((a, b) -> Integer.compare(a.getPriority(), b.getPriority()))
                    .ifPresent(bus -> bus.setSuspended(false));
        }

        this.suspendedCount = (int) buses.stream().filter(PriorityBusBlockEntity::isSuspended).count();
        this.networkFull = ratio >= 0.999D ? 1 : 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("AppliedScale", this.appliedScale);
        tag.putInt("SaturatedScans", this.saturatedScans);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.appliedScale = tag.contains("AppliedScale") ? tag.getFloat("AppliedScale") : 1.0F;
        this.saturatedScans = tag.getInt("SaturatedScans");
    }
}

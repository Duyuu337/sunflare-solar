package dev.sunflare.block;

import dev.sunflare.Sunflare;
import dev.sunflare.SunflareRegistry;
import dev.sunflare.SolarTier;
import dev.sunflare.util.Numbers;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * 太阳能板方块实体。
 *
 * <p>只发 FE：AE2 与通用机械都能直接取这个能力，换算由对方完成
 * （AE2 用 Energy Acceptor：2 FE = 1 AE；Mekanism：0.4 FE = 1 J）。</p>
 *
 * <p>输出 = 额定输出 × 日照曲线 × 天空光照遮挡 × 天气系数，每 tick 计算一次。</p>
 */
public class SolarPanelBlockEntity extends BlockEntity implements IEnergyStorage {

    private static final String TAG_ENERGY = "Energy";
    private static final String TAG_OUTPUT_SCALE = "OutputScale";
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int DUSK = 12_000;
    private static final int SAVE_INTERVAL = 20;

    private final SolarTier tier;
    private int energy;
    private int lastOutput;
    private int ticks;
    private boolean pushErrorLogged;

    /** 由通量调控器写入的输出倍率，0.0~1.0，持久化。 */
    private float outputScale = 1.0F;

    /** 缓存六个方向的相邻能量接口，邻居变化时由方块回调失效。 */
    private final IEnergyStorage[] neighbourHandlers = new IEnergyStorage[6];
    private final boolean[] neighbourKnown = new boolean[6];

    /** 供 GUI 同步：能量、容量、当前输出、档位序号、额定输出。 */
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy;
                case 1 -> tier.bufferFe();
                case 2 -> lastOutput;
                case 3 -> tier.ordinal();
                case 4 -> tier.peakFePerTick;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // 客户端同步写入
            if (index == 0) {
                energy = value;
            } else if (index == 2) {
                lastOutput = value;
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(SunflareRegistry.SOLAR_PANEL_BE.get(), pos, state);
        this.tier = state.getBlock() instanceof SolarPanelBlock panel ? panel.tier() : SolarTier.T1;
    }

    public SolarTier tier() {
        return this.tier;
    }

    /** 当前输出倍率，1.0 为满发。 */
    public float getOutputScale() {
        return this.outputScale;
    }

    /** 设置输出倍率，范围钳制在 0.0~1.0；变化时标记存档。 */
    public void setOutputScale(float scale) {
        float clamped = Math.max(0.0F, Math.min(1.0F, scale));
        if (Math.abs(clamped - this.outputScale) > 0.001F) {
            this.outputScale = clamped;
            this.setChanged();
        }
    }

    public ContainerData data() {
        return this.dataAccess;
    }

    /** 邻居方块变动时清空缓存，避免把电推进已经被替换掉的方块实体。 */
    public void invalidateNeighbours() {
        Arrays.fill(this.neighbourKnown, false);
        Arrays.fill(this.neighbourHandlers, null);
    }

    @Override
    public void setRemoved() {
        this.invalidateNeighbours();
        super.setRemoved();
    }

    public void serverTick() {
        Level level = this.level;
        if (level == null || level.isClientSide) {
            return;
        }

        int produced = this.computeOutput(level);
        this.lastOutput = produced;
        if (produced > 0) {
            this.energy = Math.min(this.tier.bufferFe(), this.energy + produced);
        }
        this.syncActiveState(level, produced > 0);
        if (this.energy > 0) {
            this.pushToNeighbours(level);
        }
        if (++this.ticks % SAVE_INTERVAL == 0) {
            this.setChanged();
        }
    }

    /**
     * 只有真正在发电时才切换到动画顶面；切换只在状态变化的那一 tick 发生。
     */
    private void syncActiveState(Level level, boolean active) {
        BlockState state = level.getBlockState(this.worldPosition);
        if (state.getBlock() instanceof SolarPanelBlock && state.getValue(SolarPanelBlock.ACTIVE) != active) {
            level.setBlock(this.worldPosition, state.setValue(SolarPanelBlock.ACTIVE, active), Block.UPDATE_ALL);
        }
    }

    /**
     * 正午最强、日出日落为 0；被遮挡按天空光照比例衰减；雨天减半、雷雨剩四分之一。
     */
    private int computeOutput(Level level) {
        if (!level.dimensionType().hasSkyLight()) {
            return 0;
        }
        long dayTime = level.getDayTime() % 24_000L;
        if (dayTime >= DUSK) {
            return 0;
        }
        double sunCurve = Math.sin(Math.PI * dayTime / DUSK);
        int skyLight = level.getBrightness(LightLayer.SKY, this.worldPosition.above());
        double weather = level.isThundering() ? 0.25D : (level.isRaining() ? 0.5D : 1.0D);
        return (int) Math.round(this.tier.peakFePerTick * sunCurve * (skyLight / 15.0D) * weather * this.outputScale);
    }

    private IEnergyStorage neighbour(Direction direction) {
        int index = direction.ordinal();
        if (!this.neighbourKnown[index]) {
            this.neighbourKnown[index] = true;
            Level level = this.level;
            this.neighbourHandlers[index] = level == null ? null : level.getCapability(
                    Capabilities.EnergyStorage.BLOCK,
                    this.worldPosition.relative(direction),
                    direction.getOpposite());
        }
        return this.neighbourHandlers[index];
    }

    private void pushToNeighbours(Level level) {
        int budget = Math.min(this.energy, this.tier.pushFePerTick());
        for (Direction direction : DIRECTIONS) {
            if (budget <= 0) {
                return;
            }
            try {
                IEnergyStorage target = this.neighbour(direction);
                if (target == null) {
                    continue;
                }
                int moved = target.receiveEnergy(budget, false);
                if (moved > 0) {
                    this.energy -= moved;
                    budget -= moved;
                }
            } catch (RuntimeException exception) {
                // 邻居是别的模组的实现，抛异常不该让整条服务端 tick 崩掉；只记一次
                this.neighbourKnown[direction.ordinal()] = false;
                if (!this.pushErrorLogged) {
                    this.pushErrorLogged = true;
                    Sunflare.LOGGER.warn("[Sunflare] a neighbouring energy handler threw while receiving FE at {}",
                            this.worldPosition, exception);
                }
            }
        }
    }

    public Component describe() {
        return Component.translatable("sunflare.message.output",
                Numbers.compact(this.tier.peakFePerTick),
                Numbers.compact(this.tier.toAe(this.tier.peakFePerTick)),
                Numbers.compact(this.tier.toJoules(this.tier.peakFePerTick)),
                Numbers.compact(this.energy),
                Numbers.compact(this.tier.bufferFe()));
    }

    // --- IEnergyStorage：只能出，不能进 ---

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        int drained = Math.min(this.energy, Math.max(0, toExtract));
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
        return this.tier.bufferFe();
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(TAG_ENERGY, this.energy);
        tag.putFloat(TAG_OUTPUT_SCALE, this.outputScale);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.energy = tag.getInt(TAG_ENERGY);
        this.outputScale = tag.contains(TAG_OUTPUT_SCALE) ? tag.getFloat(TAG_OUTPUT_SCALE) : 1.0F;
    }
}

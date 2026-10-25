package dev.sunflare.block;

import com.mojang.serialization.MapCodec;
import dev.sunflare.SolarTier;
import dev.sunflare.menu.SolarPanelMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SolarPanelBlock extends Block implements EntityBlock {

    /** 薄板：3/16 格高，比半砖（8/16）薄，和日光通量那种平铺板一致。 */
    private static final VoxelShape SHAPE = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 3.0D / 16.0D, 1.0D);

    /** 正在发电：true 时切换到带动画的顶面模型。 */
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private final SolarTier tier;

    public SolarPanelBlock(BlockBehaviour.Properties properties, SolarTier tier) {
        super(properties);
        this.tier = tier;
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    public SolarTier tier() {
        return this.tier;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return simpleCodec(properties -> new SolarPanelBlock(properties, this.tier));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SolarPanelBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (lvl, pos, blockState, blockEntity) -> {
            if (blockEntity instanceof SolarPanelBlockEntity panel) {
                panel.serverTick();
            }
        };
    }

    /**
     * 右键开信息界面；潜行右键在动作栏快速读数。
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isShiftKeyDown()) {
            // 潜行右键留给物品（耦合卡绑定）
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                            (containerId, inventory, p) -> new SolarPanelMenu(containerId, inventory, pos),
                            state.getBlock().getName()),
                    // 一定要带额外数据，否则客户端收到 open_screen 时 buffer 为 null
                    buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        // 邻居变了就丢掉能力缓存，避免把电推进已被替换掉的方块实体
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof SolarPanelBlockEntity panel) {
            panel.invalidateNeighbours();
        }
    }
}

package dev.sunflare.block;

import com.mojang.serialization.MapCodec;
import dev.sunflare.SunflareConfig;
import dev.sunflare.world.AnchorChunks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.world.chunk.TicketController;

/**
 * 超立方锚定节点：把一个区块强制加载并保持 ticking，让远端面板继续发电。
 *
 * <p>不产电、不传能，只负责区块票据。</p>
 */
public class TesseractAnchorBlock extends Block {

    public static final TicketController CONTROLLER = new TicketController(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                    dev.sunflare.Sunflare.MOD_ID, "tesseract_anchor"));

    public TesseractAnchorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return simpleCodec(TesseractAnchorBlock::new);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        AnchorChunks data = AnchorChunks.get(serverLevel);
        if (!data.add(pos)) {
            level.removeBlock(pos, false);
            if (placer instanceof net.minecraft.world.entity.player.Player player) {
                player.displayClientMessage(Component.translatable("sunflare.anchor.limit",
                        SunflareConfig.ANCHOR_MAX_CHUNKS.get()), true);
            }
            return;
        }
        CONTROLLER.forceChunk(serverLevel, pos, pos.getX() >> 4, pos.getZ() >> 4, true, true);
        if (serverLevel.dimension() == Level.END && data.size() >= 3 && placer instanceof Player player) {
            dev.sunflare.advancement.Achievements.fire(player,
                    dev.sunflare.advancement.SunflareTrigger.Codes.ANCHOR_THREE_END);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel) {
            AnchorChunks.get(serverLevel).remove(pos);
            CONTROLLER.forceChunk(serverLevel, pos, pos.getX() >> 4, pos.getZ() >> 4, false, true);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}

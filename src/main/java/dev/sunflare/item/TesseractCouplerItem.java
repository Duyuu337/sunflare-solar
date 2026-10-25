package dev.sunflare.item;

import dev.sunflare.block.SolarPanelBlock;
import dev.sunflare.util.ItemEnergy;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 超立方耦合卡：把某一台耀阳板绑定成信标的远端抽取目标。
 */
public class TesseractCouplerItem extends Item {

    public TesseractCouplerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        if (player == null || !(state.getBlock() instanceof SolarPanelBlock)) {
            return InteractionResult.PASS;
        }
        if (!player.isShiftKeyDown()) {
            // 普通右键交给方块（打开面板 GUI）
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = context.getItemInHand();
        if (ItemEnergy.hasLink(stack)) {
            ItemEnergy.clearLink(stack);
            player.displayClientMessage(Component.translatable("sunflare.coupler.cleared"), true);
            return InteractionResult.CONSUME;
        }
        String dim = level.dimension().location().toString();
        ItemEnergy.setLink(stack, dim, pos.asLong(), player.getUUID().toString());
        player.displayClientMessage(Component.translatable("sunflare.coupler.bound",
                dim, pos.getX(), pos.getY(), pos.getZ()), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (ItemEnergy.hasLink(stack)) {
            BlockPos pos = BlockPos.of(ItemEnergy.linkPos(stack));
            tooltip.add(Component.translatable("sunflare.coupler.tooltip.link",
                            ItemEnergy.linkDim(stack), pos.getX(), pos.getY(), pos.getZ())
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("sunflare.coupler.tooltip.owner", shortOwner(ItemEnergy.linkOwner(stack)))
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.add(Component.translatable("sunflare.coupler.tooltip.unbound")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    private static String shortOwner(String owner) {
        return owner.length() > 8 ? owner.substring(0, 8) : owner;
    }
}

package dev.sunflare.item;

import dev.sunflare.SolarTier;
import dev.sunflare.util.Numbers;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

/**
 * 面板物品：在 tooltip 里直接给出额定输出与容量，不用放下来才知道。
 */
public class SolarPanelItem extends BlockItem {

    private final SolarTier tier;

    public SolarPanelItem(Block block, Item.Properties properties, SolarTier tier) {
        super(block, properties);
        this.tier = tier;
    }

    public SolarTier tier() {
        return this.tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("sunflare.tooltip.rate",
                        Numbers.compact(this.tier.peakFePerTick),
                        Numbers.compact(this.tier.toAe(this.tier.peakFePerTick)),
                        Numbers.compact(this.tier.toJoules(this.tier.peakFePerTick)))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("sunflare.tooltip.buffer", Numbers.compact(this.tier.bufferFe()))
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}

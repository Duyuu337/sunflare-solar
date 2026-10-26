package dev.sunflare.item;

import dev.sunflare.SunflareConfig;
import dev.sunflare.util.ItemEnergy;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * 亚空间中继信标：随身充能装置。
 *
 * <p>能量存在物品自身的 {@code CUSTOM_DATA} 里；右键空气可以插入/取出耦合卡。</p>
 */
public class SubspaceRelayBeaconItem extends Item {

    public SubspaceRelayBeaconItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static int capacity() {
        return SunflareConfig.RELAY_CAPACITY.get();
    }

    /** 物品能量能力的视图，读写都落在 ItemStack 上。 */
    public static final class Storage implements IEnergyStorage {
        private final ItemStack stack;

        public Storage(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            int stored = ItemEnergy.get(this.stack);
            int accepted = Math.min(toReceive, capacity() - stored);
            if (!simulate) {
                ItemEnergy.set(this.stack, stored + accepted);
            }
            return accepted;
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            int stored = ItemEnergy.get(this.stack);
            int drained = Math.min(toExtract, stored);
            if (!simulate) {
                ItemEnergy.set(this.stack, stored - drained);
            }
            return drained;
        }

        @Override
        public int getEnergyStored() {
            return ItemEnergy.get(this.stack);
        }

        @Override
        public int getMaxEnergyStored() {
            return capacity();
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack beacon = player.getItemInHand(hand);
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new net.minecraft.world.SimpleMenuProvider(
                    (id, inventory, p) -> new dev.sunflare.menu.SubspaceRelayMenu(id, inventory),
                    Component.translatable(this.getDescriptionId())), buffer -> {
            });
        }
        return InteractionResultHolder.success(beacon);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        int stored = ItemEnergy.get(stack);
        tooltip.add(Component.translatable("sunflare.relay.tooltip.energy",
                        stored / 1000, capacity() / 1000)
                .withStyle(ChatFormatting.GRAY));
        if (ItemEnergy.hasLink(stack)) {
            BlockPos pos = BlockPos.of(ItemEnergy.linkPos(stack));
            tooltip.add(Component.translatable("sunflare.relay.tooltip.link",
                            ItemEnergy.linkDim(stack), pos.getX(), pos.getY(), pos.getZ())
                    .withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable("sunflare.relay.tooltip.nolink")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.add(Component.translatable("sunflare.relay.tooltip.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("sunflare.relay.tooltip.hint2").withStyle(ChatFormatting.DARK_GRAY));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(dev.sunflare.Sunflare.MOD_ID, path);
    }
}

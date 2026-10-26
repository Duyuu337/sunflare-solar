package dev.sunflare.logic;

import dev.sunflare.SunflareConfig;
import dev.sunflare.block.SolarPanelBlockEntity;
import dev.sunflare.compat.CuriosCompat;
import dev.sunflare.item.SubspaceRelayBeaconItem;
import dev.sunflare.util.ItemEnergy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * 中继信标的充能逻辑。挂在 PlayerTickEvent 与 Curios 的 curioTick 上。
 *
 * <p>同一个玩家只会有一个信标被驱动（Curio 优先），所以不会重复扣电。</p>
 */
public final class RelayLogic {

    private static final int CYCLE_TICKS = 20;
    private static volatile boolean ERROR_LOGGED;

    private RelayLogic() {
    }

    public static void onPlayerTick(Player player) {
        if (player.level().isClientSide() || player.tickCount % CYCLE_TICKS != 0) {
            return;
        }
        try {
            ItemStack beacon = findBeacon(player);
            if (!beacon.isEmpty()) {
                cycle(player, beacon);
            }
        } catch (RuntimeException exception) {
            // 玩家 tick 里抛异常会崩服，这里只记一次
            if (!ERROR_LOGGED) {
                ERROR_LOGGED = true;
                dev.sunflare.Sunflare.LOGGER.warn("[Sunflare] relay tick failed", exception);
            }
        }
    }

    /** Curios 路径：装备槽里的信标每个 curioTick 调用一次。 */
    public static void onCurioTick(LivingEntity entity, ItemStack beacon) {
        if (!(entity instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        if (player.tickCount % CYCLE_TICKS != 0) {
            return;
        }
        try {
            cycle(player, beacon);
        } catch (RuntimeException exception) {
            if (!ERROR_LOGGED) {
                ERROR_LOGGED = true;
                dev.sunflare.Sunflare.LOGGER.warn("[Sunflare] relay curio tick failed", exception);
            }
        }
    }

    private static ItemStack findBeacon(Player player) {
        ItemStack equipped = CuriosCompat.findEquipped(player);
        if (!equipped.isEmpty()) {
            return equipped;
        }
        // 没装 Curios（没有饰品栏）时退化为背包模式
        if (CuriosCompat.available()) {
            return ItemStack.EMPTY;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof SubspaceRelayBeaconItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void cycle(Player player, ItemStack beacon) {
        int capacity = SubspaceRelayBeaconItem.capacity();
        int transfer = SunflareConfig.RELAY_TRANSFER.get();
        int stored = ItemEnergy.get(beacon);

        int space = capacity - stored;
        // 只有真的存在可充电物品时才抽电，避免空抽
        if (space > 0 && hasDemand(player)) {
            stored += draw(player, beacon, Math.min(space, transfer));
        }

        int upkeep = SunflareConfig.RELAY_UPKEEP.get();
        if (stored <= upkeep) {
            ItemEnergy.set(beacon, stored);
            return;
        }
        stored -= upkeep;

        double efficiency = SunflareConfig.RELAY_EFFICIENCY.get();
        int budget = Math.min(stored, transfer);
        if (budget > 0) {
            int demand = (int) Math.floor(budget * efficiency);
            int delivered = chargePlayer(player, beacon, demand);
            if (delivered > 0) {
                boolean remoteDim = player.level().dimension() == Level.NETHER || player.level().dimension() == Level.END;
                if (remoteDim && !CuriosCompat.findWornCard(player).isEmpty()) {
                    dev.sunflare.advancement.Achievements.fire(player,
                            dev.sunflare.advancement.SunflareTrigger.Codes.CROSS_DIM_CHARGE);
                }
                stored -= Math.min(stored, (int) Math.ceil(delivered / efficiency));
            }
        }
        ItemEnergy.set(beacon, stored);
    }

    /** 背包 + 饰品栏里是否存在还能吃电的物品。 */
    private static boolean hasDemand(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (space(player.getInventory().getItem(i)) > 0) {
                return true;
            }
        }
        return CuriosCompat.hasChargeable(player);
    }

    private static int space(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        try {
            var storage = stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.ITEM, null);
            if (storage != null && storage.canReceive()) {
                return storage.receiveEnergy(Integer.MAX_VALUE, true);
            }
        } catch (RuntimeException ignored) {
        }
        return 0;
    }

    private static int draw(Player player, ItemStack beacon, int amount) {
        if (amount <= 0) {
            return 0;
        }
        // 只有装备在饰品栏里的耦合卡才算数
        return ItemEnergy.hasLink(beacon)
                ? drawLinked(player, beacon, amount)
                : drawNearby(player, amount);
    }

    /** 卡片模式：跨维度、精确到方块；源区块未加载则返回 0。 */
    private static int drawLinked(Player player, ItemStack card, int amount) {
        try {
            ServerLevel target = resolve(player, ItemEnergy.linkDim(card));
            if (target == null) {
                return 0;
            }
            BlockPos pos = BlockPos.of(ItemEnergy.linkPos(card));
            if (!target.isLoaded(pos)) {
                return 0;
            }
            IEnergyStorage storage = target.getCapability(Capabilities.EnergyStorage.BLOCK, pos, null);
            if (!(storage instanceof SolarPanelBlockEntity panel)) {
                // 目标不是我们的面板（可能被替换）→ 链接失效
                return 0;
            }
            return panel.extractEnergy(amount, false);
        } catch (RuntimeException exception) {
            return 0;
        }
    }

    /** 无卡片模式：扫描半径内的本模组面板，就近抽取。 */
    private static int drawNearby(Player player, int amount) {
        if (!(player.level() instanceof ServerLevel level)) {
            return 0;
        }
        int radius = SunflareConfig.RELAY_RADIUS.get();
        BlockPos origin = player.blockPosition();
        int collected = 0;
        for (BlockPos cursor : BlockPos.betweenClosed(
                origin.offset(-radius, -radius, -radius),
                origin.offset(radius, radius, radius))) {
            if (collected >= amount) {
                break;
            }
            if (!level.isLoaded(cursor)) {
                continue;
            }
            BlockEntity blockEntity = level.getBlockEntity(cursor);
            if (blockEntity instanceof SolarPanelBlockEntity panel) {
                collected += panel.extractEnergy(amount - collected, false);
            }
        }
        return collected;
    }

    private static ServerLevel resolve(Player player, String dim) {
        if (player.getServer() == null || dim.isEmpty()) {
            return null;
        }
        try {
            ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dim));
            return player.getServer().getLevel(key);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    /** 充能顺序：主手 → 副手 → 护甲 → 其余背包；跳过信标自己。 */
    private static int chargePlayer(Player player, ItemStack beacon, int amount) {
        if (amount <= 0) {
            return 0;
        }
        int remaining = amount;
        remaining -= chargeStack(player.getMainHandItem(), beacon, remaining);
        remaining -= chargeStack(player.getOffhandItem(), beacon, remaining);
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            remaining -= chargeStack(player.getInventory().getItem(i), beacon, remaining);
        }
        remaining -= CuriosCompat.charge(player, remaining);
        return amount - remaining;
    }

    private static int chargeStack(ItemStack stack, ItemStack beacon, int amount) {
        if (stack.isEmpty() || amount <= 0 || stack == beacon) {
            return 0;
        }
        try {
            IEnergyStorage storage = stack.getCapability(Capabilities.EnergyStorage.ITEM, null);
            if (storage == null || !storage.canReceive()) {
                return 0;
            }
            return storage.receiveEnergy(amount, false);
        } catch (RuntimeException exception) {
            return 0;
        }
    }
}

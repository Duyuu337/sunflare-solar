package dev.sunflare;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 通量调控阵列的可调参数。全部走 COMMON 配置，改完 /reload 或重启生效。
 */
public final class SunflareConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue GOVERNOR_RADIUS = BUILDER
            .comment("Radius in blocks the Flux Governor scans for panels and bus nodes.")
            .defineInRange("fluxGovernor.radius", 8, 1, 24);

    public static final ModConfigSpec.IntValue GOVERNOR_INTERVAL = BUILDER
            .comment("Ticks between two governor evaluations (20 = once per second).")
            .defineInRange("fluxGovernor.intervalTicks", 20, 10, 200);

    public static final ModConfigSpec.DoubleValue HIGH_BUFFER = BUILDER
            .comment("Average panel buffer above this ratio starts throttling the panels.")
            .defineInRange("fluxGovernor.highBuffer", 0.95D, 0.5D, 1.0D);

    public static final ModConfigSpec.DoubleValue LOW_BUFFER = BUILDER
            .comment("Average panel buffer below this ratio lets the panels ramp back up.")
            .defineInRange("fluxGovernor.lowBuffer", 0.80D, 0.1D, 0.95D);

    public static final ModConfigSpec.DoubleValue BUS_LOW = BUILDER
            .comment("Bus network charge ratio below which the lowest priority node is disconnected.")
            .defineInRange("priorityBus.lowNetwork", 0.20D, 0.0D, 0.9D);

    public static final ModConfigSpec.DoubleValue BUS_HIGH = BUILDER
            .comment("Bus network charge ratio above which a disconnected node is restored.")
            .defineInRange("priorityBus.highNetwork", 0.40D, 0.1D, 1.0D);

    // ---------------- 亚空间中继 ----------------

    public static final ModConfigSpec.IntValue RELAY_RADIUS = BUILDER
            .comment("Radius the relay beacon scans for solar panels when no coupling card is installed.")
            .defineInRange("subspaceRelay.radius", 16, 1, 32);

    public static final ModConfigSpec.IntValue RELAY_CAPACITY = BUILDER
            .comment("Internal FE buffer of the Subspace Relay Beacon.")
            .defineInRange("subspaceRelay.capacity", 400_000, 10_000, 4_000_000);

    public static final ModConfigSpec.IntValue RELAY_TRANSFER = BUILDER
            .comment("Maximum FE moved per transfer tick (both draw and charge).")
            .defineInRange("subspaceRelay.transferPerTick", 2_000, 100, 100_000);

    public static final ModConfigSpec.DoubleValue RELAY_EFFICIENCY = BUILDER
            .comment("Charging efficiency: only this fraction of the drained buffer reaches the items.")
            .defineInRange("subspaceRelay.efficiency", 0.9D, 0.1D, 1.0D);

    public static final ModConfigSpec.IntValue RELAY_UPKEEP = BUILDER
            .comment("FE upkeep paid once per charging cycle; without it the relay stops.")
            .defineInRange("subspaceRelay.upkeepPerCycle", 10, 0, 10_000);

    public static final ModConfigSpec.IntValue ANCHOR_MAX_CHUNKS = BUILDER
            .comment("Maximum number of chunks force-loaded by Tesseract Anchors per level.")
            .defineInRange("tesseractAnchor.maxChunks", 3, 1, 32);

    public static final ModConfigSpec SPEC = BUILDER.build();

    // ---------------- 客户端 ----------------

    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue RANGE_PREVIEW = CLIENT_BUILDER
            .comment("Draw the working-range box while holding the Flux Governor or Priority Bus Node.")
            .define("rangePreview", true);

    public static final ModConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();

    private SunflareConfig() {
    }
}

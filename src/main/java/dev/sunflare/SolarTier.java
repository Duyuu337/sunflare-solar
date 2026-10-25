package dev.sunflare;

/**
 * 八档太阳能板。
 *
 * <p>规模参考日光通量（Solar Flux Reborn）并继续向上延伸：它的顶档是 32,768 RF/t，
 * 本模组 T6 对齐该值，T7 / T8 再各 ×4，顶档 524,288 FE/t（≈209,715 J/t）。</p>
 *
 * <p>换算（对方模组官方比例）：2 FE = 1 AE（AE2，需要 Energy Acceptor），0.4 FE = 1 J（Mekanism）。</p>
 */
public enum SolarTier {

    T1(32),
    T2(128),
    T3(512),
    T4(2_048),
    T5(8_192),
    T6(32_768),
    T7(131_072),
    T8(1_048_576);

    /** 正午、晴天、无遮挡时的 FE/t。 */
    public final int peakFePerTick;

    SolarTier(int peakFePerTick) {
        this.peakFePerTick = peakFePerTick;
    }

    /**
     * 约 200 秒满发缓冲，上限 10 亿，避免 T8 的 {@code peak * 4000} 逼近 int 上限。
     */
    public int bufferFe() {
        return (int) Math.min((long) this.peakFePerTick * 4000L, 1_000_000_000L);
    }

    /** 允许以 8 倍发电速率向外推送，把缓冲快速排空。 */
    public int pushFePerTick() {
        return this.peakFePerTick * 8;
    }

    public int toAe(int fe) {
        return fe / 2;
    }

    public int toJoules(int fe) {
        return fe * 2 / 5;
    }
}

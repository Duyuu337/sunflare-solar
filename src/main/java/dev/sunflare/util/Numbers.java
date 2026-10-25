package dev.sunflare.util;

import java.util.Locale;

/**
 * 大数字压缩显示：32768 → 32.8k，524288 → 524k，1000000000 → 1B。
 *
 * <p>和常见科技模组一致，用 k / M / B / T 四级后缀，避免界面上出现一长串 0。</p>
 */
public final class Numbers {

    private static final String[] UNITS = {"k", "M", "B", "T"};

    private Numbers() {
    }

    public static String compact(long value) {
        long abs = Math.abs(value);
        if (abs < 1000L) {
            return Long.toString(value);
        }

        double scaled = value;
        int unit = -1;
        while (Math.abs(scaled) >= 1000.0 && unit < UNITS.length - 1) {
            scaled /= 1000.0;
            unit++;
        }

        String text = Math.abs(scaled) >= 100.0
                ? String.format(Locale.ROOT, "%.0f", scaled)
                : String.format(Locale.ROOT, "%.1f", scaled);
        if (text.endsWith(".0")) {
            text = text.substring(0, text.length() - 2);
        }
        return text + UNITS[unit];
    }
}

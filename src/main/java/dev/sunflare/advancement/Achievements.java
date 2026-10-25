package dev.sunflare.advancement;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class Achievements {

    private Achievements() {
    }

    public static void fire(Player player, String code) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModTriggers.EVENT.get().fire(serverPlayer, code);
        }
    }
}

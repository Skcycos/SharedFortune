package com.tanrunn.sharedfortune.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class LinkEffectManager {
    private LinkEffectManager() {
    }

    public static void playCreate(ServerPlayer a, ServerPlayer b) {
        sendToBoth(a, b, ParticleTypes.END_ROD, 18, 0.5D, 0.8D, 0.5D, 0.02D);
    }

    public static void playUpgrade(ServerPlayer a, ServerPlayer b, int level) {
        int count = Math.max(1, level * 8);
        sendToBoth(a, b, ParticleTypes.END_ROD, count, 0.5D, 0.8D, 0.5D, 0.02D);
    }

    public static void playBreak(ServerPlayer a, ServerPlayer b) {
        sendToBoth(a, b, ParticleTypes.SMOKE, 16, 0.4D, 0.6D, 0.4D, 0.01D);
    }

    public static void playSharedDamage(ServerPlayer player) {
        sendAround(player, ParticleTypes.DAMAGE_INDICATOR, 8, 0.35D, 0.5D, 0.35D, 0.02D);
    }

    public static void playSharedHeal(ServerPlayer player) {
        sendAround(player, ParticleTypes.HEART, 8, 0.35D, 0.5D, 0.35D, 0.02D);
    }

    private static void sendToBoth(ServerPlayer a, ServerPlayer b,
                                   net.minecraft.core.particles.ParticleOptions particle,
                                   int count, double dx, double dy, double dz, double speed) {
        sendAround(a, particle, count, dx, dy, dz, speed);
        sendAround(b, particle, count, dx, dy, dz, speed);
    }

    private static void sendAround(ServerPlayer player,
                                   net.minecraft.core.particles.ParticleOptions particle,
                                   int count, double dx, double dy, double dz, double speed) {
        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(particle, player.getX(), player.getY() + 1.0D, player.getZ(),
                    count, dx, dy, dz, speed);
        }
    }
}

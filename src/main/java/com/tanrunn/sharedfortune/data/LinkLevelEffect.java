package com.tanrunn.sharedfortune.data;

public final class LinkLevelEffect {
    private LinkLevelEffect() {
    }

    public static boolean shareDamage(int level) {
        return isSupportedLevel(level);
    }

    public static boolean shareHeal(int level) {
        return level >= 2 && isSupportedLevel(level);
    }

    public static float damageMultiplier(int level) {
        return switch (clampLevel(level)) {
            case 4 -> 0.75F;
            case 5 -> 0.5F;
            default -> level == 3 ? 0.8F : 1.0F;
        };
    }

    public static float healMultiplier(int level) {
        return 1.0F;
    }

    private static boolean isSupportedLevel(int level) {
        return level >= SoulLink.MIN_LEVEL && level <= SoulLink.MAX_LEVEL;
    }

    private static int clampLevel(int level) {
        return Math.clamp(level, SoulLink.MIN_LEVEL, SoulLink.MAX_LEVEL);
    }
}

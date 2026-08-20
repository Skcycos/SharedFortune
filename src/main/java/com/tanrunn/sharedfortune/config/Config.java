package com.tanrunn.sharedfortune.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_SHARED_DAMAGE = BUILDER
            .comment("Whether linked players share damage.")
            .define("enableSharedDamage", true);

    public static final ModConfigSpec.BooleanValue ENABLE_SHARED_HEAL = BUILDER
            .comment("Whether linked players share healing.")
            .define("enableSharedHeal", true);

    public static final ModConfigSpec.IntValue MAX_LINK_DISTANCE = BUILDER
            .comment("Maximum distance between linked players. 0 means unlimited.")
            .defineInRange("maxLinkDistance", 0, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {
    }
}

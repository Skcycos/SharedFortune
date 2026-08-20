package com.tanrunn.sharedfortune;

import com.tanrunn.sharedfortune.config.Config;
import com.tanrunn.sharedfortune.item.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(SharedFortune.MOD_ID)
public final class SharedFortune {
    public static final String MOD_ID = "sharedfortune";

    public SharedFortune(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.ITEMS.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}

package com.tanrunn.sharedfortune.command;

import com.tanrunn.sharedfortune.SharedFortune;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = SharedFortune.MOD_ID)
public final class LinkRequestEvents {
    private LinkRequestEvents() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        LinkRequestManager.cleanupExpired();
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LinkRequestManager.removeFor(player.getUUID());
        }
    }
}

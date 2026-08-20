package com.tanrunn.sharedfortune.event;

import com.tanrunn.sharedfortune.SharedFortune;
import com.tanrunn.sharedfortune.data.SharedFortuneSavedData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = SharedFortune.MOD_ID)
public final class DeathHandler {
    private DeathHandler() {
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel level)) {
            return;
        }

        SharedFortuneSavedData data = SharedFortuneSavedData.get(level);
        var partnerId = data.getPartner(player.getUUID());
        if (partnerId.isPresent()) {
            data.unlink(player.getUUID());
            player.sendSystemMessage(Component.literal("生命链接已解除。"));
            ServerPlayer partner = level.getServer().getPlayerList().getPlayer(partnerId.get());
            if (partner != null) {
                partner.sendSystemMessage(Component.literal("生命链接已解除。"));
            }
        }
    }
}

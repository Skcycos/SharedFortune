package com.tanrunn.sharedfortune.event;

import com.tanrunn.sharedfortune.SharedFortune;
import com.tanrunn.sharedfortune.config.Config;
import com.tanrunn.sharedfortune.data.SoulLink;
import com.tanrunn.sharedfortune.data.SharedFortuneSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;

@EventBusSubscriber(modid = SharedFortune.MOD_ID)
public final class HealHandler {
    private static final ThreadLocal<Boolean> SYNCING = ThreadLocal.withInitial(() -> false);

    private HealHandler() {
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (!Config.ENABLE_SHARED_HEAL.get() || SYNCING.get()
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel level)) {
            return;
        }

        SoulLink link = SharedFortuneSavedData.get(level).getLink(player.getUUID());
        if (link == null || !link.active()) {
            return;
        }
        ServerPlayer partner = level.getServer().getPlayerList().getPlayer(link.getOtherPlayer(player.getUUID()));
        if (partner == null) {
            return;
        }

        SYNCING.set(true);
        try {
            partner.heal(event.getAmount());
        } finally {
            SYNCING.set(false);
        }
    }

}

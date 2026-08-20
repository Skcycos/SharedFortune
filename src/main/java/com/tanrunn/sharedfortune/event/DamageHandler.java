package com.tanrunn.sharedfortune.event;

import com.tanrunn.sharedfortune.SharedFortune;
import com.tanrunn.sharedfortune.config.Config;
import com.tanrunn.sharedfortune.data.SharedFortuneSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = SharedFortune.MOD_ID)
public final class DamageHandler {
    private static final ThreadLocal<Boolean> SYNCING = ThreadLocal.withInitial(() -> false);

    private DamageHandler() {
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!Config.ENABLE_SHARED_DAMAGE.get() || SYNCING.get()
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel level)) {
            return;
        }

        java.util.UUID partnerId = SharedFortuneSavedData.get(level).getPartner(player.getUUID());
        if (partnerId == null) {
            return;
        }
        ServerPlayer partner = level.getServer().getPlayerList().getPlayer(partnerId);
        if (partner == null) {
            return;
        }

        SYNCING.set(true);
        try {
            partner.hurt(event.getSource(), event.getAmount());
        } finally {
            SYNCING.set(false);
        }
    }

}

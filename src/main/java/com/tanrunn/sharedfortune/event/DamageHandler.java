package com.tanrunn.sharedfortune.event;

import com.tanrunn.sharedfortune.SharedFortune;
import com.tanrunn.sharedfortune.config.Config;
import com.tanrunn.sharedfortune.data.LinkLevelEffect;
import com.tanrunn.sharedfortune.data.SoulLink;
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

        SoulLink link = SharedFortuneSavedData.get(level).getLink(player.getUUID());
        if (link == null || !link.isValid()) {
            return;
        }
        int levelValue = link.getLevel();
        if (!LinkLevelEffect.shareDamage(levelValue)) {
            return;
        }

        ServerPlayer partner = level.getServer().getPlayerList().getPlayer(link.getOtherPlayer(player.getUUID()));
        if (partner == null) {
            return;
        }

        SYNCING.set(true);
        try {
            float syncedDamage = event.getAmount() * LinkLevelEffect.damageMultiplier(levelValue);
            partner.hurt(event.getSource(), syncedDamage);
        } finally {
            SYNCING.set(false);
        }
    }

}

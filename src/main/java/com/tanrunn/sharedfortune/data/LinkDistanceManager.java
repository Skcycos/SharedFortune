package com.tanrunn.sharedfortune.data;

import com.tanrunn.sharedfortune.config.Config;
import net.minecraft.server.level.ServerPlayer;

public final class LinkDistanceManager {
    private LinkDistanceManager() {
    }

    public static boolean canInteract(ServerPlayer a, ServerPlayer b) {
        int maxDistance = Config.MAX_LINK_DISTANCE.get();
        if (maxDistance <= 0) {
            return true;
        }
        if (a.level() != b.level()) {
            return false;
        }
        return a.distanceTo(b) <= maxDistance;
    }
}

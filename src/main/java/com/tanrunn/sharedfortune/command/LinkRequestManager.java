package com.tanrunn.sharedfortune.command;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class LinkRequestManager {
    private static final long REQUEST_TIMEOUT_MILLIS = 60_000L;
    private static final Map<UUID, PendingRequest> REQUESTS = new HashMap<>();

    private LinkRequestManager() {
    }

    public static boolean create(ServerPlayer requester, ServerPlayer target) {
        if (REQUESTS.containsKey(target.getUUID())) {
            return false;
        }
        REQUESTS.put(target.getUUID(), new PendingRequest(requester.getUUID(), System.currentTimeMillis()));
        return true;
    }

    public static UUID accept(ServerPlayer target) {
        PendingRequest request = REQUESTS.remove(target.getUUID());
        if (request == null || request.isExpired()) {
            return null;
        }
        return request.requesterId();
    }

    public static UUID deny(ServerPlayer target) {
        PendingRequest request = REQUESTS.remove(target.getUUID());
        if (request == null || request.isExpired()) {
            return null;
        }
        return request.requesterId();
    }

    public static void removeFor(UUID playerId) {
        REQUESTS.entrySet().removeIf(entry -> entry.getKey().equals(playerId)
                || entry.getValue().requesterId().equals(playerId));
    }

    public static void cleanupExpired() {
        REQUESTS.values().removeIf(PendingRequest::isExpired);
    }

    private record PendingRequest(UUID requesterId, long createdAt) {
        private boolean isExpired() {
            return System.currentTimeMillis() - createdAt >= REQUEST_TIMEOUT_MILLIS;
        }
    }
}

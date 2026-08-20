package com.tanrunn.sharedfortune.data;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public final class SoulLink {
    private static final String PLAYER_A_TAG = "playerA";
    private static final String PLAYER_B_TAG = "playerB";
    private static final String CREATE_TIME_TAG = "createTime";
    private static final String LEVEL_TAG = "level";
    private static final String ACTIVE_TAG = "active";

    private final UUID playerA;
    private final UUID playerB;
    private final long createTime;
    private final int level;
    private boolean active;

    public SoulLink(UUID playerA, UUID playerB, long createTime, int level, boolean active) {
        this.playerA = playerA;
        this.playerB = playerB;
        this.createTime = createTime;
        this.level = level;
        this.active = active;
    }

    public static SoulLink create(UUID playerA, UUID playerB) {
        return new SoulLink(playerA, playerB, System.currentTimeMillis(), 1, true);
    }

    public static SoulLink load(CompoundTag tag) {
        return new SoulLink(
                UUID.fromString(tag.getString(PLAYER_A_TAG)),
                UUID.fromString(tag.getString(PLAYER_B_TAG)),
                tag.getLong(CREATE_TIME_TAG),
                tag.getInt(LEVEL_TAG),
                tag.getBoolean(ACTIVE_TAG));
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString(PLAYER_A_TAG, playerA.toString());
        tag.putString(PLAYER_B_TAG, playerB.toString());
        tag.putLong(CREATE_TIME_TAG, createTime);
        tag.putInt(LEVEL_TAG, level);
        tag.putBoolean(ACTIVE_TAG, active);
        return tag;
    }

    public UUID playerA() {
        return playerA;
    }

    public UUID playerB() {
        return playerB;
    }

    public long createTime() {
        return createTime;
    }

    public int level() {
        return level;
    }

    public boolean active() {
        return active;
    }

    public void deactivate() {
        active = false;
    }

    public UUID getOtherPlayer(UUID player) {
        if (playerA.equals(player)) {
            return playerB;
        }
        if (playerB.equals(player)) {
            return playerA;
        }
        return null;
    }
}

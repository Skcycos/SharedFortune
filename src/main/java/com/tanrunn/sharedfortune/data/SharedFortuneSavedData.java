package com.tanrunn.sharedfortune.data;

import com.tanrunn.sharedfortune.SharedFortune;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SharedFortuneSavedData extends SavedData {
    private static final String DATA_NAME = "shared_fortune";
    private static final String LINKS_TAG = "links";

    private final Map<UUID, SoulLink> links = new HashMap<>();

    public static SharedFortuneSavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().getLevel(ServerLevel.OVERWORLD);
        if (overworld == null) {
            throw new IllegalStateException("The overworld is unavailable");
        }
        return overworld.getDataStorage().computeIfAbsent(
                new Factory<>(SharedFortuneSavedData::new, SharedFortuneSavedData::load, null), DATA_NAME);
    }

    public void addLink(ServerPlayer playerA, ServerPlayer playerB) {
        SoulLink link = SoulLink.create(playerA.getUUID(), playerB.getUUID());
        links.put(link.playerA(), link);
        links.put(link.playerB(), link);
        setDirty();
    }

    public SoulLink getLink(UUID player) {
        return links.get(player);
    }

    public UUID getPartner(UUID player) {
        SoulLink link = getLink(player);
        return link == null || !link.isValid() ? null : link.getOtherPlayer(player);
    }

    public boolean hasLink(UUID player) {
        SoulLink link = getLink(player);
        return link != null && link.active();
    }

    public void removeLink(UUID player) {
        SoulLink link = getLink(player);
        if (link != null) {
            links.remove(link.playerA());
            links.remove(link.playerB());
            link.deactivate();
            setDirty();
        }
    }

    public void cleanupInactiveLinks() {
        boolean changed = links.values().removeIf(link -> !link.isValid());
        if (changed) {
            setDirty();
        }
    }

    public static SharedFortuneSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        SharedFortuneSavedData data = new SharedFortuneSavedData();
        ListTag linksTag = tag.getList(LINKS_TAG, Tag.TAG_COMPOUND);
        for (Tag entry : linksTag) {
            try {
                SoulLink link = SoulLink.load((CompoundTag) entry);
                if (link.isValid()) {
                    data.links.put(link.playerA(), link);
                    data.links.put(link.playerB(), link);
                }
            } catch (IllegalArgumentException | IllegalStateException exception) {
                SharedFortune.LOGGER.warn("跳过损坏的 Shared Fortune 生命链接数据", exception);
            }
        }
        data.cleanupInactiveLinks();
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag linksTag = new ListTag();
        for (SoulLink link : links.values()) {
            if (link.active() && link.playerA().compareTo(link.playerB()) < 0) {
                linksTag.add(link.save());
            }
        }
        tag.put(LINKS_TAG, linksTag);
        return tag;
    }
}

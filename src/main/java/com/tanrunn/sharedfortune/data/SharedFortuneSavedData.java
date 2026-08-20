package com.tanrunn.sharedfortune.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class SharedFortuneSavedData extends SavedData {
    private static final String DATA_NAME = "shared_fortune";
    private static final String LINKS_TAG = "Links";
    private static final String FIRST_TAG = "First";
    private static final String SECOND_TAG = "Second";

    private final Map<UUID, UUID> links = new HashMap<>();

    public static SharedFortuneSavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().getLevel(ServerLevel.OVERWORLD);
        if (overworld == null) {
            throw new IllegalStateException("The overworld is unavailable");
        }
        return overworld.getDataStorage().computeIfAbsent(
                new Factory<>(SharedFortuneSavedData::new, SharedFortuneSavedData::load, null), DATA_NAME);
    }

    public Optional<UUID> getPartner(UUID player) {
        return Optional.ofNullable(links.get(player));
    }

    public void link(UUID first, UUID second) {
        links.put(first, second);
        links.put(second, first);
        setDirty();
    }

    public void unlink(UUID player) {
        UUID partner = links.remove(player);
        if (partner != null && player.equals(links.get(partner))) {
            links.remove(partner);
        }
        setDirty();
    }

    public static SharedFortuneSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        SharedFortuneSavedData data = new SharedFortuneSavedData();
        ListTag linksTag = tag.getList(LINKS_TAG, Tag.TAG_COMPOUND);
        for (Tag entry : linksTag) {
            CompoundTag link = (CompoundTag) entry;
            UUID first = NbtUtils.loadUUID(link.get(FIRST_TAG));
            UUID second = NbtUtils.loadUUID(link.get(SECOND_TAG));
            data.links.put(first, second);
            data.links.put(second, first);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag linksTag = new ListTag();
        for (Map.Entry<UUID, UUID> entry : links.entrySet()) {
            if (entry.getKey().compareTo(entry.getValue()) < 0) {
                CompoundTag link = new CompoundTag();
                link.put(FIRST_TAG, NbtUtils.createUUID(entry.getKey()));
                link.put(SECOND_TAG, NbtUtils.createUUID(entry.getValue()));
                linksTag.add(link);
            }
        }
        tag.put(LINKS_TAG, linksTag);
        return tag;
    }
}

package com.tanrunn.sharedfortune.item;

import com.tanrunn.sharedfortune.SharedFortune;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SharedFortune.MOD_ID);

    public static final DeferredItem<Item> SHARED_FORTUNE = ITEMS.registerItem(
            "shared_fortune", SharedFortuneItem::new, new Item.Properties());

    private ModItems() {
    }
}

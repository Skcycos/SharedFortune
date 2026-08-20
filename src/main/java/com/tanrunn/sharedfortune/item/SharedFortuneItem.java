package com.tanrunn.sharedfortune.item;

import com.tanrunn.sharedfortune.data.SharedFortuneSavedData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class SharedFortuneItem extends Item {
    public SharedFortuneItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(player instanceof ServerPlayer owner) || !(target instanceof ServerPlayer partner)
                || !(player.level() instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        if (owner == partner) {
            return InteractionResult.PASS;
        }

        SharedFortuneSavedData data = SharedFortuneSavedData.get(serverLevel);
        if (data.hasLink(owner.getUUID())) {
            owner.sendSystemMessage(Component.literal("你已经建立了生命链接。"));
            return InteractionResult.SUCCESS;
        }
        if (data.hasLink(partner.getUUID())) {
            owner.sendSystemMessage(Component.literal("对方已经建立了生命链接。"));
            return InteractionResult.SUCCESS;
        }

        data.addLink(owner, partner);
        owner.sendSystemMessage(Component.literal("§a你和玩家 " + partner.getGameProfile().getName() + " 建立了生命链接！"));
        partner.sendSystemMessage(Component.literal("§a你和玩家 " + owner.getGameProfile().getName() + " 建立了生命链接！"));
        return InteractionResult.SUCCESS;
    }
}

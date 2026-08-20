package com.tanrunn.sharedfortune.item;

import com.tanrunn.sharedfortune.config.Config;
import com.tanrunn.sharedfortune.data.SharedFortuneSavedData;
import com.tanrunn.sharedfortune.effect.LinkEffectManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ContractCertificateItem extends Item {
    public ContractCertificateItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(player instanceof ServerPlayer owner) || !(target instanceof ServerPlayer partner)
                || !(player.level() instanceof ServerLevel serverLevel) || owner == partner) {
            return InteractionResult.PASS;
        }

        SharedFortuneSavedData data = SharedFortuneSavedData.get(serverLevel);
        if (!data.hasLink(owner.getUUID()) || !data.hasLink(partner.getUUID())) {
            owner.sendSystemMessage(Component.literal("没有生命契约。"));
            return InteractionResult.SUCCESS;
        }

        if (!data.getLink(owner.getUUID()).canUpgrade()) {
            owner.sendSystemMessage(Component.literal("契约等级已达到上限。"));
            return InteractionResult.SUCCESS;
        }

        if (!data.upgradeLink(owner.getUUID())) {
            owner.sendSystemMessage(Component.literal("契约等级提升失败。"));
            return InteractionResult.SUCCESS;
        }

        int newLevel = data.getLink(owner.getUUID()).getLevel();
        LinkEffectManager.playUpgrade(owner, partner, newLevel);
        owner.sendSystemMessage(Component.literal("生命契约已提升至等级 " + newLevel + "。"));
        partner.sendSystemMessage(Component.literal("生命契约已提升至等级 " + newLevel + "。"));
        if (!owner.getAbilities().instabuild && Config.UPGRADE_ITEM_CONSUME.get()) {
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}

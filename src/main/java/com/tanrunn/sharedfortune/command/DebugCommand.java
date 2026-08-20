package com.tanrunn.sharedfortune.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.tanrunn.sharedfortune.SharedFortune;
import com.tanrunn.sharedfortune.config.Config;
import com.tanrunn.sharedfortune.data.SharedFortuneSavedData;
import com.tanrunn.sharedfortune.data.SoulLink;
import com.tanrunn.sharedfortune.data.LinkDistanceManager;
import com.tanrunn.sharedfortune.effect.LinkEffectManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = SharedFortune.MOD_ID)
public final class DebugCommand {
    private DebugCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("sharedfortune")
                .then(Commands.literal("debug")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> execute(context.getSource())))
                .then(Commands.literal("unlink")
                        .executes(context -> unlinkSelf(context.getSource())))
                .then(Commands.literal("info")
                        .executes(context -> info(context.getSource())))
                .then(Commands.literal("link")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> requestLink(
                                        context.getSource(), EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("accept")
                        .executes(context -> acceptLink(context.getSource())))
                .then(Commands.literal("deny")
                        .executes(context -> denyLink(context.getSource())))
                .then(Commands.literal("admin")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("unlink")
                                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                        .executes(context -> adminUnlink(
                                                context.getSource(),
                                                GameProfileArgument.getGameProfiles(context, "player")))))));
    }

    private static int execute(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        SharedFortuneSavedData data = SharedFortuneSavedData.get(player.serverLevel());
        SoulLink link = data.getLink(player.getUUID());
        source.sendSuccess(() -> Component.literal("当前玩家: " + player.getUUID()), false);

        if (link == null) {
            source.sendSuccess(() -> Component.literal("绑定对象: 无"), false);
            return 1;
        }

        ServerPlayer partner = player.getServer().getPlayerList().getPlayer(link.getOtherPlayer(player.getUUID()));
        source.sendSuccess(() -> Component.literal("绑定对象: " + link.getOtherPlayer(player.getUUID())), false);
        source.sendSuccess(() -> Component.literal("Created Time: " + link.createTime()), false);
        source.sendSuccess(() -> Component.literal("Partner Online: " + (partner != null)), false);
        if (partner == null) {
            source.sendSuccess(() -> Component.literal("Partner Distance: 离线"), false);
            source.sendSuccess(() -> Component.literal("Distance Allowed: false"), false);
        } else {
            source.sendSuccess(() -> Component.literal("Partner Distance: " + player.distanceTo(partner)), false);
            source.sendSuccess(() -> Component.literal("Distance Allowed: " + LinkDistanceManager.canInteract(player, partner)), false);
        }
        source.sendSuccess(() -> Component.literal("Link Level: " + link.getLevel()), false);
        source.sendSuccess(() -> Component.literal("Can Upgrade: " + link.canUpgrade()), false);
        source.sendSuccess(() -> Component.literal("Can Downgrade: " + link.canDowngrade()), false);
        source.sendSuccess(() -> Component.literal("Upgrade Available: " + link.canUpgrade()), false);
        source.sendSuccess(() -> Component.literal("Upgrade Item Required: " + Config.UPGRADE_ITEM_CONSUME.get()), false);
        source.sendSuccess(() -> Component.literal("Link: active=" + link.active()
                + ", createTime=" + link.createTime()), false);
        return 1;
    }

    private static int unlinkSelf(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        return unlinkPlayer(source, player.getUUID(), player.getName().getString());
    }

    private static int requestLink(CommandSourceStack source, ServerPlayer target) throws CommandSyntaxException {
        ServerPlayer requester = source.getPlayerOrException();
        if (requester == target) {
            source.sendFailure(Component.literal("不能和自己建立生命契约。"));
            return 0;
        }

        SharedFortuneSavedData data = SharedFortuneSavedData.get(requester.serverLevel());
        if (data.hasLink(requester.getUUID()) || data.hasLink(target.getUUID())) {
            source.sendFailure(Component.literal("你或目标玩家已经存在生命契约。"));
            return 0;
        }
        if (!LinkRequestManager.create(requester, target)) {
            source.sendFailure(Component.literal("目标玩家已有待处理的生命契约请求。"));
            return 0;
        }

        requester.sendSystemMessage(Component.literal("已向玩家 " + target.getName().getString() + " 发送生命契约请求。"));
        target.sendSystemMessage(Component.literal("玩家 " + requester.getName().getString()
                + " 请求与你建立生命契约。输入 /sharedfortune accept 接受，或 /sharedfortune deny 拒绝。请求 60 秒后过期。"));
        return 1;
    }

    private static int acceptLink(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer target = source.getPlayerOrException();
        java.util.UUID requesterId = LinkRequestManager.accept(target);
        if (requesterId == null) {
            source.sendFailure(Component.literal("没有有效的生命契约请求。"));
            return 0;
        }

        ServerPlayer requester = target.getServer().getPlayerList().getPlayer(requesterId);
        if (requester == null) {
            source.sendFailure(Component.literal("请求发起者已离线，请重新发起请求。"));
            return 0;
        }

        SharedFortuneSavedData data = SharedFortuneSavedData.get(target.serverLevel());
        if (data.hasLink(target.getUUID()) || data.hasLink(requester.getUUID())) {
            source.sendFailure(Component.literal("你或请求发起者已经存在生命契约。"));
            return 0;
        }
        data.addLink(requester, target);
        LinkEffectManager.playCreate(requester, target);
        Component message = Component.literal("生命契约已建立。");
        requester.sendSystemMessage(message);
        target.sendSystemMessage(message);
        return 1;
    }

    private static int denyLink(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer target = source.getPlayerOrException();
        java.util.UUID requesterId = LinkRequestManager.deny(target);
        if (requesterId == null) {
            source.sendFailure(Component.literal("没有有效的生命契约请求。"));
            return 0;
        }

        ServerPlayer requester = target.getServer().getPlayerList().getPlayer(requesterId);
        target.sendSystemMessage(Component.literal("你已拒绝生命契约请求。"));
        if (requester != null) {
            requester.sendSystemMessage(Component.literal("玩家 " + target.getName().getString() + " 已拒绝生命契约请求。"));
        }
        return 1;
    }

    private static int info(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        SharedFortuneSavedData data = SharedFortuneSavedData.get(player.serverLevel());
        SoulLink link = data.getLink(player.getUUID());

        source.sendSuccess(() -> Component.literal("§6生命契约信息"), false);
        if (link == null || !link.isValid()) {
            source.sendSuccess(() -> Component.literal("是否存在契约: 否"), false);
            return 1;
        }

        java.util.UUID partnerId = link.getOtherPlayer(player.getUUID());
        ServerPlayer partner = player.getServer().getPlayerList().getPlayer(partnerId);
        String partnerName = player.getServer().getProfileCache().get(partnerId)
                .map(profile -> profile.getName())
                .orElse("未知玩家");
        int maxDistance = Config.MAX_LINK_DISTANCE.get();
        boolean distanceAllowed = partner != null && LinkDistanceManager.canInteract(player, partner);

        source.sendSuccess(() -> Component.literal("是否存在契约: 是"), false);
        source.sendSuccess(() -> Component.literal("契约对象名称: " + partnerName), false);
        source.sendSuccess(() -> Component.literal("契约对象 UUID: " + partnerId), false);
        source.sendSuccess(() -> Component.literal("契约等级: " + link.getLevel()), false);
        source.sendSuccess(() -> Component.literal("active: " + link.active()), false);
        source.sendSuccess(() -> Component.literal("创建时间: " + link.createTime()), false);
        source.sendSuccess(() -> Component.literal("伙伴在线: " + (partner != null)), false);
        source.sendSuccess(() -> Component.literal("当前距离: " + (partner == null ? "不可用" : player.distanceTo(partner))), false);
        source.sendSuccess(() -> Component.literal("最大允许距离: " + (maxDistance <= 0 ? "无限" : maxDistance)), false);
        source.sendSuccess(() -> Component.literal("是否允许同步: " + distanceAllowed), false);
        return 1;
    }

    private static int adminUnlink(CommandSourceStack source, java.util.Collection<GameProfile> profiles) {
        int count = 0;
        for (GameProfile profile : profiles) {
            count += unlinkPlayer(source, profile.getId(), profile.getName());
        }
        return count;
    }

    private static int unlinkPlayer(CommandSourceStack source, java.util.UUID playerId, String playerName) {
        SharedFortuneSavedData data = SharedFortuneSavedData.get(source.getLevel());
        java.util.UUID partnerId = data.getPartner(playerId);
        if (partnerId == null) {
            source.sendFailure(Component.literal("没有生命契约。"));
            return 0;
        }

        data.removeLink(playerId);
        source.sendSuccess(() -> Component.literal("已解除玩家 " + playerName + " 的生命契约。"), true);

        ServerPlayer player = source.getServer().getPlayerList().getPlayer(playerId);
        ServerPlayer partner = source.getServer().getPlayerList().getPlayer(partnerId);
        Component message = Component.literal("生命契约已解除。");
        if (player != null) {
            player.sendSystemMessage(message);
        }
        if (partner != null && partner != player) {
            partner.sendSystemMessage(message);
        }
        if (player != null && partner != null) {
            LinkEffectManager.playBreak(player, partner);
        }
        return 1;
    }
}

package com.tanrunn.sharedfortune.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.tanrunn.sharedfortune.SharedFortune;
import com.tanrunn.sharedfortune.config.Config;
import com.tanrunn.sharedfortune.data.SharedFortuneSavedData;
import com.tanrunn.sharedfortune.data.SoulLink;
import com.tanrunn.sharedfortune.data.LinkDistanceManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
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
        return 1;
    }
}

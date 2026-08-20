package com.tanrunn.sharedfortune.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.tanrunn.sharedfortune.SharedFortune;
import com.tanrunn.sharedfortune.config.Config;
import com.tanrunn.sharedfortune.data.SharedFortuneSavedData;
import com.tanrunn.sharedfortune.data.SoulLink;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
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
                        .executes(context -> execute(context.getSource()))));
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

        source.sendSuccess(() -> Component.literal("绑定对象: " + link.getOtherPlayer(player.getUUID())), false);
        source.sendSuccess(() -> Component.literal("Link Level: " + link.getLevel()), false);
        source.sendSuccess(() -> Component.literal("Can Upgrade: " + link.canUpgrade()), false);
        source.sendSuccess(() -> Component.literal("Can Downgrade: " + link.canDowngrade()), false);
        source.sendSuccess(() -> Component.literal("Upgrade Available: " + link.canUpgrade()), false);
        source.sendSuccess(() -> Component.literal("Upgrade Item Required: " + Config.UPGRADE_ITEM_CONSUME.get()), false);
        source.sendSuccess(() -> Component.literal("Link: active=" + link.active()
                + ", createTime=" + link.createTime()), false);
        return 1;
    }
}

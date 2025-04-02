package net.beeboyd.beeserverutilities.deathmessage;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.beeboyd.beeserverutilities.BeeServerUtilites;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import com.mojang.brigadier.Command;

public class DeathMessageCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("deathmessage")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("on")
                .executes(DeathMessageCommand::executeOn)
            )
            .then(Commands.literal("off")
                .executes(DeathMessageCommand::executeOff)
            )
            .then(Commands.literal("reload")
                .executes(DeathMessageCommand::executeReload)
            )
        );
    }

    private static int executeOn(CommandContext<CommandSourceStack> context) {
        DeathMessageConfig.setEnabled(true);
        context.getSource().sendSuccess(Component.literal("Death messages enabled.").withStyle(ChatFormatting.GREEN), true);
        BeeServerUtilites.LOGGER.info("Death messages enabled.");
        return Command.SINGLE_SUCCESS;
    }

    private static int executeOff(CommandContext<CommandSourceStack> context) {
        DeathMessageConfig.setEnabled(false);
        context.getSource().sendSuccess(Component.literal("Death messages disabled.").withStyle(ChatFormatting.RED), true);
        BeeServerUtilites.LOGGER.info("Death messages disabled.");
        return Command.SINGLE_SUCCESS;
    }

    private static int executeReload(CommandContext<CommandSourceStack> context) {
        DeathMessageConfig.reload();
        context.getSource().sendSuccess(Component.literal("Death message config reloaded."), true);
        BeeServerUtilites.LOGGER.info("Death message config reloaded.");
        return Command.SINGLE_SUCCESS;
    }
}

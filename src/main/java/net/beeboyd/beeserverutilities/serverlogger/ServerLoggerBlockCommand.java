package net.beeboyd.beeserverutilities.serverlogger;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.beeboyd.beeserverutilities.BeeServerUtilities;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ServerLoggerBlockCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("serverlogger")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("block")
                        .then(Commands.literal("add")
                                .then(Commands.argument("x", IntegerArgumentType.integer())
                                        .then(Commands.argument("y", IntegerArgumentType.integer())
                                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                                        .then(Commands.argument("nameForDetails", StringArgumentType.greedyString())
                                                                .executes(ServerLoggerBlockCommand::executeBlockAdd)
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("remove")
                                .then(Commands.argument("nameForDetails", StringArgumentType.greedyString())
                                        .executes(ServerLoggerBlockCommand::executeBlockRemove)
                                )
                        )
                        .then(Commands.literal("list")
                                .executes(ServerLoggerBlockCommand::executeBlockList)
                        )
                )
        );
    }

    private static int executeBlockAdd(CommandContext<CommandSourceStack> context) {
        int x = IntegerArgumentType.getInteger(context, "x");
        int y = IntegerArgumentType.getInteger(context, "y");
        int z = IntegerArgumentType.getInteger(context, "z");
        String detailName = StringArgumentType.getString(context, "nameForDetails").trim();

        EnteredBlockTrigger trigger = new EnteredBlockTrigger(x, y, z, detailName);
        ServerLoggerConfig.get().enteredBlockTriggers.add(trigger);
        ServerLoggerConfig.save();
        context.getSource().sendSuccess(Component.literal("Added block trigger: " + trigger.toString()).withStyle(ChatFormatting.GREEN), true);
        BeeServerUtilities.LOGGER.info("Added block trigger: " + trigger.toString());
        return Command.SINGLE_SUCCESS;
    }

    private static int executeBlockRemove(CommandContext<CommandSourceStack> context) {
        String detailName = StringArgumentType.getString(context, "nameForDetails").trim();
        boolean removed = ServerLoggerConfig.get().enteredBlockTriggers.removeIf(trigger -> trigger.detailName.equalsIgnoreCase(detailName));
        if (removed) {
            ServerLoggerConfig.save();
            context.getSource().sendSuccess(Component.literal("Removed block trigger(s) with name: " + detailName).withStyle(ChatFormatting.GREEN), true);
            BeeServerUtilities.LOGGER.info("Removed block trigger(s) with name: " + detailName);
        } else {
            context.getSource().sendSuccess(Component.literal("No block trigger found with name: " + detailName).withStyle(ChatFormatting.RED), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int executeBlockList(CommandContext<CommandSourceStack> context) {
        StringBuilder sb = new StringBuilder("Block Triggers:\n");
        for (EnteredBlockTrigger trigger : ServerLoggerConfig.get().enteredBlockTriggers) {
            sb.append(trigger.toString()).append("\n");
        }
        context.getSource().sendSuccess(Component.literal(sb.toString()).withStyle(ChatFormatting.AQUA), false);
        return Command.SINGLE_SUCCESS;
    }
}
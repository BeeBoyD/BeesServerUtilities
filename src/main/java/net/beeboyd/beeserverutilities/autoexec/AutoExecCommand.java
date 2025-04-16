package net.beeboyd.beeserverutilities.autoexec;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.beeboyd.beeserverutilities.BeeServerUtilities;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AutoExecCommand {

    // Suggestion provider for schedule types.
    private static final SuggestionProvider<CommandSourceStack> SCHEDULE_TYPE_SUGGESTIONS = (context, builder) -> {
        for (AutoExecScheduleType type : AutoExecScheduleType.values()) {
            builder.suggest(type.name());
        }
        return builder.buildFuture();
    };

    // Suggestion provider for autoexec names.
    private static final SuggestionProvider<CommandSourceStack> AUTOEXEC_NAME_SUGGESTIONS = (context, builder) -> {
        List<String> names = AutoExecManager.getRules().stream()
                .map(rule -> rule.name)
                .distinct()
                .collect(Collectors.toList());
        for (String name : names) {
            builder.suggest(name);
        }
        return builder.buildFuture();
    };

    // Suggestion provider for target argument based on schedule type.
    private static SuggestionProvider<CommandSourceStack> targetSuggestions(String scheduleTypeStr) {
        return (context, builder) -> {
            AutoExecScheduleType scheduleType;
            try {
                scheduleType = AutoExecScheduleType.valueOf(scheduleTypeStr.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return builder.buildFuture();
            }
            // For player-related events, suggest online player names.
            if (scheduleType == AutoExecScheduleType.ON_PLAYER_JOIN ||
                scheduleType == AutoExecScheduleType.ON_PLAYER_LEAVE ||
                scheduleType == AutoExecScheduleType.ON_PLAYER_DEATH) {
                List<String> players = new ArrayList<>(context.getSource().getOnlinePlayerNames());
                for (String player : players) {
                    builder.suggest(player);
                }
            }
            // For time intervals, suggest common intervals.
            else if (scheduleType == AutoExecScheduleType.ON_TIME_INTERVAL) {
                String[] intervals = {"600", "1200", "2400"};
                for (String s : intervals) {
                    builder.suggest(s);
                }
            }
            return builder.buildFuture();
        };
    };

    // Suggestion provider for command argument.
    private static final SuggestionProvider<CommandSourceStack> COMMAND_SUGGESTIONS = (context, builder) -> {
        String[] commonCommands = {"/say", "/give", "/tp", "/kick", "/ban"};
        for (String cmd : commonCommands) {
            builder.suggest(cmd);
        }
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("autoexec")
                .requires(source -> source.hasPermission(4))
                // /autoexec add <name> <scheduleType> <target> <command>
                .then(Commands.literal("add")
                    .then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("scheduleType", StringArgumentType.word())
                            .suggests(SCHEDULE_TYPE_SUGGESTIONS)
                            .then(Commands.argument("target", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    String scheduleTypeStr = context.getArgument("scheduleType", String.class);
                                    return targetSuggestions(scheduleTypeStr).getSuggestions(context, builder);
                                })
                                .then(Commands.argument("command", StringArgumentType.greedyString())
                                    .suggests(COMMAND_SUGGESTIONS)
                                    .executes(context -> executeAdd(context))
                                )
                            )
                        )
                    )
                )
                // /autoexec remove <name> <scheduleType> <target> <command>
                .then(Commands.literal("remove")
                    .then(Commands.argument("name", StringArgumentType.word())
                        .suggests(AUTOEXEC_NAME_SUGGESTIONS)
                        .then(Commands.argument("scheduleType", StringArgumentType.word())
                            .suggests(SCHEDULE_TYPE_SUGGESTIONS)
                            .then(Commands.argument("target", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    String scheduleTypeStr = context.getArgument("scheduleType", String.class);
                                    return targetSuggestions(scheduleTypeStr).getSuggestions(context, builder);
                                })
                                .then(Commands.argument("command", StringArgumentType.greedyString())
                                    .suggests(COMMAND_SUGGESTIONS)
                                    .executes(context -> executeRemove(context))
                                )
                            )
                        )
                    )
                )
                // /autoexec reload – dynamically reload the configuration
                .then(Commands.literal("reload")
                    .executes(context -> executeReload(context))
                )
                // /autoexec list – list all current autoexec rules
                .then(Commands.literal("list")
                    .executes(context -> executeList(context))
                )
        );
    }

    private static int executeAdd(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name").trim();
        String scheduleTypeStr = StringArgumentType.getString(context, "scheduleType");
        String target = StringArgumentType.getString(context, "target").trim();
        String command = StringArgumentType.getString(context, "command").trim();

        AutoExecScheduleType scheduleType = parseScheduleType(scheduleTypeStr, context.getSource());
        if (scheduleType == null) {
            return Command.SINGLE_SUCCESS;
        }
        if (name.isEmpty()) {
            context.getSource().sendFailure(Component.literal("Autoexec name cannot be empty."));
            return Command.SINGLE_SUCCESS;
        }
        if (target.isEmpty()) {
            context.getSource().sendFailure(Component.literal("Target cannot be empty."));
            return Command.SINGLE_SUCCESS;
        }
        if (command.isEmpty()) {
            context.getSource().sendFailure(Component.literal("Command cannot be empty."));
            return Command.SINGLE_SUCCESS;
        }
        if (scheduleType == AutoExecScheduleType.ON_TIME_INTERVAL) {
            try {
                long interval = Long.parseLong(target);
                if (interval <= 0) {
                    context.getSource().sendFailure(Component.literal("Time interval must be positive."));
                    return Command.SINGLE_SUCCESS;
                }
            } catch (NumberFormatException e) {
                context.getSource().sendFailure(Component.literal("Time interval must be a valid number."));
                return Command.SINGLE_SUCCESS;
            }
        }
        if (!command.startsWith("/")) {
            command = "/" + command;
        }
        AutoExecManager.addRule(name, scheduleType, target, command);
        context.getSource().sendSuccess(Component.literal("Added autoexec rule [" + name + "]: " + scheduleType + " " + target + " -> " + command), true);
        BeeServerUtilities.LOGGER.info("Added autoexec rule [{}]: {} {} -> {}", name, scheduleType, target, command);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeRemove(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name").trim();
        String scheduleTypeStr = StringArgumentType.getString(context, "scheduleType");
        String target = StringArgumentType.getString(context, "target").trim();
        String command = StringArgumentType.getString(context, "command").trim();

        AutoExecScheduleType scheduleType = parseScheduleType(scheduleTypeStr, context.getSource());
        if (scheduleType == null) {
            return Command.SINGLE_SUCCESS;
        }
        if (!command.startsWith("/")) {
            command = "/" + command;
        }
        boolean removed = AutoExecManager.removeRule(name, scheduleType, target, command);
        if (removed) {
            context.getSource().sendSuccess(Component.literal("Removed autoexec rule [" + name + "]."), true);
            BeeServerUtilities.LOGGER.info("Removed autoexec rule [{}].", name);
        } else {
            context.getSource().sendFailure(Component.literal("No matching rule found to remove."));
            BeeServerUtilities.LOGGER.warn("Failed to remove autoexec rule [{}]: rule not found.", name);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int executeReload(CommandContext<CommandSourceStack> context) {
        AutoExecManager.loadRules();
        context.getSource().sendSuccess(Component.literal("Reloaded autoexec configuration."), true);
        BeeServerUtilities.LOGGER.info("Reloaded autoexec configuration.");
        return Command.SINGLE_SUCCESS;
    }

    private static int executeList(CommandContext<CommandSourceStack> context) {
        StringBuilder list = new StringBuilder("Autoexec Rules:\n");
        for (AutoExecManager.AutoExecRule rule : AutoExecManager.getRules()) {
            list.append(rule.name)
                .append(" - ")
                .append(rule.scheduleType)
                .append(" ")
                .append(rule.target)
                .append(" -> ")
                .append(rule.command)
                .append("\n");
        }
        context.getSource().sendSuccess(Component.literal(list.toString()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static AutoExecScheduleType parseScheduleType(String input, CommandSourceStack source) {
        try {
            return AutoExecScheduleType.valueOf(input.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("Invalid schedule type. Use one of: " + java.util.Arrays.toString(AutoExecScheduleType.values())));
            return null;
        }
    }
}

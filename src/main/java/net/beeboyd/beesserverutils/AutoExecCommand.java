package net.beeboyd.beesserverutils;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
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

    // Suggestion provider for autoexec names when removing.
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
            // If player events, suggest online player names.
            if (scheduleType == AutoExecScheduleType.ON_PLAYER_JOIN || scheduleType == AutoExecScheduleType.ON_PLAYER_LEAVE) {
                // Wrap the collection into an ArrayList to ensure it's a List.
                List<String> players = new ArrayList<>(context.getSource().getOnlinePlayerNames());
                for (String player : players) {
                    builder.suggest(player);
                }
            }
            // If time interval, suggest common intervals.
            else if (scheduleType == AutoExecScheduleType.ON_TIME_INTERVAL) {
                String[] intervals = {"600", "1200", "2400"};
                for (String s : intervals) {
                    builder.suggest(s);
                }
            }
            return builder.buildFuture();
        };
    }

    // Suggestion provider for command argument.
    private static final SuggestionProvider<CommandSourceStack> COMMAND_SUGGESTIONS = (context, builder) -> {
        // This is a basic fixed list; you can extend it as needed.
        String[] commonCommands = {"/say", "/give", "/tp", "/kick", "/ban"};
        for (String cmd : commonCommands) {
            builder.suggest(cmd);
        }
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("autoexec")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("add")
                    .then(Commands.argument("name", StringArgumentType.word())
                        // No suggestions for "name"—it’s free-form.
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
                    context.getSource().sendFailure(Component.literal("Time interval must be a positive number."));
                    return Command.SINGLE_SUCCESS;
                }
            } catch (NumberFormatException e) {
                context.getSource().sendFailure(Component.literal("Time interval must be a valid number."));
                return Command.SINGLE_SUCCESS;
            }
        }
        // Ensure command starts with a slash.
        if (!command.startsWith("/")) {
            command = "/" + command;
        }
        AutoExecManager.addRule(name, scheduleType, target, command);
        context.getSource().sendSuccess(
            Component.literal("Added autoexec rule [" + name + "]: " + scheduleType + " " + target + " -> " + command),
            true
        );
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
            context.getSource().sendSuccess(
                Component.literal("Removed autoexec rule [" + name + "]."),
                true
            );
        } else {
            context.getSource().sendFailure(
                Component.literal("No matching rule found to remove.")
            );
        }
        return Command.SINGLE_SUCCESS;
    }

    private static AutoExecScheduleType parseScheduleType(String input, CommandSourceStack source) {
        try {
            return AutoExecScheduleType.valueOf(input.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("Invalid schedule type. Use one of: ON_PLAYER_JOIN, ON_PLAYER_LEAVE, ON_SERVER_STARTUP, ON_SERVER_SHUTDOWN, ON_TIME_INTERVAL"));
            return null;
        }
    }
}

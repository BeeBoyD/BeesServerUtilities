package net.beeboyd.beeserverutilities.nametag;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Registers the /nametag command and its subcommands.
 */
public class NametagCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nametag")
                // Subcommands under /nametag team
                .then(Commands.literal("team")
                        // /nametag team create {nameForTeam} {color}
                        .then(Commands.literal("create")
                                .then(Commands.argument("teamName", StringArgumentType.word())
                                        .then(Commands.argument("color", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    String teamName = StringArgumentType.getString(ctx, "teamName");
                                                    String color = StringArgumentType.getString(ctx, "color");
                                                    try {
                                                        NametagManager.createTeam(ctx.getSource(), teamName, color);
                                                        ctx.getSource().sendSuccess(Component.literal("Team " + teamName + " created with color " + color), true);
                                                    } catch (Exception e) {
                                                        ctx.getSource().sendFailure(Component.literal("Error creating team: " + e.getMessage()));
                                                    }
                                                    return 1;
                                                })
                                        )
                                )
                        )
                        // /nametag team delete [autocomplete nameForTeam if allowed]
                        .then(Commands.literal("delete")
                                // No argument passed: use the caller's team if not admin
                                .executes(ctx -> {
                                    String teamName = NametagManager.getPlayerTeam(ctx.getSource().getPlayerOrException());
                                    try {
                                        NametagManager.deleteTeam(ctx.getSource(), teamName);
                                        ctx.getSource().sendSuccess(Component.literal("Team " + teamName + " deleted."), true);
                                    } catch (Exception e) {
                                        ctx.getSource().sendFailure(Component.literal("Error deleting team: " + e.getMessage()));
                                    }
                                    return 1;
                                })
                                // With argument (admin)
                                .then(Commands.argument("teamName", StringArgumentType.word())
                                        .executes(ctx -> {
                                            String teamName = StringArgumentType.getString(ctx, "teamName");
                                            try {
                                                NametagManager.deleteTeam(ctx.getSource(), teamName);
                                                ctx.getSource().sendSuccess(Component.literal("Team " + teamName + " deleted."), true);
                                            } catch (Exception e) {
                                                ctx.getSource().sendFailure(Component.literal("Error deleting team: " + e.getMessage()));
                                            }
                                            return 1;
                                        })
                                )
                        )
                        // /nametag team myteam ...
                        .then(Commands.literal("myteam")
                                // /nametag team myteam invite {username}
                                .then(Commands.literal("invite")
                                        .then(Commands.argument("username", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    String username = StringArgumentType.getString(ctx, "username");
                                                    try {
                                                        NametagManager.invitePlayer(ctx.getSource(), username);
                                                        ctx.getSource().sendSuccess(Component.literal("Invitation sent to " + username), true);
                                                    } catch (Exception e) {
                                                        ctx.getSource().sendFailure(Component.literal("Error inviting player: " + e.getMessage()));
                                                    }
                                                    return 1;
                                                })
                                        )
                                )
                                // /nametag team myteam kick {username}
                                .then(Commands.literal("kick")
                                        .then(Commands.argument("username", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    String username = StringArgumentType.getString(ctx, "username");
                                                    try {
                                                        NametagManager.kickPlayer(ctx.getSource(), username);
                                                        ctx.getSource().sendSuccess(Component.literal("Player " + username + " kicked from team."), true);
                                                    } catch (Exception e) {
                                                        ctx.getSource().sendFailure(Component.literal("Error kicking player: " + e.getMessage()));
                                                    }
                                                    return 1;
                                                })
                                        )
                                )
                                // /nametag team prefix {teamPrefixString} {prefixColor}
                                .then(Commands.literal("prefix")
                                        .then(Commands.argument("teamPrefix", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    String fullArg = StringArgumentType.getString(ctx, "teamPrefix");
                                                    String[] parts = fullArg.split(" ");
                                                    if (parts.length < 2) {
                                                        ctx.getSource().sendFailure(Component.literal("Usage: /nametag team prefix <teamPrefix> <prefixColor>"));
                                                        return 0;
                                                    }
                                                    StringBuilder prefixBuilder = new StringBuilder();
                                                    for (int i = 0; i < parts.length - 1; i++) {
                                                        prefixBuilder.append(parts[i]).append(" ");
                                                    }
                                                    String prefixStr = prefixBuilder.toString().trim();
                                                    String prefixColor = parts[parts.length - 1];
                                                    try {
                                                        NametagManager.setTeamPrefix(ctx.getSource(), prefixStr, prefixColor);
                                                        ctx.getSource().sendSuccess(Component.literal("Team prefix set to [" + prefixStr.toUpperCase() + "] with color " + prefixColor), true);
                                                    } catch (Exception e) {
                                                        ctx.getSource().sendFailure(Component.literal("Error setting team prefix: " + e.getMessage()));
                                                    }
                                                    return 1;
                                                })
                                        )
                                )
                        )
                        // /nametag team admin ...
                        .then(Commands.literal("admin")
                                        // Example admin subcommand: set a player's team membership
                                        .then(Commands.literal("setteam")
                                                .then(Commands.argument("username", StringArgumentType.word())
                                                        .then(Commands.argument("teamName", StringArgumentType.word())
                                                                .executes(ctx -> {
                                                                    String username = StringArgumentType.getString(ctx, "username");
                                                                    String teamName = StringArgumentType.getString(ctx, "teamName");
                                                                    try {
                                                                        NametagManager.adminSetTeam(ctx.getSource(), username, teamName);
                                                                        ctx.getSource().sendSuccess(Component.literal("Admin set team of " + username + " to " + teamName), true);
                                                                    } catch (Exception e) {
                                                                        ctx.getSource().sendFailure(Component.literal("Error in admin set team: " + e.getMessage()));
                                                                    }
                                                                    return 1;
                                                                })
                                                        )
                                                )
                                        )
                                // Additional admin subcommands can be added here.
                        )
                )
                // /nametag myprefix {ownPrefixString} {prefixColor}
                .then(Commands.literal("myprefix")
                        .then(Commands.argument("ownPrefix", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String fullArg = StringArgumentType.getString(ctx, "ownPrefix");
                                    String[] parts = fullArg.split(" ");
                                    if (parts.length < 2) {
                                        ctx.getSource().sendFailure(Component.literal("Usage: /nametag myprefix <prefix> <color>"));
                                        return 0;
                                    }
                                    StringBuilder prefixBuilder = new StringBuilder();
                                    for (int i = 0; i < parts.length - 1; i++) {
                                        prefixBuilder.append(parts[i]).append(" ");
                                    }
                                    String prefixStr = prefixBuilder.toString().trim();
                                    String prefixColor = parts[parts.length - 1];
                                    try {
                                        NametagManager.setOwnPrefix(ctx.getSource(), prefixStr, prefixColor);
                                        ctx.getSource().sendSuccess(Component.literal("Your prefix set to [" + prefixStr.toUpperCase() + "] with color " + prefixColor), true);
                                    } catch (Exception e) {
                                        ctx.getSource().sendFailure(Component.literal("Error setting your prefix: " + e.getMessage()));
                                    }
                                    return 1;
                                })
                        )
                )
                // /nametag admin setplayerprefix {username} {prefix} {color}
                .then(Commands.literal("admin")
                        .then(Commands.literal("setplayerprefix")
                                .then(Commands.argument("username", StringArgumentType.word())
                                        .then(Commands.argument("prefix", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    String username = StringArgumentType.getString(ctx, "username");
                                                    String fullArg = StringArgumentType.getString(ctx, "prefix");
                                                    String[] parts = fullArg.split(" ");
                                                    if (parts.length < 2) {
                                                        ctx.getSource().sendFailure(Component.literal("Usage: /nametag admin setplayerprefix <username> <prefix> <color>"));
                                                        return 0;
                                                    }
                                                    StringBuilder prefixBuilder = new StringBuilder();
                                                    for (int i = 0; i < parts.length - 1; i++) {
                                                        prefixBuilder.append(parts[i]).append(" ");
                                                    }
                                                    String prefixStr = prefixBuilder.toString().trim();
                                                    String prefixColor = parts[parts.length - 1];
                                                    try {
                                                        NametagManager.adminSetPlayerPrefix(ctx.getSource(), username, prefixStr, prefixColor);
                                                        ctx.getSource().sendSuccess(Component.literal("Admin set " + username + "'s prefix to [" + prefixStr.toUpperCase() + "] with color " + prefixColor), true);
                                                    } catch (Exception e) {
                                                        ctx.getSource().sendFailure(Component.literal("Error in admin setting player prefix: " + e.getMessage()));
                                                    }
                                                    return 1;
                                                })
                                        )
                                )
                        )
                )
        );
    }
}

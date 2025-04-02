package net.beeboyd.beeserverutilities.serverlogger;

import net.beeboyd.beeserverutilities.BeeServerUtilites;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerLogger {
    private static final File LOG_FILE;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        // Create logs folder if it doesn't exist
        File logDir = new File("logs");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        LOG_FILE = new File(logDir, "beelogged.log");
        try {
            if (!LOG_FILE.exists()) {
                LOG_FILE.createNewFile();
            }
        } catch (IOException e) {
            BeeServerUtilites.LOGGER.error("Failed to create beelogged.log: " + e.getMessage());
        }
    }

    /**
     * Logs an event with its details to the log file.
     *
     * @param eventName  The event identifier (e.g., "placed_tnt")
     * @param playerName The player's name (or "N/A")
     * @param details    Additional event details (e.g., coordinates)
     */
    public static void logEvent(String eventName, String playerName, String details) {
        if (!ServerLoggerConfig.get().shouldLogEvent(eventName)) return;
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logEntry = String.format("[%s] Event: %s | Player: %s | Details: %s", timestamp, eventName, playerName, details);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(logEntry);
            writer.newLine();
        } catch (IOException e) {
            BeeServerUtilites.LOGGER.error("Failed to write to beelogged.log: " + e.getMessage());
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("serverlogger")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("on")
                        .executes(ctx -> {
                            ServerLoggerConfig.setEnabled(true);
                            ctx.getSource().sendSuccess(Component.literal("Server logging enabled.").withStyle(ChatFormatting.GREEN), true);
                            BeeServerUtilites.LOGGER.info("Server logging enabled.");
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(Commands.literal("off")
                        .executes(ctx -> {
                            ServerLoggerConfig.setEnabled(false);
                            ctx.getSource().sendSuccess(Component.literal("Server logging disabled.").withStyle(ChatFormatting.RED), true);
                            BeeServerUtilites.LOGGER.info("Server logging disabled.");
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(Commands.literal("reload")
                        .executes(ctx -> {
                            ServerLoggerConfig.reload();
                            ctx.getSource().sendSuccess(Component.literal("Server logger configuration reloaded."), true);
                            BeeServerUtilites.LOGGER.info("Server logger configuration reloaded.");
                            return Command.SINGLE_SUCCESS;
                        }))
        );
    }
}

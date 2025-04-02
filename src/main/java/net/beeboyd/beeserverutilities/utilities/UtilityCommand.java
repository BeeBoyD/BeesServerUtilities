package net.beeboyd.beeserverutilities.utilities;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.*;

public class UtilityCommand {
    private static final long startTime = System.currentTimeMillis();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static ScheduledFuture<?> cpuMonitorTask;
    private static Component lastCpuMessage;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("utility")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("tps").executes(UtilityCommand::executeTPS))
            .then(Commands.literal("cpu").executes(UtilityCommand::executeCPU))
            .then(Commands.literal("cpu")
                .then(Commands.argument("monitor_time", IntegerArgumentType.integer(1, 60))
                    .executes(ctx -> executeCPUMonitor(ctx, IntegerArgumentType.getInteger(ctx, "monitor_time")))))
            .then(Commands.literal("uptime").executes(UtilityCommand::executeUptime))
        );
    }

    private static int executeTPS(CommandContext<CommandSourceStack> context) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            context.getSource().sendFailure(Component.literal("Error: Could not retrieve TPS data."));
            return Command.SINGLE_SUCCESS;
        }

        double tps = Math.min(20.0, 1000.0 / Math.max(50.0, server.getAverageTickTime()));
        ChatFormatting color = (tps > 18) ? ChatFormatting.GREEN : (tps > 10) ? ChatFormatting.YELLOW : ChatFormatting.RED;

        context.getSource().sendSuccess(
            Component.literal("Current TPS: ").append(Component.literal(String.format("%.2f", tps)).withStyle(color)),
            true
        );

        return Command.SINGLE_SUCCESS;
    }

    private static int executeCPU(CommandContext<CommandSourceStack> context) {
        String stats = getCPUAndRAMUsage();
        context.getSource().sendSuccess(Component.literal(stats).withStyle(ChatFormatting.AQUA), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeCPUMonitor(CommandContext<CommandSourceStack> context, int interval) {
        CommandSourceStack source = context.getSource();

        // If a monitoring task is already running, cancel it first
        if (cpuMonitorTask != null && !cpuMonitorTask.isCancelled()) {
            cpuMonitorTask.cancel(false);
        }

        // Send initial CPU message and store the reference
        lastCpuMessage = Component.literal(getCPUAndRAMUsage()).withStyle(ChatFormatting.DARK_AQUA);
        source.sendSuccess(lastCpuMessage, false);

        // Schedule a new task to update the message in real time
        cpuMonitorTask = scheduler.scheduleAtFixedRate(() -> {
            if (source.getEntity() != null) {
                lastCpuMessage = Component.literal(getCPUAndRAMUsage()).withStyle(ChatFormatting.DARK_AQUA);
                source.sendSuccess(lastCpuMessage, false);
            }
        }, 0, interval, TimeUnit.SECONDS);

        source.sendSuccess(Component.literal("CPU & RAM monitoring started. Updates every " + interval + " seconds.").withStyle(ChatFormatting.GREEN), false);
        return Command.SINGLE_SUCCESS;
    }

    private static String getCPUAndRAMUsage() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double cpuLoad = osBean.getSystemLoadAverage();
        long totalMemory = Runtime.getRuntime().maxMemory();
        long usedMemory = totalMemory - Runtime.getRuntime().freeMemory();
        long allocatedMemory = Runtime.getRuntime().totalMemory();

        return String.format("CPU Load: %.2f%% | RAM Usage: %d MB / %d MB (Allocated: %d MB)",
            cpuLoad * 100, usedMemory / (1024 * 1024), totalMemory / (1024 * 1024), allocatedMemory / (1024 * 1024));
    }

    private static int executeUptime(CommandContext<CommandSourceStack> context) {
        long currentTime = System.currentTimeMillis();
        long uptimeMillis = currentTime - startTime;
        long seconds = (uptimeMillis / 1000) % 60;
        long minutes = (uptimeMillis / (1000 * 60)) % 60;
        long hours = (uptimeMillis / (1000 * 60 * 60)) % 24;
        long days = uptimeMillis / (1000 * 60 * 60 * 24);

        String uptimeString;
        if (days > 0) {
            uptimeString = String.format("%d days, %d hours, %d minutes, %d seconds", days, hours, minutes, seconds);
        } else {
            uptimeString = String.format("%d hours, %d minutes, %d seconds", hours, minutes, seconds);
        }

        context.getSource().sendSuccess(Component.literal("Server uptime: " + uptimeString).withStyle(ChatFormatting.YELLOW), true);
        return Command.SINGLE_SUCCESS;
    }
}

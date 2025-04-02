package net.beeboyd.beeserverutilities.serverutils;

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
import com.sun.management.OperatingSystemMXBean;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerStatsCommand {
    private static final long startTime = System.currentTimeMillis();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    // We'll use this for non-monitor CPU command, if needed.
    private static ScheduledFuture<?> cpuMonitorTask;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("serverstats")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("tps").executes(ServerStatsCommand::executeTPS))
                .then(Commands.literal("cpu")
                        .executes(ServerStatsCommand::executeCPU)
                )
                .then(Commands.literal("cpu")
                        .then(Commands.argument("monitor_time", IntegerArgumentType.integer(1, 300))
                                .executes(ctx -> executeCPUMonitor(ctx, IntegerArgumentType.getInteger(ctx, "monitor_time")))
                        )
                )
                .then(Commands.literal("uptime").executes(ServerStatsCommand::executeUptime))
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
        Component tpsComponent = Component.literal(String.format("%.2f", tps)).withStyle(color);
        context.getSource().sendSuccess(Component.literal("Current TPS: ").append(tpsComponent), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeCPU(CommandContext<CommandSourceStack> context) {
        String stats = getCPUAndRAMUsage();
        context.getSource().sendSuccess(Component.literal(stats).withStyle(ChatFormatting.AQUA), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeCPUMonitor(CommandContext<CommandSourceStack> context, int monitorTime) {
        CommandSourceStack source = context.getSource();

        // If a previous monitor task is running, cancel it.
        if (cpuMonitorTask != null && !cpuMonitorTask.isCancelled()) {
            cpuMonitorTask.cancel(false);
        }

        // We'll update every 20 ticks (approximately every 1 second).
        final AtomicInteger counter = new AtomicInteger(0);
        final int totalSeconds = monitorTime;
        // Use an array to hold the ScheduledFuture reference for cancellation within the lambda.
        final ScheduledFuture<?>[] futureHolder = new ScheduledFuture<?>[1];
        futureHolder[0] = scheduler.scheduleAtFixedRate(() -> {
            int elapsed = counter.incrementAndGet();
            // Update the CPU and RAM usage message.
            String stats = getCPUAndRAMUsage();
            source.sendSuccess(Component.literal(stats).withStyle(ChatFormatting.DARK_AQUA), false);
            if (elapsed >= totalSeconds) {
                futureHolder[0].cancel(false);
                source.sendSuccess(Component.literal("CPU & RAM monitoring ended.").withStyle(ChatFormatting.GREEN), false);
            }
        }, 0, 1, TimeUnit.SECONDS); // 1 second intervals ~20 ticks

        source.sendSuccess(Component.literal("CPU & RAM monitoring started for " + totalSeconds + " seconds.").withStyle(ChatFormatting.GREEN), false);
        return Command.SINGLE_SUCCESS;
    }

    private static String getCPUAndRAMUsage() {
        // Using OperatingSystemMXBean for CPU load as before.
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        double cpuLoad = osBean.getSystemCpuLoad();

        // Use Runtime for Minecraft's memory usage.
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory(); // Maximum memory the JVM can use (allocated)
        long totalMemory = runtime.totalMemory(); // Total memory currently in use by the JVM
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        // Convert bytes to MB.
        long maxMB = maxMemory / (1024 * 1024);
        long usedMB = usedMemory / (1024 * 1024);

        return String.format("CPU Load: %.2f%% | RAM Usage: %d MB / %d MB", cpuLoad * 100, usedMB, maxMB);
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

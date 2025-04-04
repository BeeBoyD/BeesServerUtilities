package net.beeboyd.beeserverutilities.serverstats;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.beeboyd.beeserverutilities.BeeServerUtilites;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;

public class ServerInfoCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("serverinfo")
                .requires(source -> source.hasPermission(2))
                .executes(ServerInfoCommand::executeServerInfo)
        );
    }

    private static int executeServerInfo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        // Get OS information
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String osArch = System.getProperty("os.arch");
        String osInfo = osName + " " + osVersion + " (" + osArch + ")";

        // Attempt to get CPU model (works on Windows via environment variable; otherwise, unknown)
        String cpuModel = System.getenv("PROCESSOR_IDENTIFIER");
        if (cpuModel == null || cpuModel.isEmpty()) {
            cpuModel = "Unknown";
        }

        // Get RAM capacity using OperatingSystemMXBean
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long totalPhysicalMemoryBytes = osBean.getTotalPhysicalMemorySize();
        long totalPhysicalMemoryMB = totalPhysicalMemoryBytes / (1024 * 1024);
        String ramCapacity = totalPhysicalMemoryMB + " MB";

        // RAM model is not available via Java, so we use a placeholder.
        String ramModel = "Not Available";

        String info = String.format("Server OS: %s\nCPU Model: %s\nRAM Capacity: %s\nRAM Model: %s",
                osInfo, cpuModel, ramCapacity, ramModel);

        source.sendSuccess(Component.literal(info).withStyle(ChatFormatting.GOLD), false);
        BeeServerUtilites.LOGGER.info("Server Info:\n" + info);

        return Command.SINGLE_SUCCESS;
    }
}

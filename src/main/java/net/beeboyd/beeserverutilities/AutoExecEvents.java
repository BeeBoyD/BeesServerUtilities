package net.beeboyd.beeserverutilities;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = BeeServerUtilites.MOD_ID)
public class AutoExecEvents {

    private static long serverTickCounter = 0;
    // Keeps track of the next tick for each ON_TIME_INTERVAL rule.
    private static final ConcurrentHashMap<AutoExecManager.AutoExecRule, Long> nextExecutionMap = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        AutoExecManager.getRules().stream()
                .filter(rule -> rule.scheduleType == AutoExecScheduleType.ON_SERVER_STARTUP)
                .forEach(rule -> runCommand(server, rule.command));
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        MinecraftServer server = event.getServer();
        AutoExecManager.getRules().stream()
                .filter(rule -> rule.scheduleType == AutoExecScheduleType.ON_SERVER_SHUTDOWN)
                .forEach(rule -> runCommand(server, rule.command));
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        MinecraftServer server = event.getEntity().getServer();
        if (server == null) return;
        String playerName = event.getEntity().getName().getString();
        AutoExecManager.getRules().stream()
                .filter(rule -> rule.scheduleType == AutoExecScheduleType.ON_PLAYER_JOIN)
                .filter(rule -> rule.target.equalsIgnoreCase(playerName) || rule.target.equalsIgnoreCase("any"))
                .forEach(rule -> runCommand(server, rule.command));
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        MinecraftServer server = event.getEntity().getServer();
        if (server == null) return;
        String playerName = event.getEntity().getName().getString();
        AutoExecManager.getRules().stream()
                .filter(rule -> rule.scheduleType == AutoExecScheduleType.ON_PLAYER_LEAVE)
                .filter(rule -> rule.target.equalsIgnoreCase(playerName) || rule.target.equalsIgnoreCase("any"))
                .forEach(rule -> runCommand(server, rule.command));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            return;
        }
        serverTickCounter++;
        MinecraftServer server = event.getServer();

        // Process ON_TIME_INTERVAL rules.
        for (AutoExecManager.AutoExecRule rule : AutoExecManager.getRules()) {
            if (rule.scheduleType == AutoExecScheduleType.ON_TIME_INTERVAL) {
                long interval;
                try {
                    interval = Long.parseLong(rule.target);
                } catch (NumberFormatException e) {
                    continue;
                }
                long nextExecution = nextExecutionMap.getOrDefault(rule, 0L);
                if (serverTickCounter >= nextExecution) {
                    runCommand(server, rule.command);
                    nextExecutionMap.put(rule, serverTickCounter + interval);
                }
            }
        }
    }

    private static void runCommand(MinecraftServer server, String command) {
        server.getCommands().performPrefixedCommand(
                server.createCommandSourceStack(),
                command
        );
    }
}

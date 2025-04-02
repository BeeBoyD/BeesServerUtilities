package net.beeboyd.beeserverutilities.autoexec;

import net.beeboyd.beeserverutilities.BeeServerUtilites;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = BeeServerUtilites.MOD_ID)
public class AutoExecEvents {

    private static long serverTickCounter = 0;
    // Map to track the next tick for ON_TIME_INTERVAL rules.
    private static final ConcurrentHashMap<AutoExecManager.AutoExecRule, Long> nextExecutionMap = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        for (AutoExecManager.AutoExecRule rule : AutoExecManager.getRules()) {
            if (rule.scheduleType == AutoExecScheduleType.ON_SERVER_STARTUP) {
                runCommand(server, rule.command, "Server Startup");
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        MinecraftServer server = event.getServer();
        for (AutoExecManager.AutoExecRule rule : AutoExecManager.getRules()) {
            if (rule.scheduleType == AutoExecScheduleType.ON_SERVER_SHUTDOWN) {
                runCommand(server, rule.command, "Server Shutdown");
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        MinecraftServer server = event.getEntity().getServer();
        if (server == null) return;
        String playerName = event.getEntity().getName().getString();
        for (AutoExecManager.AutoExecRule rule : AutoExecManager.getRules()) {
            if (rule.scheduleType == AutoExecScheduleType.ON_PLAYER_JOIN &&
                (rule.target.equalsIgnoreCase(playerName) || rule.target.equalsIgnoreCase("any"))) {
                runCommand(server, rule.command, "Player Join: " + playerName);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        MinecraftServer server = event.getEntity().getServer();
        if (server == null) return;
        String playerName = event.getEntity().getName().getString();
        for (AutoExecManager.AutoExecRule rule : AutoExecManager.getRules()) {
            if (rule.scheduleType == AutoExecScheduleType.ON_PLAYER_LEAVE &&
                (rule.target.equalsIgnoreCase(playerName) || rule.target.equalsIgnoreCase("any"))) {
                runCommand(server, rule.command, "Player Leave: " + playerName);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer)) return;
        ServerPlayer player = (ServerPlayer) event.getEntity();
        MinecraftServer server = player.getServer();
        if (server == null) return;
        String playerName = player.getName().getString();
        for (AutoExecManager.AutoExecRule rule : AutoExecManager.getRules()) {
            if (rule.scheduleType == AutoExecScheduleType.ON_PLAYER_DEATH &&
                (rule.target.equalsIgnoreCase(playerName) || rule.target.equalsIgnoreCase("any"))) {
                runCommand(server, rule.command, "Player Death: " + playerName);
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;
        serverTickCounter++;
        MinecraftServer server = event.getServer();
        for (AutoExecManager.AutoExecRule rule : AutoExecManager.getRules()) {
            if (rule.scheduleType == AutoExecScheduleType.ON_TIME_INTERVAL) {
                long interval;
                try {
                    interval = Long.parseLong(rule.target);
                } catch (NumberFormatException e) {
                    BeeServerUtilites.LOGGER.error("Invalid interval in rule [{}]: {}", rule.name, rule.target);
                    continue;
                }
                long nextExecution = nextExecutionMap.getOrDefault(rule, 0L);
                if (serverTickCounter >= nextExecution) {
                    runCommand(server, rule.command, "Time Interval Rule: " + rule.name);
                    nextExecutionMap.put(rule, serverTickCounter + interval);
                }
            }
        }
    }

    // (Custom events can be added here as needed.)

    private static void runCommand(MinecraftServer server, String command, String triggerInfo) {
        BeeServerUtilites.LOGGER.info("Executing command [{}] triggered by [{}].", command, triggerInfo);
        try {
            server.getCommands().performPrefixedCommand(
                    server.createCommandSourceStack(),
                    command
            );
        } catch (Exception e) {
            BeeServerUtilites.LOGGER.error("Error executing command [{}] for trigger [{}]: {}", command, triggerInfo, e.getMessage());
        }
    }
}

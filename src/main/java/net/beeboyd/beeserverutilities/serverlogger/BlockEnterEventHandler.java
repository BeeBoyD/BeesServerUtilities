package net.beeboyd.beeserverutilities.serverlogger;

import net.beeboyd.beeserverutilities.BeeServerUtilities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = BeeServerUtilities.MOD_ID)
public class BlockEnterEventHandler {

    // Map from player UUID to last detection timestamp (in milliseconds)
    private static final Map<UUID, Long> lastDetectionMap = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Process only at the END phase on the server side.
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide()) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        // If admins are excluded and the player has admin permissions, skip logging
        if (ServerLoggerConfig.get().excludeAdminsFromBlockTrigger && player.hasPermissions(2)) return;

        // Get current time and the configured cooldown delay
        long currentTime = System.currentTimeMillis();
        long cooldownDelay = ServerLoggerConfig.get().blockEnterCooldown;
        UUID playerId = player.getUUID();

        if (lastDetectionMap.containsKey(playerId)) {
            long lastTime = lastDetectionMap.get(playerId);
            if (currentTime - lastTime < cooldownDelay) {
                // Cooldown not expired; skip logging.
                return;
            }
        }

        // Get the player's current block coordinates (floored)
        BlockPos currentPos = new BlockPos((int) Math.floor(player.getX()),
                (int) Math.floor(player.getY()),
                (int) Math.floor(player.getZ()));

        // Check if currentPos matches any tracked block trigger
        for (EnteredBlockTrigger trigger : ServerLoggerConfig.get().enteredBlockTriggers) {
            if (trigger.matches(currentPos.getX(), currentPos.getY(), currentPos.getZ())) {
                ServerLogger.logEvent("entered_block", player.getScoreboardName(), trigger.detailName + " at " + currentPos);
                // Update the last detection time for this player.
                lastDetectionMap.put(playerId, currentTime);
                break; // Log once per tick per player.
            }
        }
    }
}

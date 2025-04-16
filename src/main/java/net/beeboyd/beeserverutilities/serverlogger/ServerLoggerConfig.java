package net.beeboyd.beeserverutilities.serverlogger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.beeboyd.beeserverutilities.BeeServerUtilities;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerLoggerConfig {
    private static final File CONFIG_FILE = new File("config/beeserverutilities/serverlogger/serverlogger_config.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static ServerLoggerConfig instance = new ServerLoggerConfig();

    // Global toggle
    public boolean enabled = true;

    // Event toggles – admins can turn each on or off
    public boolean logPlacedTNT         = true;
    public boolean logOpenedChest       = true;
    public boolean logBrokenBlock       = true;
    public boolean logCrafting          = true;
    public boolean logLogin             = true;
    public boolean logLogout            = true;
    public boolean logChat              = true;
    public boolean logItemPickup        = true;
    public boolean logItemDrop          = true;
    public boolean logPortalUse         = true;
    public boolean logDamage            = true;
    public boolean logExplosion         = true;
    public boolean logCommandUse        = true;
    public boolean logBlockInteraction  = true;
    public boolean logEntitySpawn       = true;
    public boolean logEntityKill        = true;
    public boolean logVillagerTrade     = true;
    public boolean logBlockBreak        = true;
    public boolean logSignEdit          = true;
    public boolean logFishCaught        = true;
    public boolean logSleep             = true;
    public boolean logBlockPlace        = true;
    public boolean logPistonExtend      = true;
    public boolean logPistonRetract     = true;
    public boolean logPotionUse         = true;
    public boolean logFlowerPick        = true;
    public boolean logWaterBucket       = true;
    public boolean logLavaBucket        = true;
    public boolean logCropHarvest       = true;
    public boolean logTrade             = true;

    // New: Player entered block event settings
    public boolean logPlayerEnteredBlock = true;
    public boolean excludeAdminsFromBlockTrigger = true;
    public long blockEnterCooldown = 10000;
    public List<EnteredBlockTrigger> enteredBlockTriggers = new ArrayList<>();

    public static ServerLoggerConfig get() {
        return instance;
    }

    public static void reload() {
        if (!CONFIG_FILE.exists()) {
            saveDefaults();
            return;
        }
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            instance = GSON.fromJson(reader, ServerLoggerConfig.class);
            BeeServerUtilities.LOGGER.info("ServerLoggerConfig reloaded.");
        } catch (IOException | JsonSyntaxException e) {
            BeeServerUtilities.LOGGER.error("Error reloading ServerLoggerConfig: " + e.getMessage());
        }
    }

    public static void saveDefaults() {
        instance = new ServerLoggerConfig();
        save();
    }

    public static void save() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(instance, writer);
            }
            BeeServerUtilities.LOGGER.info("ServerLoggerConfig saved.");
        } catch (IOException e) {
            BeeServerUtilities.LOGGER.error("Error saving ServerLoggerConfig: " + e.getMessage());
        }
    }

    public static void setEnabled(boolean value) {
        instance.enabled = value;
        save();
    }

    /**
     * Returns whether the given event (provided as a lowercase string) should be logged.
     */
    public boolean shouldLogEvent(String eventName) {
        if (!enabled) return false;
        switch (eventName) {
            case "placed_tnt":         return logPlacedTNT;
            case "opened_chest":       return logOpenedChest;
            case "broken_block":       return logBrokenBlock;
            case "crafting":           return logCrafting;
            case "login":              return logLogin;
            case "logout":             return logLogout;
            case "chat":               return logChat;
            case "item_pickup":        return logItemPickup;
            case "item_drop":          return logItemDrop;
            case "portal_use":         return logPortalUse;
            case "damage":             return logDamage;
            case "explosion":          return logExplosion;
            case "command_use":        return logCommandUse;
            case "block_interaction":  return logBlockInteraction;
            case "entity_spawn":       return logEntitySpawn;
            case "entity_kill":        return logEntityKill;
            case "villager_trade":     return logVillagerTrade;
            case "block_break":        return logBlockBreak;
            case "sign_edit":          return logSignEdit;
            case "fish_caught":        return logFishCaught;
            case "sleep":              return logSleep;
            case "block_place":        return logBlockPlace;
            case "piston_extend":      return logPistonExtend;
            case "piston_retract":     return logPistonRetract;
            case "potion_use":         return logPotionUse;
            case "flower_pick":        return logFlowerPick;
            case "water_bucket":       return logWaterBucket;
            case "lava_bucket":        return logLavaBucket;
            case "crop_harvest":       return logCropHarvest;
            case "trade":              return logTrade;
            case "entered_block":      return logPlayerEnteredBlock;
            default:                   return false;
        }
    }
}

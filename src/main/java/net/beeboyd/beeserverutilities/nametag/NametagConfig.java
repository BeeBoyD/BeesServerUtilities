package net.beeboyd.beeserverutilities.nametag;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Handles the complete configuration for the Nametag feature.
 * Every aspect of the feature is configurable through this JSON file.
 */
public class NametagConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("NametagConfig");
    private static final File CONFIG_FILE = new File("config/beeserverutilities/nametag/nametag_config.json");
    private static final Gson GSON = new Gson();
    private static JsonObject configData;

    /**
     * Loads the configuration from the JSON file.
     */
    public static void loadConfig() {
        try {
            if (CONFIG_FILE.exists()) {
                String json = FileUtils.readFileToString(CONFIG_FILE, StandardCharsets.UTF_8);
                configData = GSON.fromJson(json, JsonObject.class);
            } else {
                // Create a default configuration file if one doesn't exist.
                configData = getDefaultConfig();
                // Ensure directory structure exists
                CONFIG_FILE.getParentFile().mkdirs();
                FileUtils.writeStringToFile(CONFIG_FILE, GSON.toJson(configData), StandardCharsets.UTF_8);
            }
            LOGGER.info("Nametag configuration loaded.");
        } catch (Exception e) {
            LOGGER.error("Error loading nametag configuration: " + e.getMessage());
            configData = getDefaultConfig();
        }
    }

    /**
     * Returns the configuration data.
     *
     * @return The JsonObject configuration.
     */
    public static JsonObject getConfigData() {
        if (configData == null) {
            loadConfig();
        }
        return configData;
    }

    /**
     * Provides a fully featured default configuration.
     * Every configurable aspect of the feature is set here.
     *
     * @return A JsonObject containing the default settings.
     */
    private static JsonObject getDefaultConfig() {
        JsonObject defaultConfig = new JsonObject();
        // General team settings
        defaultConfig.addProperty("defaultTeamColor", "#FFFFFF");
        defaultConfig.addProperty("maxTeamMembers", 10);
        defaultConfig.addProperty("allowDuplicateTeamNames", false);

        // Invitation message settings
        defaultConfig.addProperty("inviteMessage", "You have been invited to join team %s. Click [ACCEPT] or [DECLINE].");

        // Prefix style settings
        JsonObject prefixStyle = new JsonObject();
        prefixStyle.addProperty("useBrackets", true);
        prefixStyle.addProperty("uppercase", true);
        prefixStyle.addProperty("defaultPrefixColor", "#FFFFFF");
        defaultConfig.add("prefixStyle", prefixStyle);

        // Permissions settings
        JsonObject permissions = new JsonObject();
        permissions.addProperty("adminPermissionLevel", 2);
        permissions.addProperty("teamOwnerCanDelete", true);
        defaultConfig.add("permissions", permissions);

        // Logging and Misc settings
        JsonObject logging = new JsonObject();
        logging.addProperty("logTeamCreation", true);
        logging.addProperty("logTeamDeletion", true);
        defaultConfig.add("logging", logging);

        // Additional configuration parameters can be added here to extend functionality.
        return defaultConfig;
    }
}

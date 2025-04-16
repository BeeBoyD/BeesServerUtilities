package net.beeboyd.beeserverutilities.deathmessage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.beeboyd.beeserverutilities.BeeServerUtilities;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DeathMessageConfig {
    private static final File CONFIG_FILE = new File("config/beeserverutilities/deathmessage/deathmessage_config.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static DeathMessageConfig instance = new DeathMessageConfig();

    // Whether death messages are enabled.
    public boolean enabled = true;
    // Customizable death message format. Placeholders: %player%, %coords%, %cause%, %items%
    public String messageFormat = "Player %player% died at %coords% by %cause%. Items: %items%";

    public static DeathMessageConfig get() {
        return instance;
    }

    public static void reload() {
        if (!CONFIG_FILE.exists()) {
            saveDefaults();
        }
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            instance = GSON.fromJson(reader, DeathMessageConfig.class);
            BeeServerUtilities.LOGGER.info("DeathMessageConfig reloaded.");
        } catch (IOException | JsonSyntaxException e) {
            BeeServerUtilities.LOGGER.error("Error reloading DeathMessageConfig: " + e.getMessage());
        }
    }

    public static void saveDefaults() {
        instance = new DeathMessageConfig();
        save();
    }

    public static void save() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(instance, writer);
            }
            BeeServerUtilities.LOGGER.info("DeathMessageConfig saved.");
        } catch (IOException e) {
            BeeServerUtilities.LOGGER.error("Error saving DeathMessageConfig: " + e.getMessage());
        }
    }

    public static void setEnabled(boolean value) {
        instance.enabled = value;
        save();
    }
}

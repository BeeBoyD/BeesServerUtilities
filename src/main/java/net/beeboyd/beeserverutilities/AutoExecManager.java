package net.beeboyd.beeserverutilities;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AutoExecManager {

    // Data container for an autoexec rule.
    public static class AutoExecRule {
        public String name; // Unique identifier for the autoexec rule.
        public AutoExecScheduleType scheduleType;
        public String target;  // Could be a player name or time interval.
        public String command; // The command to execute.

        public AutoExecRule(String name, AutoExecScheduleType scheduleType, String target, String command) {
            this.name = name;
            this.scheduleType = scheduleType;
            this.target = target;
            this.command = command;
        }

        // No-arg constructor needed for Gson.
        public AutoExecRule() {
        }
    }

    private static final List<AutoExecRule> RULES = new ArrayList<>();
    private static final Gson gson = new Gson();
    // Config file path.
    private static final Path CONFIG_PATH = Path.of("config", "beeserverutilities", "autoexec_rules.json");

    public static void addRule(String name, AutoExecScheduleType scheduleType, String target, String command) {
        RULES.add(new AutoExecRule(name, scheduleType, target, command));
        saveRules();
    }

    public static boolean removeRule(String name, AutoExecScheduleType scheduleType, String target, String command) {
        boolean removed = RULES.removeIf(rule ->
                rule.name.equalsIgnoreCase(name) &&
                rule.scheduleType == scheduleType &&
                rule.target.equals(target) &&
                rule.command.equals(command)
        );
        if (removed) {
            saveRules();
        }
        return removed;
    }

    public static List<AutoExecRule> getRules() {
        return RULES;
    }

    public static void loadRules() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                Files.createDirectories(CONFIG_PATH.getParent());
                saveRules(); // Saves empty list.
                return;
            }
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                Type listType = new TypeToken<List<AutoExecRule>>() {}.getType();
                List<AutoExecRule> loadedRules = gson.fromJson(reader, listType);
                if (loadedRules != null) {
                    RULES.clear();
                    RULES.addAll(loadedRules);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveRules() {
        try {
            if (!Files.exists(CONFIG_PATH.getParent())) {
                Files.createDirectories(CONFIG_PATH.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                gson.toJson(RULES, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

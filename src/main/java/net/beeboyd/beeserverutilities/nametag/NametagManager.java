package net.beeboyd.beeserverutilities.nametag;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages team data (nametags) for players including creation, deletion,
 * invitations, prefix settings, and admin modifications.
 */
public class NametagManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("NametagManager");
    private static final Gson GSON = new Gson();
    // Path for the JSON file that stores team data
    private static final File DATA_FILE = new File("config/beeserverutilities/nametag/nametag_data.json");
    // In-memory storage for team data. Key: team name, Value: Team instance.
    private static Map<String, Team> teams = new HashMap<>();

    static {
        // Load existing team data during class initialization.
        loadData();
    }

    /**
     * Loads team data from the JSON file.
     */
    public static void loadData() {
        try {
            if (DATA_FILE.exists()) {
                String json = FileUtils.readFileToString(DATA_FILE, StandardCharsets.UTF_8);
                Type type = new TypeToken<Map<String, Team>>() {}.getType();
                teams = GSON.fromJson(json, type);
                if (teams == null) {
                    teams = new HashMap<>();
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error loading team data: " + e.getMessage());
            teams = new HashMap<>();
        }
    }

    /**
     * Saves the current team data to the JSON file.
     */
    public static void saveData() {
        try {
            String json = GSON.toJson(teams);
            FileUtils.writeStringToFile(DATA_FILE, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Error saving team data: " + e.getMessage());
        }
    }

    /**
     * Creates a new team with the specified name and color.
     *
     * @param source   The command source.
     * @param teamName The desired team name.
     * @param color    The team's color code or hex color (with #).
     * @throws Exception if the team already exists.
     */
    public static void createTeam(CommandSourceStack source, String teamName, String color) throws Exception {
        ServerPlayer player = source.getPlayerOrException();
        if (teams.containsKey(teamName)) {
            throw new Exception("Team already exists.");
        }
        // Create a new team; the creator is the owner and initial member.
        Team team = new Team(teamName, color, "", player.getName().getString());
        team.addMember(player.getName().getString());
        teams.put(teamName, team);
        saveData();
        LOGGER.info("Team created: " + teamName + " by " + player.getName().getString());
    }

    /**
     * Deletes an existing team by its name.
     *
     * @param source   The command source.
     * @param teamName The name of the team to delete.
     * @throws Exception if the team does not exist or the player lacks permissions.
     */
    public static void deleteTeam(CommandSourceStack source, String teamName) throws Exception {
        ServerPlayer player = source.getPlayerOrException();
        Team team = teams.get(teamName);
        if (team == null) {
            throw new Exception("Team does not exist.");
        }
        // Only allow deletion if the player is an admin or the team owner.
        if (!player.hasPermissions(2) && !team.getOwner().equals(player.getName().getString())) {
            throw new Exception("You don't have permission to delete this team.");
        }
        teams.remove(teamName);
        saveData();
        LOGGER.info("Team deleted: " + teamName + " by " + player.getName().getString());
    }

    /**
     * Sends an invitation for the caller's team to another player.
     * (The invitation mechanics such as clickable buttons should be implemented as needed.)
     *
     * @param source         The command source.
     * @param targetUsername The username to invite.
     * @throws Exception if the caller is not in any team.
     */
    public static void invitePlayer(CommandSourceStack source, String targetUsername) throws Exception {
        ServerPlayer player = source.getPlayerOrException();
        String teamName = getPlayerTeam(player);
        if (teamName == null || teamName.isEmpty()) {
            throw new Exception("You are not in a team.");
        }
        // Here you would add logic for sending an interactive invite (e.g., with a clickable chat button).
        LOGGER.info("Player " + player.getName().getString() + " invited " + targetUsername + " to team " + teamName);
    }

    /**
     * Removes a specified player from the caller’s team.
     *
     * @param source         The command source.
     * @param targetUsername The player to remove.
     * @throws Exception if the team or player is not valid or insufficient permissions.
     */
    public static void kickPlayer(CommandSourceStack source, String targetUsername) throws Exception {
        ServerPlayer player = source.getPlayerOrException();
        String teamName = getPlayerTeam(player);
        if (teamName == null || teamName.isEmpty()) {
            throw new Exception("You are not in a team.");
        }
        Team team = teams.get(teamName);
        if (team == null) {
            throw new Exception("Team does not exist.");
        }
        // Only the team owner or an admin can kick players.
        if (!player.hasPermissions(2) && !team.getOwner().equals(player.getName().getString())) {
            throw new Exception("You don't have permission to kick players.");
        }
        if (!team.getMembers().contains(targetUsername)) {
            throw new Exception("Player is not in your team.");
        }
        team.removeMember(targetUsername);
        saveData();
        LOGGER.info("Player " + targetUsername + " kicked from team " + teamName + " by " + player.getName().getString());
    }

    /**
     * Sets the team prefix which will appear in chat and player lists.
     *
     * @param source The command source.
     * @param prefix The prefix string.
     * @param color  The prefix color.
     * @throws Exception if the caller is not in any team or lacks permission.
     */
    public static void setTeamPrefix(CommandSourceStack source, String prefix, String color) throws Exception {
        ServerPlayer player = source.getPlayerOrException();
        String teamName = getPlayerTeam(player);
        if (teamName == null || teamName.isEmpty()) {
            throw new Exception("You are not in a team.");
        }
        Team team = teams.get(teamName);
        if (team == null) {
            throw new Exception("Team does not exist.");
        }
        if (!player.hasPermissions(2) && !team.getOwner().equals(player.getName().getString())) {
            throw new Exception("You don't have permission to set the team prefix.");
        }
        team.setPrefix(prefix);
        team.setPrefixColor(color);
        saveData();
        LOGGER.info("Team " + teamName + " prefix set to " + prefix + " with color " + color + " by " + player.getName().getString());
    }

    /**
     * Sets the calling player's personal prefix.
     *
     * @param source The command source.
     * @param prefix The prefix string.
     * @param color  The prefix color.
     * @throws Exception when there are errors (e.g., problems storing the prefix).
     */
    public static void setOwnPrefix(CommandSourceStack source, String prefix, String color) throws Exception {
        ServerPlayer player = source.getPlayerOrException();
        // Implement how your mod should store the player prefix in a persistent way.
        LOGGER.info("Player " + player.getName().getString() + " set their own prefix to " + prefix + " with color " + color);
        // (Store the prefix in a config or player data file as required.)
    }

    /**
     * Admin command: sets the team membership for a target player.
     *
     * @param source         The command source.
     * @param targetUsername The target player's username.
     * @param teamName       The team name to assign.
     * @throws Exception if the caller is not admin or the team does not exist.
     */
    public static void adminSetTeam(CommandSourceStack source, String targetUsername, String teamName) throws Exception {
        ServerPlayer admin = source.getPlayerOrException();
        if (!admin.hasPermissions(2)) {
            throw new Exception("You don't have admin privileges.");
        }
        Team team = teams.get(teamName);
        if (team == null) {
            throw new Exception("Team does not exist.");
        }
        // For this example, simply add the target player to the team.
        team.addMember(targetUsername);
        saveData();
        LOGGER.info("Admin " + admin.getName().getString() + " set " + targetUsername + " to team " + teamName);
    }

    /**
     * Admin command: sets a target player's prefix.
     *
     * @param source         The command source.
     * @param targetUsername The target player's username.
     * @param prefix         The prefix string.
     * @param color          The prefix color.
     * @throws Exception if the caller is not admin.
     */
    public static void adminSetPlayerPrefix(CommandSourceStack source, String targetUsername, String prefix, String color) throws Exception {
        ServerPlayer admin = source.getPlayerOrException();
        if (!admin.hasPermissions(2)) {
            throw new Exception("You don't have admin privileges.");
        }
        // Implement storing the target player's prefix in your system as needed.
        LOGGER.info("Admin " + admin.getName().getString() + " set " + targetUsername + "'s prefix to " + prefix + " with color " + color);
    }

    /**
     * Utility method to determine the team of a given player.
     *
     * @param player The server player.
     * @return The team name if the player is in one; otherwise an empty string.
     */
    public static String getPlayerTeam(ServerPlayer player) {
        for (Team team : teams.values()) {
            if (team.getMembers().contains(player.getName().getString())) {
                return team.getTeamName();
            }
        }
        return "";
    }
}

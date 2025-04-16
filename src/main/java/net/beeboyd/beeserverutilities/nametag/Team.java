package net.beeboyd.beeserverutilities.nametag;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a team (or nametag group) that players can join.
 */
public class Team {
    private String teamName;
    private String teamColor;
    private String prefix;
    private String prefixColor;
    private String owner;
    private List<String> members;
    private List<String> invites;

    /**
     * Constructs a new Team.
     *
     * @param teamName The name of the team.
     * @param teamColor The team's color code or hex value.
     * @param prefix The team prefix.
     * @param owner The username of the player who created the team.
     */
    public Team(String teamName, String teamColor, String prefix, String owner) {
        this.teamName = teamName;
        this.teamColor = teamColor;
        this.prefix = prefix;
        this.prefixColor = "";
        this.owner = owner;
        this.members = new ArrayList<>();
        this.invites = new ArrayList<>();
    }

    public String getTeamName() {
        return teamName;
    }

    public String getTeamColor() {
        return teamColor;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getPrefixColor() {
        return prefixColor;
    }

    public String getOwner() {
        return owner;
    }

    public List<String> getMembers() {
        return members;
    }

    public List<String> getInvites() {
        return invites;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setPrefixColor(String prefixColor) {
        this.prefixColor = prefixColor;
    }

    /**
     * Adds a player to this team's member list if not already present.
     *
     * @param playerName The username to add.
     */
    public void addMember(String playerName) {
        if (!members.contains(playerName)) {
            members.add(playerName);
        }
    }

    /**
     * Removes a player from this team.
     *
     * @param playerName The username to remove.
     */
    public void removeMember(String playerName) {
        members.remove(playerName);
    }

    /**
     * Adds a pending invitation for a player.
     *
     * @param playerName The username to invite.
     */
    public void addInvite(String playerName) {
        if (!invites.contains(playerName)) {
            invites.add(playerName);
        }
    }

    /**
     * Removes a pending invitation.
     *
     * @param playerName The username to remove from invites.
     */
    public void removeInvite(String playerName) {
        invites.remove(playerName);
    }
}

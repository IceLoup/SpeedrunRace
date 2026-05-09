package xyz.pyxismc.speedrunrace.core;

import xyz.pyxismc.speedrunrace.models.Team;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class TeamManager {
    public static final int MAX_TEAM_SIZE = 3;

    private final Map<String, Team> teams = new LinkedHashMap<>();
    private final Map<UUID, Team> playerTeamMap = new LinkedHashMap<>();

    public Team getOrCreateTeam(String id) {
        return teams.computeIfAbsent(normalizeTeamId(id), Team::new);
    }

    public Team addPlayerToTeam(String teamId, Player player) {
        removePlayerFromCurrentTeam(player);
        Team team = getOrCreateTeam(teamId);
        if (!team.getPlayers().contains(player.getUniqueId())) {
            team.getPlayers().add(player.getUniqueId());
        }
        playerTeamMap.put(player.getUniqueId(), team);
        return team;
    }

    public void removePlayerFromCurrentTeam(Player player) {
        Team oldTeam = playerTeamMap.remove(player.getUniqueId());
        if (oldTeam != null) {
            oldTeam.getPlayers().remove(player.getUniqueId());
            if (oldTeam.getPlayers().isEmpty()) {
                teams.remove(oldTeam.getId());
            }
        }
    }

    public boolean canJoin(Team team, Player player) {
        return team.getPlayers().contains(player.getUniqueId()) || team.getPlayers().size() < MAX_TEAM_SIZE;
    }

    public Collection<Team> getTeams() { return teams.values(); }
    public Team getByPlayer(Player p) { return playerTeamMap.get(p.getUniqueId()); }
    public Team getByWorldName(String name) {
        return teams.values().stream()
                .filter(t -> name.equals(t.getWorldName(World.Environment.NORMAL))
                        || name.equals(t.getWorldName(World.Environment.NETHER))
                        || name.equals(t.getWorldName(World.Environment.THE_END)))
                .findFirst()
                .orElse(null);
    }

    public String normalizeTeamId(String id) {
        return id.trim().toLowerCase();
    }
}

package xyz.pyxismc.speedrunrace.core;

import xyz.pyxismc.speedrunrace.models.Team;
import org.bukkit.entity.Player;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeamManager {
    private final Map<String, Team> teams = new HashMap<>();
    private final Map<UUID, Team> playerTeamMap = new HashMap<>();

    public Team getOrCreateTeam(String id) {
        return teams.computeIfAbsent(id.toLowerCase(), Team::new);
    }

    public void addPlayerToTeam(String teamId, Player player) {
        removePlayerFromCurrentTeam(player);
        Team team = getOrCreateTeam(teamId);
        team.getPlayers().add(player.getUniqueId());
        playerTeamMap.put(player.getUniqueId(), team);
    }

    public void removePlayerFromCurrentTeam(Player player) {
        Team oldTeam = playerTeamMap.remove(player.getUniqueId());
        if (oldTeam != null) oldTeam.getPlayers().remove(player.getUniqueId());
    }

    public Collection<Team> getTeams() { return teams.values(); }
    public Team getByPlayer(Player p) { return playerTeamMap.get(p.getUniqueId()); }
    public Team getByWorldName(String name) {
        return teams.values().stream().filter(t -> name.startsWith("race_" + t.getId())).findFirst().orElse(null);
    }
}

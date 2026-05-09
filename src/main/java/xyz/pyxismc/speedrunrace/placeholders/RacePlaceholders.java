package xyz.pyxismc.speedrunrace.placeholders;

import io.github.miniplaceholders.api.Expansion;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.bukkit.entity.Player;
import xyz.pyxismc.speedrunrace.SpeedrunRace;
import xyz.pyxismc.speedrunrace.models.Team;

import java.util.Comparator;
import java.util.List;

public class RacePlaceholders {
    private static final int MAX_RANK_PLACEHOLDERS = 100;
    private static final String PRIMARY_COLOR = "<#6b109e>";
    private static final String SECONDARY_COLOR = "<#c13cff>";
    private static final String DEFAULT_RANK_COLOR = "<gray>";
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final SpeedrunRace plugin;
    private Expansion expansion;

    public RacePlaceholders(SpeedrunRace plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Expansion.Builder builder = Expansion.builder("speedrunrace")
                .audiencePlaceholder(Player.class, "team_performance", (player, ctx, queue) ->
                        inserting(formatPlayerTeamPerformance(player)));

        for (int rank = 1; rank <= MAX_RANK_PLACEHOLDERS; rank++) {
            int capturedRank = rank;
            builder.globalPlaceholder(rank + "_team", (ctx, queue) ->
                    inserting(formatRankedTeamPerformance(capturedRank)));
        }

        this.expansion = builder.build();
        this.expansion.register();
    }

    public void unregister() {
        if (expansion != null) {
            expansion.unregister();
            expansion = null;
        }
    }

    private String formatRankedTeamPerformance(int rank) {
        List<Team> finishedTeams = plugin.getTeamManager().getTeams().stream()
                .filter(Team::isFinished)
                .sorted(Comparator.comparingLong(Team::getFinishDurationMillis))
                .toList();

        int index = rank - 1;
        if (index < 0 || index >= finishedTeams.size()) {
            return "";
        }

        Team team = finishedTeams.get(index);
        return getRankColor(rank) + rank + ". " + formatTeamPerformance(team, team.getFinishDurationMillis());
    }

    private String formatPlayerTeamPerformance(Player player) {
        Team team = plugin.getTeamManager().getByPlayer(player);
        if (team == null) {
            return "";
        }

        long duration = team.isFinished()
                ? team.getFinishDurationMillis()
                : getCurrentDuration(team);
        return formatTeamPerformance(team, duration);
    }

    private long getCurrentDuration(Team team) {
        if (team.getStartTime() <= 0) {
            return 0;
        }
        return Math.max(0, System.currentTimeMillis() - team.getStartTime());
    }

    private String formatTeamPerformance(Team team, long duration) {
        return PRIMARY_COLOR + team.getId().toUpperCase() + " <gray>- " + SECONDARY_COLOR + formatPlaceholderDuration(duration);
    }

    private String getRankColor(int rank) {
        switch (rank) {
            case 1:
            case 2:
            case 3:
                return PRIMARY_COLOR;
            default:
                return DEFAULT_RANK_COLOR;
        }
    }

    private String formatPlaceholderDuration(long duration) {
        return plugin.formatDuration(duration).replace(':', '.');
    }

    private static Tag inserting(String value) {
        return Tag.selfClosingInserting(MM.deserialize(value));
    }
}

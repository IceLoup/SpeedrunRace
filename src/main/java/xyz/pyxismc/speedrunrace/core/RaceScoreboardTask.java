package xyz.pyxismc.speedrunrace.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import xyz.pyxismc.speedrunrace.SpeedrunRace;
import xyz.pyxismc.speedrunrace.models.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class RaceScoreboardTask extends BukkitRunnable {
    private final SpeedrunRace plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public RaceScoreboardTask(SpeedrunRace p) { this.plugin = p; }

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            updatePlayer(p);
        }
    }

    public void updatePlayer(Player p) {
        Team t = plugin.getTeamManager().getByPlayer(p);

        Component title = MM.deserialize("<gradient:#6b109e:#c13cff><bold>Speedrun Race");

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("race", "dummy", title);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        if (t == null) {
            setLine(obj, "l6", 6, MM.deserialize("<gradient:#6b109e:#c13cff> "));
            setLine(obj, "l5", 5, MM.deserialize("<color:#6b109e>Team: <color:#c13cff>N/A"));
            setLine(obj, "l4", 4, MM.deserialize("<color:#6b109e>Online: <color:#c13cff>" + Bukkit.getOnlinePlayers().size()));
            setLine(obj, "l3", 3, MM.deserialize("<gradient:#6b109e:#c13cff> "));
            setLine(obj, "l2", 2, MM.deserialize("<color:#6b109e>Join your team: <color:#c13cff>/join [TAG]"));
            setLine(obj, "l1", 1, MM.deserialize("<gradient:#6b109e:#c13cff> "));
            setLine(obj, "l0", 0, MM.deserialize("<gradient:#6b109e:#c13cff>event.franceranked.fr"));
            p.setScoreboard(board);
            return;
        }

        String timeStr;
        if (t.getStartTime() > 0) {
            long s = (System.currentTimeMillis() - t.getStartTime()) / 1000;
            timeStr = String.format("<color:#c13cff>%02dh%02dm%02ds", s / 3600, (s % 3600) / 60, s % 60);
        } else if (plugin.isRaceStarting()) {
            timeStr = "<color:#c13cff>Start in " + plugin.getCountdownRemainingSeconds() + "s";
        } else {
            timeStr = "<color:#6b109e><italic>Waiting...";
        }

        setLine(obj, "l6", 6, MM.deserialize("<gradient:#6b109e:#c13cff> "));
        setLine(obj, "l5", 5, MM.deserialize("<color:#6b109e>Team: <color:#c13cff>" + t.getId().toUpperCase()));
        setLine(obj, "l4", 4, MM.deserialize("<color:#6b109e>Members: <color:#c13cff>" + t.getPlayers().size() + "/" + TeamManager.MAX_TEAM_SIZE));
        setLine(obj, "l3", 3, MM.deserialize("<gradient:#6b109e:#c13cff> "));
        setLine(obj, "l2", 2, MM.deserialize("<color:#6b109e>Time: " + timeStr));
        setLine(obj, "l1", 1, MM.deserialize("<gradient:#6b109e:#c13cff> "));
        setLine(obj, "l0", 0, MM.deserialize("<gradient:#6b109e:#c13cff>event.franceranked.fr"));

        p.setScoreboard(board);
    }

    private void setLine(Objective obj, String key, int score, Component display) {
        Score s = obj.getScore(key);
        s.customName(display);
        s.setScore(score);
    }
}

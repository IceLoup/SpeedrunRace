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
            Team t = plugin.getTeamManager().getByPlayer(p);
            if (t == null) continue;

            Component title = MM.deserialize("<gradient:#4A6FA5:#7AA3D4><bold>Speedrun Race");

            Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
            Objective obj = board.registerNewObjective("race", "dummy", title);
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);

            String timeStr;
            if (t.getStartTime() > 0) {
                long s = (System.currentTimeMillis() - t.getStartTime()) / 1000;
                timeStr = String.format("<color:#4A6FA5>%02d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60);
            } else {
                timeStr = "<color:#E8DCC8><italic>Waiting...";
            }

            setLine(obj, "l6", 6, Component.empty());
            setLine(obj, "l5", 5, MM.deserialize("<color:#E8DCC8>Team: <color:#4A6FA5>" + t.getId().toUpperCase()));
            setLine(obj, "l4", 4, MM.deserialize("<color:#E8DCC8>Members: <color:#7AA3D4>" + t.getPlayers().size() + "/3"));
            setLine(obj, "l3", 3, Component.empty());
            setLine(obj, "l2", 2, MM.deserialize("<color:#E8DCC8>Time: " + timeStr));
            setLine(obj, "l1", 1, Component.empty());
            setLine(obj, "l0", 0, MM.deserialize("<color:#4A6FA5>pyxismc.xyz"));

            p.setScoreboard(board);
        }
    }

    private void setLine(Objective obj, String key, int score, Component display) {
        Score s = obj.getScore(key);
        s.customName(display);
        s.setScore(score);
    }
}

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

            Component Title = MM.deserialize("<gradient:yellow:gold>Speedrun Race");

            Scoreboard b = Bukkit.getScoreboardManager().getNewScoreboard();
            Objective obj = b.registerNewObjective("race", "dummy", "" + Title);
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);

            net.kyori.adventure.text.Component Wait = MM.deserialize("<white><italic>Waiting...");

            String timeStr = "" + Wait;
            if (t.getStartTime() > 0) {
                long s = (System.currentTimeMillis() - t.getStartTime()) / 1000;
                timeStr = String.format("§a%02d:%02d:%02d", s/3600, (s%3600)/60, s%60);
            }

            obj.getScore("§1").setScore(6);
            obj.getScore("§fTeam: §b" + t.getId().toUpperCase()).setScore(5);
            obj.getScore("§fMembers: §e" + t.getPlayers().size() + "/3").setScore(4);
            obj.getScore("§2").setScore(3);
            obj.getScore("§fTime: " + timeStr).setScore(2);
            obj.getScore("§3").setScore(1);
            obj.getScore("pyxismc.xyz").setScore(0);
            p.setScoreboard(b);
        }
    }
}

package xyz.pyxismc.speedrunrace.core;

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
    public RaceScoreboardTask(SpeedrunRace p) { this.plugin = p; }

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Team t = plugin.getTeamManager().getByPlayer(p);
            if (t == null) continue;

            Scoreboard b = Bukkit.getScoreboardManager().getNewScoreboard();
            Objective obj = b.registerNewObjective("race", "dummy", "§6§lSPEEDRUN RACE");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);

            String timeStr = "§7En attente...";
            if (t.getStartTime() > 0) {
                long s = (System.currentTimeMillis() - t.getStartTime()) / 1000;
                timeStr = String.format("§a%02d:%02d:%02d", s/3600, (s%3600)/60, s%60);
            }

            obj.getScore("§1").setScore(6);
            obj.getScore("§fÉquipe: §b" + t.getId().toUpperCase()).setScore(5);
            obj.getScore("§fMembres: §e" + t.getPlayers().size() + "/3").setScore(4);
            obj.getScore("§2").setScore(3);
            obj.getScore("§fTemps: " + timeStr).setScore(2);
            obj.getScore("§3").setScore(1);
            obj.getScore("§etonserveur.com").setScore(0);
            p.setScoreboard(b);
        }
    }
}

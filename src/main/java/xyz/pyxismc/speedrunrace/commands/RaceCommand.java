package xyz.pyxismc.speedrunrace.commands;

import xyz.pyxismc.speedrunrace.SpeedrunRace;
import xyz.pyxismc.speedrunrace.models.Team;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RaceCommand implements CommandExecutor {
    private final SpeedrunRace plugin;
    public RaceCommand(SpeedrunRace p) { this.plugin = p; }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] args) {
        if (args.length == 0) return false;

        if (args[0].equalsIgnoreCase("pregen")) {
            s.sendMessage("§e[Race] §aLancement Chunky (Rayon 1000)...");
            String[] tps = {"template_overworld", "template_nether", "template_end"};
            for (String t : tps) {
                Bukkit.createWorld(new WorldCreator(t));
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky world " + t);
                int rad = t.contains("nether") ? 125 : 1000;
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky radius " + rad);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky start");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("start")) {
            Bukkit.broadcastMessage("§6§lDÉMARRAGE DE LA COURSE !");
            for (Team team : plugin.getTeamManager().getTeams()) {
                plugin.getWorldManager().prepareTeamWorlds(team, () -> {
                    World w = Bukkit.getWorld(team.getWorldName(World.Environment.NORMAL));
                    team.setStartTime(System.currentTimeMillis());
                    for (java.util.UUID id : team.getPlayers()) {
                        Player p = Bukkit.getPlayer(id);
                        if (p != null) {
                            p.teleport(w.getSpawnLocation());
                            p.getInventory().clear();
                            p.setGameMode(GameMode.SURVIVAL);
                        }
                    }
                });
            }
            return true;
        }
        return true;
    }
}

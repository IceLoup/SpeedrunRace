package xyz.pyxismc.speedrunrace.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import xyz.pyxismc.speedrunrace.SpeedrunRace;
import xyz.pyxismc.speedrunrace.models.Team;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RaceCommand implements CommandExecutor {
    private final SpeedrunRace plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public RaceCommand(SpeedrunRace p) { this.plugin = p; }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] args) {
        if (args.length == 0) return false;

        if (args[0].equalsIgnoreCase("pregen")) {
            s.sendMessage(MM.deserialize("<#4A6FA5>[Race] <green>Lancement Chunky (Rayon 1000)..."));
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
            Bukkit.broadcast(MM.deserialize("<gradient:#4A6FA5:#E8DCC8><bold>THE RACE HAS STARTED!"));
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

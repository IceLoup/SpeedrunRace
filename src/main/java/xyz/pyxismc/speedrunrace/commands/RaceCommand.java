package xyz.pyxismc.speedrunrace.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import xyz.pyxismc.speedrunrace.SpeedrunRace;
import xyz.pyxismc.speedrunrace.models.Team;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.pyxismc.speedrunrace.core.TeamManager;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class RaceCommand implements CommandExecutor {
    private final SpeedrunRace plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public RaceCommand(SpeedrunRace p) { this.plugin = p; }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] args) {
        if (l.equalsIgnoreCase("forcestart") || l.equalsIgnoreCase("force-start")) {
            startRace(s, true);
            return true;
        }

        if (args.length == 0) {
            s.sendMessage(MM.deserialize("<#c13cff>Usage: <#6b109e>/race pregen|duplicate|start|forcestart"));
            return true;
        }

        if (args[0].equalsIgnoreCase("pregen")) {
            s.sendMessage(MM.deserialize("<#c13cff>[Race] <#6b109e>Starting Chunky pre-generation (radius 1000)..."));
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

        if (args[0].equalsIgnoreCase("duplicate")) {
            duplicateWorlds(s);
            return true;
        }

        if (args[0].equalsIgnoreCase("start")) {
            startRace(s, false);
            return true;
        }

        if (args[0].equalsIgnoreCase("forcestart") || args[0].equalsIgnoreCase("force-start")) {
            startRace(s, true);
            return true;
        }
        return true;
    }

    private void duplicateWorlds(CommandSender sender) {
        Collection<Team> teams = plugin.getTeamManager().getTeams();
        if (teams.isEmpty()) {
            sender.sendMessage(MM.deserialize("<#c13cff>[Race] <#6b109e>No teams to duplicate."));
            return;
        }

        AtomicInteger remaining = new AtomicInteger(teams.size());
        sender.sendMessage(MM.deserialize("<#c13cff>[Race] <#6b109e>Duplicating worlds for " + teams.size() + " team(s)..."));
        for (Team team : teams) {
            plugin.getWorldManager().prepareTeamWorlds(team, () -> {
                int left = remaining.decrementAndGet();
                sender.sendMessage(MM.deserialize("<#c13cff>[Race] <#c13cff>" + team.getId().toUpperCase() + " ready <gray>(" + left + " remaining)"));
                if (left == 0) {
                    Bukkit.broadcast(MM.deserialize("<gradient:#6b109e:#c13cff><bold>All race worlds are ready."));
                }
            });
        }
    }

    private void startRace(CommandSender sender, boolean force) {
        Collection<Team> teams = plugin.getTeamManager().getTeams();
        if (!force && teams.isEmpty()) {
            sender.sendMessage(MM.deserialize("<#c13cff>[Race] <#6b109e>No teams registered."));
            return;
        }
        if (!force && (plugin.isRaceStarting() || plugin.isRaceStarted())) {
            sender.sendMessage(MM.deserialize("<#c13cff>[Race] <#6b109e>The race is already starting or running."));
            return;
        }

        if (!force) {
            for (Team team : teams) {
                String validationError = getTeamValidationError(team);
                if (validationError != null) {
                    sender.sendMessage(MM.deserialize(validationError));
                    return;
                }
                if (!plugin.getWorldManager().areTeamWorldsReady(team)) {
                    sender.sendMessage(MM.deserialize("<#c13cff>[Race] <#6b109e>Worlds are not ready for <#c13cff>" + team.getId().toUpperCase() + "<#6b109e>. Use <#c13cff>/race duplicate<#6b109e> first."));
                    return;
                }
            }
        }

        for (Team team : teams) {
            World w = Bukkit.getWorld(team.getWorldName(World.Environment.NORMAL));
            if (w == null) {
                if (!force) {
                    sender.sendMessage(MM.deserialize("<#c13cff>[Race] <#6b109e>Missing overworld for <#c13cff>" + team.getId().toUpperCase() + "<#6b109e>."));
                    return;
                }
                w = new WorldCreator(team.getWorldName(World.Environment.NORMAL)).environment(World.Environment.NORMAL).createWorld();
            }
            if (force && Bukkit.getWorld(team.getWorldName(World.Environment.NETHER)) == null) {
                new WorldCreator(team.getWorldName(World.Environment.NETHER)).environment(World.Environment.NETHER).createWorld();
            }
            if (force && Bukkit.getWorld(team.getWorldName(World.Environment.THE_END)) == null) {
                new WorldCreator(team.getWorldName(World.Environment.THE_END)).environment(World.Environment.THE_END).createWorld();
            }
            team.resetRaceProgress();
            for (UUID id : team.getPlayers()) {
                Player p = Bukkit.getPlayer(id);
                if (p == null || !p.isOnline()) {
                    if (!force) {
                        sender.sendMessage(MM.deserialize("<#c13cff>[Race] <#6b109e>A player from <#c13cff>" + team.getId().toUpperCase() + " <#6b109e>disconnected before the start."));
                        return;
                    }
                    continue;
                }
                p.teleport(w.getSpawnLocation());
                p.getInventory().clear();
                p.setGameMode(GameMode.SURVIVAL);
            }
        }

        if (force) {
            long now = System.currentTimeMillis();
            for (Team team : plugin.getTeamManager().getTeams()) {
                team.setStartTime(now);
            }
            plugin.markRaceStarted();
            Bukkit.broadcast(MM.deserialize("<gradient:#6b109e:#c13cff><bold>THE RACE HAS BEEN FORCE-STARTED!"));
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendActionBar(MM.deserialize("<#c13cff><bold>GO!"));
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }
            return;
        }

        int startSequence = plugin.beginCountdown(10);
        Bukkit.broadcast(MM.deserialize("<gradient:#6b109e:#c13cff><bold>The race starts in 10 seconds."));

        new BukkitRunnable() {
            private int secondsLeft = 10;

            @Override
            public void run() {
                if (!plugin.isRaceStartSequence(startSequence) || !plugin.isRaceStarting()) {
                    cancel();
                    return;
                }

                if (secondsLeft > 0) {
                    Bukkit.broadcast(MM.deserialize("<#c13cff>[Race] <#6b109e>Start in <#c13cff><bold>" + secondsLeft + "</bold><#6b109e>s"));
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendActionBar(MM.deserialize("<#6b109e>Start in <#c13cff><bold>" + secondsLeft + "</bold><#6b109e>s"));
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.2f);
                    }
                    secondsLeft--;
                    return;
                }

                long now = System.currentTimeMillis();
                for (Team team : plugin.getTeamManager().getTeams()) {
                    team.setStartTime(now);
                }
                plugin.markRaceStarted();
                Bukkit.broadcast(MM.deserialize("<gradient:#6b109e:#c13cff><bold>THE RACE HAS STARTED!"));
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendActionBar(MM.deserialize("<#c13cff><bold>GO!"));
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                }
                cancel();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private String getTeamValidationError(Team team) {
        if (team.getPlayers().size() != TeamManager.MAX_TEAM_SIZE) {
            return "<#c13cff>[Race] <#6b109e>Team <#c13cff>" + team.getId().toUpperCase()
                    + " <#6b109e>is not complete <gray>(" + team.getPlayers().size() + "/" + TeamManager.MAX_TEAM_SIZE + ").";
        }

        for (UUID id : team.getPlayers()) {
            Player player = Bukkit.getPlayer(id);
            if (player == null || !player.isOnline()) {
                String name = Bukkit.getOfflinePlayer(id).getName();
                if (name == null) name = id.toString();
                return "<#c13cff>[Race] <#6b109e>Team <#c13cff>" + team.getId().toUpperCase()
                        + " <#6b109e>is not complete: <#c13cff>" + name + " <#6b109e>is offline.";
            }
        }

        return null;
    }
}

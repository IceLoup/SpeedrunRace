package xyz.pyxismc.speedrunrace;


import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import xyz.pyxismc.speedrunrace.commands.JoinCommand;
import xyz.pyxismc.speedrunrace.commands.RaceCommand;
import xyz.pyxismc.speedrunrace.core.RaceScoreboardTask;
import xyz.pyxismc.speedrunrace.core.TeamManager;
import xyz.pyxismc.speedrunrace.core.WorldManager;
import xyz.pyxismc.speedrunrace.listeners.GameListener;
import xyz.pyxismc.speedrunrace.listeners.PortalListener;
import xyz.pyxismc.speedrunrace.models.Team;
import xyz.pyxismc.speedrunrace.placeholders.RacePlaceholders;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class SpeedrunRace extends JavaPlugin {
    private static final String BASE_WORLD_NAME = "world";

    private TeamManager teamManager;
    private WorldManager worldManager;
    private long countdownEndTime = 0L;
    private int raceStartSequence = 0;
    private boolean raceStarting = false;
    private boolean raceStarted = false;
    private RacePlaceholders racePlaceholders;

    @Override
    public void onEnable() {

        cleanOldWorlds();

        this.teamManager = new TeamManager();
        this.worldManager = new WorldManager(this);


        RaceCommand raceCommand = new RaceCommand(this);
        getCommand("race").setExecutor(raceCommand);
        getCommand("forcestart").setExecutor(raceCommand);
        getCommand("join").setExecutor(new JoinCommand(this));


        getServer().getPluginManager().registerEvents(new PortalListener(this), this);
        getServer().getPluginManager().registerEvents(new GameListener(this), this);

        if (Bukkit.getPluginManager().isPluginEnabled("MiniPlaceholders")) {
            this.racePlaceholders = new RacePlaceholders(this);
            this.racePlaceholders.register();
            getLogger().info("SpeedrunRace MiniPlaceholders placeholders registered.");
        }


        new RaceScoreboardTask(this).runTaskTimer(this, 0L, 20L);

        getLogger().info("SpeedrunRace is running with LuckPerms.");
    }

    public TeamManager getTeamManager() { return teamManager; }
    public WorldManager getWorldManager() { return worldManager; }
    public boolean isRaceStarting() { return raceStarting; }
    public boolean isRaceStarted() { return raceStarted; }

    @Override
    public void onDisable() {
        if (racePlaceholders != null) {
            racePlaceholders.unregister();
        }

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            for (Player target : Bukkit.getOnlinePlayers()) {
                viewer.showPlayer(this, target);
            }
        }
    }

    public int beginCountdown(int seconds) {
        this.raceStartSequence++;
        this.countdownEndTime = System.currentTimeMillis() + seconds * 1000L;
        this.raceStarting = true;
        this.raceStarted = false;
        return raceStartSequence;
    }

    public void markRaceStarted() {
        this.raceStartSequence++;
        this.countdownEndTime = 0L;
        this.raceStarting = false;
        this.raceStarted = true;
        refreshPlayerVisibility();
    }

    public boolean isRaceStartSequence(int sequence) {
        return raceStartSequence == sequence;
    }

    public void finishTeam(Team team) {
        if (team.isFinished()) return;

        long duration = Math.max(0, System.currentTimeMillis() - team.getStartTime());
        team.setFinished(true);
        team.setFinishDurationMillis(duration);
        moveFinishedTeamPlayersToBaseWorld(team);
        saveFinishedTeamTimes();
        refreshPlayerVisibility();
    }

    public void applyFinishedTeamGameMode(Player player) {
        Team team = teamManager.getByPlayer(player);
        if (team != null && team.isFinished()) {
            moveFinishedPlayerToBaseWorld(player);
        }
    }

    private void moveFinishedTeamPlayersToBaseWorld(Team team) {
        for (UUID id : team.getPlayers()) {
            Player player = Bukkit.getPlayer(id);
            if (player != null && player.isOnline()) {
                moveFinishedPlayerToBaseWorld(player);
            }
        }
    }

    private void moveFinishedPlayerToBaseWorld(Player player) {
        player.setGameMode(GameMode.SPECTATOR);

        World baseWorld = Bukkit.getWorld(BASE_WORLD_NAME);
        if (baseWorld == null) {
            getLogger().warning("Could not teleport finished player " + player.getName() + ": world '" + BASE_WORLD_NAME + "' is not loaded.");
            return;
        }

        player.teleport(baseWorld.getSpawnLocation());
    }

    public int getCountdownRemainingSeconds() {
        if (!raceStarting) return 0;
        long remaining = countdownEndTime - System.currentTimeMillis();
        return (int) Math.max(0, Math.ceil(remaining / 1000.0));
    }

    public boolean isInLobbyWorld(Player player) {
        World world = player.getWorld();
        return world != null && world.getName().equals(BASE_WORLD_NAME);
    }

    public boolean canSeeInTab(Player viewer, Player target) {
        if (viewer.equals(target) || !raceStarted) return true;
        if (isAdmin(viewer)) return true;

        if (isInLobbyWorld(viewer)) {
            return isInLobbyWorld(target);
        }

        Team viewerTeam = teamManager.getByPlayer(viewer);
        return viewerTeam != null && viewerTeam.getPlayers().contains(target.getUniqueId());
    }

    public void refreshPlayerVisibility() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (canSeeInTab(viewer, target)) {
                    viewer.showPlayer(this, target);
                } else {
                    viewer.hidePlayer(this, target);
                }
            }
        }
    }

    public boolean isSpectator(Player player) {
        return player.getGameMode() == GameMode.SPECTATOR;
    }

    public boolean isTeamMember(Team team, Player player) {
        return team.getPlayers().contains(player.getUniqueId());
    }

    public boolean isAdmin(Player player) {
        User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return false;
        }

        return user.getPrimaryGroup().equalsIgnoreCase("admin")
                || user.getInheritedGroups(user.getQueryOptions()).stream()
                        .anyMatch(group -> group.getName().equalsIgnoreCase("admin"));
    }

    public String formatDuration(long millis) {
        long secs = millis / 1000;
        return String.format("%02d:%02d:%02d", secs / 3600, (secs % 3600) / 60, secs % 60);
    }

    private void saveFinishedTeamTimes() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().warning("Could not create plugin data folder for finished team times.");
            return;
        }

        File file = new File(getDataFolder(), "finished-team-times.yml");
        YamlConfiguration config = new YamlConfiguration();
        for (Team team : teamManager.getTeams()) {
            if (!team.isFinished()) continue;

            String path = "teams." + team.getId();
            config.set(path + ".duration-ms", team.getFinishDurationMillis());
            config.set(path + ".time", formatDuration(team.getFinishDurationMillis()));
            config.set(path + ".players", team.getPlayers().stream().map(UUID::toString).toList());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            getLogger().warning("Could not save finished team times: " + e.getMessage());
        }
    }

    private void cleanOldWorlds() {
        File serverFolder = Bukkit.getWorldContainer();
        File[] folders = serverFolder.listFiles();
        if (folders != null) {
            for (File f : folders) {
                if (f.isDirectory() && f.getName().startsWith("race_")) {
                    deleteDirectory(f);
                }
            }
        }
    }

    private void deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) deleteDirectory(file);
                    else file.delete();
                }
            }
            path.delete();
        }
    }
}

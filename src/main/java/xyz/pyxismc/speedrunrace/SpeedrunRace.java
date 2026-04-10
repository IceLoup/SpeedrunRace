package xyz.pyxismc.speedrunrace;


import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.pyxismc.speedrunrace.commands.JoinCommand;
import xyz.pyxismc.speedrunrace.commands.RaceCommand;
import xyz.pyxismc.speedrunrace.core.RaceScoreboardTask;
import xyz.pyxismc.speedrunrace.core.TeamManager;
import xyz.pyxismc.speedrunrace.core.WorldManager;
import xyz.pyxismc.speedrunrace.listeners.GameListener;
import xyz.pyxismc.speedrunrace.listeners.PortalListener;

import java.io.File;

public class SpeedrunRace extends JavaPlugin {
    private TeamManager teamManager;
    private WorldManager worldManager;

    @Override
    public void onEnable() {

        cleanOldWorlds();

        this.teamManager = new TeamManager();
        this.worldManager = new WorldManager(this);


        getCommand("race").setExecutor(new RaceCommand(this));
        getCommand("join").setExecutor(new JoinCommand(this));


        getServer().getPluginManager().registerEvents(new PortalListener(teamManager), this);
        getServer().getPluginManager().registerEvents(new GameListener(teamManager), this);


        new RaceScoreboardTask(this).runTaskTimer(this, 0L, 20L);

        getLogger().info("§aSpeedrunRace opérationnel avec LuckPerms !");
    }

    public TeamManager getTeamManager() { return teamManager; }
    public WorldManager getWorldManager() { return worldManager; }

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
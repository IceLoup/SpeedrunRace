package xyz.pyxismc.speedrunrace.listeners;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import xyz.pyxismc.speedrunrace.core.TeamManager; // Vérifie que le package est bon
import xyz.pyxismc.speedrunrace.models.Team;

public class GameListener implements Listener {

    private final TeamManager teamManager;

    // Le constructeur est indispensable pour que la classe connaisse tes équipes
    public GameListener(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    @EventHandler
    public void onWin(EntityDeathEvent e) {
        // Optimisation : on vérifie si c'est un Dragon avant de faire des calculs
        if (!(e.getEntity() instanceof EnderDragon)) return;

        World world = e.getEntity().getWorld();
        Team t = teamManager.getByWorldName(world.getName());

        if (t != null && !t.isFinished()) {
            t.setFinished(true); // On marque l'équipe comme victorieuse

            long duration = System.currentTimeMillis() - t.getStartTime();
            String time = formatTime(duration);

            Bukkit.broadcastMessage(" ");
            Bukkit.broadcastMessage("§6§l🏆 VICTOIRE DE L'ÉQUIPE " + t.getId().toUpperCase() + " 🏆");
            Bukkit.broadcastMessage("§eTemps final : §f" + time);
            Bukkit.broadcastMessage(" ");
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Team t = teamManager.getByPlayer(e.getPlayer());
        if (t != null) {
            World overworld = Bukkit.getWorld(t.getWorldName(World.Environment.NORMAL));
            if (overworld != null) {
                e.setRespawnLocation(overworld.getSpawnLocation());
            }
        }
    }

    // Petite méthode utilitaire pour rendre le temps lisible
    private String formatTime(long millis) {
        long secs = millis / 1000;
        return String.format("%02d:%02d:%02d", secs / 3600, (secs % 3600) / 60, secs % 60);
    }
}

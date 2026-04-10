package xyz.pyxismc.speedrunrace.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import xyz.pyxismc.speedrunrace.core.TeamManager;
import xyz.pyxismc.speedrunrace.models.Team;

public class PortalListener implements Listener {

    private final TeamManager teamManager;

    // Injection du TeamManager via le constructeur
    public PortalListener(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent e) {
        Player player = e.getPlayer();
        Team team = teamManager.getByPlayer(player);

        // Si le joueur n'est pas dans une équipe de course, on ne fait rien (comportement vanilla)
        if (team == null) return;

        World fromWorld = e.getFrom().getWorld();
        if (fromWorld == null) return;

        World targetWorld;
        double ratio;

        // Détermination de la destination et du ratio de coordonnées
        if (fromWorld.getEnvironment() == World.Environment.NORMAL) {
            // Overworld -> Nether
            targetWorld = Bukkit.getWorld(team.getWorldName(World.Environment.NETHER));
            ratio = 0.125; // Division par 8
        } else if (fromWorld.getEnvironment() == World.Environment.NETHER) {
            // Nether -> Overworld
            targetWorld = Bukkit.getWorld(team.getWorldName(World.Environment.NORMAL));
            ratio = 8.0; // Multiplication par 8
        } else {
            // Gestion de l'End ou autre : on laisse le comportement par défaut ou on ignore
            return;
        }

        if (targetWorld != null) {
            Location to = e.getFrom().clone();
            to.setWorld(targetWorld);
            to.setX(to.getX() * ratio);
            to.setZ(to.getZ() * ratio);

            // On définit la destination finale du portail
            e.setTo(to);
        }
    }
}

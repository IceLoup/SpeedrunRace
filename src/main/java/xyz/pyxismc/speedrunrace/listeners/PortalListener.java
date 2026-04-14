package xyz.pyxismc.speedrunrace.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import xyz.pyxismc.speedrunrace.core.TeamManager;
import xyz.pyxismc.speedrunrace.models.Team;

public class PortalListener implements Listener {

    private final TeamManager teamManager;

    public PortalListener(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent e) {
        Player player = e.getPlayer();
        Team team = teamManager.getByPlayer(player);
        if (team == null) return;

        World fromWorld = e.getFrom().getWorld();
        if (fromWorld == null) return;

        TeleportCause cause = e.getCause();

        if (cause == TeleportCause.NETHER_PORTAL) {
            handleNetherPortal(e, team, fromWorld);
        } else if (cause == TeleportCause.END_PORTAL) {
            handleEndPortal(e, team, fromWorld);
        }
    }

    private void handleNetherPortal(PlayerPortalEvent e, Team team, World fromWorld) {
        World targetWorld;
        double ratio;

        if (fromWorld.getEnvironment() == World.Environment.NORMAL) {
            targetWorld = Bukkit.getWorld(team.getWorldName(World.Environment.NETHER));
            ratio = 0.125;
        } else if (fromWorld.getEnvironment() == World.Environment.NETHER) {
            targetWorld = Bukkit.getWorld(team.getWorldName(World.Environment.NORMAL));
            ratio = 8.0;
        } else {
            return;
        }

        if (targetWorld != null) {
            Location to = e.getFrom().clone();
            to.setWorld(targetWorld);
            to.setX(to.getX() * ratio);
            to.setZ(to.getZ() * ratio);
            e.setTo(to);
        }
    }

    private void handleEndPortal(PlayerPortalEvent e, Team team, World fromWorld) {
        if (fromWorld.getEnvironment() == World.Environment.NORMAL) {

            World endWorld = Bukkit.getWorld(team.getWorldName(World.Environment.THE_END));
            if (endWorld != null) {
                e.setTo(endWorld.getSpawnLocation());
            }
        } else if (fromWorld.getEnvironment() == World.Environment.THE_END) {

            World overworld = Bukkit.getWorld(team.getWorldName(World.Environment.NORMAL));
            if (overworld != null) {
                e.setTo(overworld.getSpawnLocation());
            }
        }
    }
}
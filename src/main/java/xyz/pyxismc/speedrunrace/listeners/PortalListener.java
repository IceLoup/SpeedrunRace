package xyz.pyxismc.speedrunrace.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import xyz.pyxismc.speedrunrace.SpeedrunRace;
import xyz.pyxismc.speedrunrace.core.TeamManager;
import xyz.pyxismc.speedrunrace.models.Team;

public class PortalListener implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final SpeedrunRace plugin;
    private final TeamManager teamManager;

    public PortalListener(SpeedrunRace plugin) {
        this.plugin = plugin;
        this.teamManager = plugin.getTeamManager();
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
                e.setTo(new Location(endWorld, 100.5, 49, 0.5));
            }
        } else if (fromWorld.getEnvironment() == World.Environment.THE_END) {

            World overworld = Bukkit.getWorld(team.getWorldName(World.Environment.NORMAL));
            if (overworld != null) {
                e.setTo(overworld.getSpawnLocation());
            }
        }
    }

    @EventHandler
    public void onDragonDeath(EntityDeathEvent e) {
        if (!plugin.isRaceStarted()) return;
        if (e.getEntity().getType() != EntityType.ENDER_DRAGON) return;

        World world = e.getEntity().getWorld();
        if (world.getEnvironment() != World.Environment.THE_END) return;

        Team team = teamManager.getByWorldName(world.getName());
        if (team == null || team.isFinished()) return;
        if (!world.getName().equals(team.getWorldName(World.Environment.THE_END))) return;

        plugin.finishTeam(team);
        announceTeamWin(team);
    }

    private void announceTeamWin(Team team) {
        Component blank = MM.deserialize(" ");
        Component title = MM.deserialize("<gradient:#6b109e:#c13cff><bold>  " + team.getId().toUpperCase() + " has slain the Ender Dragon!  </bold>");
        Component time = MM.deserialize("<#6b109e>Final time: <white><bold>" + plugin.formatDuration(team.getFinishDurationMillis()) + "</bold>");

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.isTeamMember(team, player)) {
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            }

            if (plugin.isTeamMember(team, player) || plugin.isSpectator(player) || plugin.isAdmin(player)) {
                player.sendMessage(blank);
                player.sendMessage(title);
                player.sendMessage(time);
                player.sendMessage(blank);
            }
        }

        plugin.refreshPlayerVisibility();
    }
}

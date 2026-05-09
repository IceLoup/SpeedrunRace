package xyz.pyxismc.speedrunrace.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import xyz.pyxismc.speedrunrace.SpeedrunRace;
import xyz.pyxismc.speedrunrace.core.RaceScoreboardTask;
import xyz.pyxismc.speedrunrace.models.Team;

public class GameListener implements Listener {

    private final SpeedrunRace plugin;

    public GameListener(SpeedrunRace plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Team t = plugin.getTeamManager().getByPlayer(e.getPlayer());
        if (t == null) return;

        if (e.getPlayer().getGameMode() == GameMode.SPECTATOR) return;

        org.bukkit.Location custom = e.getPlayer().getRespawnLocation();
        if (custom != null) {
            String customWorld = custom.getWorld().getName();
            boolean isTeamWorld = customWorld.equals(t.getWorldName(World.Environment.NORMAL))
                    || customWorld.equals(t.getWorldName(World.Environment.NETHER))
                    || customWorld.equals(t.getWorldName(World.Environment.THE_END));
            if (isTeamWorld) return;
        }

        World overworld = Bukkit.getWorld(t.getWorldName(World.Environment.NORMAL));
        if (overworld != null) {
            e.setRespawnLocation(overworld.getSpawnLocation());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        plugin.applyFinishedTeamGameMode(e.getPlayer());
        new RaceScoreboardTask(plugin).updatePlayer(e.getPlayer());
        plugin.refreshPlayerVisibility();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Bukkit.getScheduler().runTask(plugin, plugin::refreshPlayerVisibility);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        plugin.refreshPlayerVisibility();
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent e) {
        Bukkit.getScheduler().runTask(plugin, plugin::refreshPlayerVisibility);
    }

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        if (!plugin.isRaceStarted()) return;

        Player sender = e.getPlayer();
        e.viewers().removeIf(audience -> audience instanceof Player viewer && !canReceiveChat(sender, viewer));
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Component message = e.deathMessage();
        if (message == null) return;

        e.deathMessage(null);
        sendTeamAnnouncement(e.getPlayer(), message);
    }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent e) {
        Component message = e.message();
        if (message == null) return;

        e.message(null);
        sendTeamAnnouncement(e.getPlayer(), message);
    }

    private void sendTeamAnnouncement(Player player, Component message) {
        Team team = plugin.getTeamManager().getByPlayer(player);
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (canReceiveTeamAnnouncement(player, viewer, team)) {
                viewer.sendMessage(message);
            }
        }
    }

    private boolean canReceiveTeamAnnouncement(Player player, Player viewer, Team team) {
        if (viewer.equals(player)) return true;
        if (plugin.isAdmin(player) || plugin.isAdmin(viewer)) return true;
        return team != null && team.getPlayers().contains(viewer.getUniqueId());
    }

    private boolean canReceiveChat(Player sender, Player viewer) {
        if (sender.equals(viewer)) return true;
        if (plugin.isAdmin(sender) || plugin.isAdmin(viewer)) return true;

        if (plugin.isInLobbyWorld(sender)) {
            return plugin.isInLobbyWorld(viewer);
        }

        Team senderTeam = plugin.getTeamManager().getByPlayer(sender);
        return senderTeam != null && senderTeam.getPlayers().contains(viewer.getUniqueId());
    }
}

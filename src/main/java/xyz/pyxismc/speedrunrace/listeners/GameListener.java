package xyz.pyxismc.speedrunrace.listeners;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import xyz.pyxismc.speedrunrace.core.TeamManager;
import xyz.pyxismc.speedrunrace.models.Team;

public class GameListener implements Listener {

    private final TeamManager teamManager;
    private static final MiniMessage MM = MiniMessage.miniMessage();


    public GameListener(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    @EventHandler
    public void onWin(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof EnderDragon)) return;

        World world = e.getEntity().getWorld();
        Team t = teamManager.getByWorldName(world.getName());

        if (t != null && !t.isFinished()) {
            t.setFinished(true);

            long duration = System.currentTimeMillis() - t.getStartTime();
            String time = formatTime(duration);

            Bukkit.broadcast(MM.deserialize(" "));
            Bukkit.broadcast(MM.deserialize("<gradient:yellow:gold><bold>Victory — " + t.getId().toUpperCase()));
            Bukkit.broadcast(MM.deserialize("<gray>Final time: <white>" + time));
            Bukkit.broadcast(MM.deserialize(" "));
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

    private String formatTime(long millis) {
        long secs = millis / 1000;
        return String.format("%02d:%02d:%02d", secs / 3600, (secs % 3600) / 60, secs % 60);
    }
}

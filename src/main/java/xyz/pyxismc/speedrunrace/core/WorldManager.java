package xyz.pyxismc.speedrunrace.core;

import xyz.pyxismc.speedrunrace.SpeedrunRace;
import xyz.pyxismc.speedrunrace.models.Team;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class WorldManager {
    private final SpeedrunRace plugin;
    public WorldManager(SpeedrunRace plugin) { this.plugin = plugin; }

    public void prepareTeamWorlds(Team team, Runnable onReady) {
        copyAndLoad("template_overworld", team.getWorldName(World.Environment.NORMAL), World.Environment.NORMAL, () -> {
            copyAndLoad("template_nether", team.getWorldName(World.Environment.NETHER), World.Environment.NETHER, () -> {
                copyAndLoad("template_end", team.getWorldName(World.Environment.THE_END), World.Environment.THE_END, onReady);
            });
        });
    }

    private void copyAndLoad(String source, String target, World.Environment env, Runnable done) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                File sourceDir = new File(Bukkit.getWorldContainer(), source);
                File targetDir = new File(Bukkit.getWorldContainer(), target);
                copyDirectory(sourceDir.toPath(), targetDir.toPath());
                new File(targetDir, "uid.dat").delete();
                new File(targetDir, "session.lock").delete();

                Bukkit.getScheduler().runTask(plugin, () -> {
                    new WorldCreator(target).environment(env).createWorld();
                    if (done != null) done.run();
                });
            } catch (IOException e) { e.printStackTrace(); }
        });
    }

    private void copyDirectory(Path s, Path t) throws IOException {
        Files.walkFileTree(s, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) throws IOException {
                Path targetDir = t.resolve(s.relativize(d));
                if (!Files.exists(targetDir)) Files.createDirectory(targetDir);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFile(Path f, BasicFileAttributes a) throws IOException {
                Files.copy(f, t.resolve(s.relativize(f)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}

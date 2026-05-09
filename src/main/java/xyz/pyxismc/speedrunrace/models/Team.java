package xyz.pyxismc.speedrunrace.models;

import org.bukkit.World;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Team {
    private final String id;
    private final List<UUID> players = new ArrayList<>();
    private long startTime = 0;
    private long finishDurationMillis = 0;
    private boolean finished = false;

    public Team(String id) { this.id = id; }
    public String getId() { return id; }
    public List<UUID> getPlayers() { return players; }
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public long getFinishDurationMillis() { return finishDurationMillis; }
    public void setFinishDurationMillis(long finishDurationMillis) { this.finishDurationMillis = finishDurationMillis; }
    public boolean isFinished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; }

    public void resetRaceProgress() {
        this.startTime = 0;
        this.finishDurationMillis = 0;
        this.finished = false;
    }

    public String getWorldName(World.Environment env) {
        switch (env) {
            case NETHER: return "race_" + id + "_nether";
            case THE_END: return "race_" + id + "_end";
            default: return "race_" + id + "_overworld";
        }
    }
}

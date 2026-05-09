package xyz.pyxismc.speedrunrace.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import xyz.pyxismc.speedrunrace.SpeedrunRace;
import xyz.pyxismc.speedrunrace.models.Team;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.pyxismc.speedrunrace.core.RaceScoreboardTask;
import xyz.pyxismc.speedrunrace.core.TeamManager;

public class JoinCommand implements CommandExecutor {
    private final SpeedrunRace plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public JoinCommand(SpeedrunRace p) { this.plugin = p; }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] args) {
        if (!(s instanceof Player p)) return true;
        if (args.length == 0) return false;

        String name = plugin.getTeamManager().normalizeTeamId(args[0]);
        LuckPerms lp = LuckPermsProvider.get();
        Group g = lp.getGroupManager().getGroup(name);

        if (g == null) {
            p.sendMessage(MM.deserialize("<#c13cff>This LuckPerms group does not exist!"));
            return true;
        }

        Team t = plugin.getTeamManager().getOrCreateTeam(name);
        if (!plugin.getTeamManager().canJoin(t, p)) {
            p.sendMessage(MM.deserialize("<#c13cff>This team is full <gray>(" + TeamManager.MAX_TEAM_SIZE + "/" + TeamManager.MAX_TEAM_SIZE + ")!"));
            return true;
        }

        User u = lp.getUserManager().getUser(p.getUniqueId());
        if (u != null) {
            u.data().clear(n -> n.getKey().startsWith("group."));
            u.data().add(InheritanceNode.builder(g).build());
            lp.getUserManager().saveUser(u);
        }

        Team joinedTeam = plugin.getTeamManager().addPlayerToTeam(name, p);
        p.sendMessage(MM.deserialize(
                "<gradient:#6b109e:#c13cff>You have joined team <team> <gray>(<size>/" + TeamManager.MAX_TEAM_SIZE + ")!",
                Placeholder.unparsed("team", name),
                Placeholder.unparsed("size", String.valueOf(joinedTeam.getPlayers().size()))
        ));
        new RaceScoreboardTask(plugin).updatePlayer(p);
        plugin.refreshPlayerVisibility();
        return true;
    }
}

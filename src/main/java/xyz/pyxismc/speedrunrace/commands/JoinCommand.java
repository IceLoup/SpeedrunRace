package xyz.pyxismc.speedrunrace.commands;

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

public class JoinCommand implements CommandExecutor {
    private final SpeedrunRace plugin;
    public JoinCommand(SpeedrunRace p) { this.plugin = p; }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] args) {
        if (!(s instanceof Player p)) return true;
        if (args.length == 0) return false;

        String name = args[0].toLowerCase();
        LuckPerms lp = LuckPermsProvider.get();
        Group g = lp.getGroupManager().getGroup(name);

        if (g == null) {
            p.sendMessage("§cCe groupe LuckPerms n'existe pas !");
            return true;
        }

        Team t = plugin.getTeamManager().getOrCreateTeam(name);
        if (t.getPlayers().size() >= 3 && !t.getPlayers().contains(p.getUniqueId())) {
            p.sendMessage("§cÉquipe pleine (3/3) !");
            return true;
        }

        User u = lp.getUserManager().getUser(p.getUniqueId());
        if (u != null) {
            u.data().clear(n -> n.getKey().startsWith("group."));
            u.data().add(InheritanceNode.builder(g).build());
            lp.getUserManager().saveUser(u);
        }

        plugin.getTeamManager().addPlayerToTeam(name, p);
        p.sendMessage("§aTu as rejoint l'équipe §e" + name);
        return true;
    }
}

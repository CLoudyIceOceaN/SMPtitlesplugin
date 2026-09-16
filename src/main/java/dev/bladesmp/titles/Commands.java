package dev.bladesmp.titles;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

    private final TitlesPlugin plugin;

    public Commands(TitlesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("titles.admin")) {
                sender.sendMessage(plugin.msg("no-permission"));
                return true;
            }
            plugin.reloadConfig();
            plugin.getTitles().loadTitles();
            // refresh everyone's floating title in case one was renamed
            for (Player p : Bukkit.getOnlinePlayers()) {
                plugin.getHeads().applyEquipped(p);
            }
            sender.sendMessage(plugin.msg("reloaded"));
            return true;
        }

        if (args.length >= 3 && (args[0].equalsIgnoreCase("give")
                || args[0].equalsIgnoreCase("take"))) {
            if (!sender.hasPermission("titles.admin")) {
                sender.sendMessage(plugin.msg("no-permission"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(plugin.msg("player-not-found"));
                return true;
            }
            Titles.Title t = plugin.getTitles().get(args[2]);
            if (t == null) {
                sender.sendMessage(plugin.msg("title-not-found"));
                return true;
            }
            if (args[0].equalsIgnoreCase("give")) {
                plugin.getTitles().addOwned(target.getUniqueId(), t.id);
                sender.sendMessage(plugin.msg("given")
                        .replace("%player%", target.getName())
                        .replace("%title%", t.display));
                target.sendMessage(plugin.msg("won")
                        .replace("%title%", t.display));
            } else {
                plugin.getTitles().removeOwned(target.getUniqueId(), t.id);
                plugin.getHeads().applyEquipped(target);
                sender.sendMessage(plugin.msg("taken")
                        .replace("%player%", target.getName())
                        .replace("%title%", t.display));
            }
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("npc")) {
            if (!sender.hasPermission("titles.admin")) {
                sender.sendMessage(plugin.msg("no-permission"));
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage("Players only.");
                return true;
            }
            plugin.getNpcs().startLinking((Player) sender);
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only. (Admin: /titles reload, /titles give <player> <id>)");
            return true;
        }
        plugin.getGui().openShop((Player) sender, 0);
        return true;
    }
}

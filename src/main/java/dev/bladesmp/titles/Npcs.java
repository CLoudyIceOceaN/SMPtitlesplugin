package dev.bladesmp.titles;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Link any mob (like a villager) to the titles menu:
 * an admin runs /titles npc, right-clicks the mob, and from
 * then on right-clicking it opens /titles for everyone.
 */
public class Npcs implements Listener {

    private final TitlesPlugin plugin;
    // admins who ran /titles npc and are about to click a mob
    private final Set<UUID> linking = new HashSet<UUID>();

    public Npcs(TitlesPlugin plugin) {
        this.plugin = plugin;
    }

    public void startLinking(Player player) {
        if (linking.remove(player.getUniqueId())) {
            player.sendMessage(plugin.msg("npc-mode-off"));
        } else {
            linking.add(player.getUniqueId());
            player.sendMessage(plugin.msg("npc-mode-on"));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEntityEvent event) {
        Entity target = event.getRightClicked();
        if (target.hasMetadata("smp-title")) return; // our floating titles
        Player player = event.getPlayer();

        if (linking.contains(player.getUniqueId())) {
            event.setCancelled(true);
            linking.remove(player.getUniqueId());
            boolean linked = plugin.getTitles().toggleNpc(target.getUniqueId());
            player.sendMessage(plugin.msg(linked ? "npc-linked" : "npc-unlinked"));
            return;
        }

        if (plugin.getTitles().isNpc(target.getUniqueId())) {
            event.setCancelled(true); // e.g. don't open villager trades
            plugin.getGui().openShop(player, 0);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (plugin.getConfig().getBoolean("protect-npcs", true)
                && plugin.getTitles().isNpc(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}

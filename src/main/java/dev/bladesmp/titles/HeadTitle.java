package dev.bladesmp.titles;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Puts the title ABOVE the player's nametag.
 *
 * Trick: an invisible, tiny armor stand rides on the player's head and
 * carries the title as its name — so the title floats above the normal
 * nametag. Works on plain 1.8 clients (Eaglercraft included).
 */
public class HeadTitle implements Listener {

    // invisible marker at the end of our stands' names, so we can always
    // recognise (and clean up) our own armor stands
    private static final String MARK = "§0§0§r";
    private static final String META = "smp-title";

    private final TitlesPlugin plugin;
    private final Map<UUID, ArmorStand> stands = new HashMap<UUID, ArmorStand>();

    public HeadTitle(TitlesPlugin plugin) {
        this.plugin = plugin;
    }

    /** Show this player's saved title (or nothing if they have none). */
    public void applyEquipped(Player player) {
        Titles.Title title = plugin.getTitles().equippedTitle(player.getUniqueId());
        apply(player, title == null ? null : title.display);
    }

    public void apply(Player player, String display) {
        remove(player);
        if (display == null || !player.isOnline()) return;
        ArmorStand stand = player.getWorld().spawn(player.getLocation(), ArmorStand.class);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setSmall(true);
        stand.setMarker(true);              // no hitbox, so nothing gets blocked
        stand.setCustomName(display + MARK);
        stand.setCustomNameVisible(true);
        stand.setMetadata(META, new FixedMetadataValue(plugin, true));
        player.setPassenger(stand);
        stands.put(player.getUniqueId(), stand);
    }

    public void remove(Player player) {
        ArmorStand stand = stands.remove(player.getUniqueId());
        if (stand != null && !stand.isDead()) stand.remove();
    }

    public void removeAll() {
        for (ArmorStand stand : stands.values()) {
            if (!stand.isDead()) stand.remove();
        }
        stands.clear();
    }

    /** Every 2 seconds: put back any title that fell off (teleports etc). */
    public void startKeeper() {
        Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (plugin.getTitles().equippedTitle(p.getUniqueId()) == null) continue;
                    ArmorStand stand = stands.get(p.getUniqueId());
                    if (stand == null || stand.isDead() || p.getPassenger() != stand) {
                        applyEquipped(p);
                    }
                }
            }
        }, 40L, 40L);
    }

    /** Deletes leftover title stands (e.g. after a crash). */
    public void cleanOrphans() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                cleanIfOrphan(entity);
            }
        }
    }

    private void cleanIfOrphan(Entity entity) {
        if (!(entity instanceof ArmorStand)) return;
        String name = entity.getCustomName();
        if (name == null || !name.endsWith(MARK)) return;
        if (!stands.containsValue(entity)) entity.remove();
    }

    // ===== keeping everything tidy =====

    private void applyLater(final Player player) {
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            public void run() {
                applyEquipped(player);
            }
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        applyLater(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        remove(event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        remove(event.getEntity());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        applyLater(event.getPlayer());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        // the stand can't teleport along; give the player a fresh one
        applyLater(event.getPlayer());
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            cleanIfOrphan(entity);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity().hasMetadata(META)) event.setCancelled(true);
    }
}

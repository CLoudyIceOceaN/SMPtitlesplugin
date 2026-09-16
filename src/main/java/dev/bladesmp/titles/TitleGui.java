package dev.bladesmp.titles;

import dev.bladesmp.titles.Titles.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The /titles menus. Titles are shown as enchanted books.
 */
public class TitleGui implements Listener {

    private final TitlesPlugin plugin;

    public TitleGui(TitlesPlugin plugin) {
        this.plugin = plugin;
    }

    private static class ShopHolder implements InventoryHolder {
        int page; Inventory inv;
        Map<Integer, String> slots = new HashMap<Integer, String>();
        public Inventory getInventory() { return inv; }
    }

    private static class MineHolder implements InventoryHolder {
        Inventory inv;
        Map<Integer, String> slots = new HashMap<Integer, String>();
        public Inventory getInventory() { return inv; }
    }

    private static class ConfirmHolder implements InventoryHolder {
        String titleId; int page; Inventory inv;
        public Inventory getInventory() { return inv; }
    }

    // ===== the shop: every title on the server =====

    public void openShop(Player player, int page) {
        List<Title> all = plugin.getTitles().all();
        int pages = Math.max(1, (all.size() + 44) / 45);
        if (page >= pages) page = pages - 1;
        if (page < 0) page = 0;

        ShopHolder holder = new ShopHolder();
        holder.page = page;
        String title = plugin.color(plugin.getConfig().getString("gui.shop-title", "&8Titles"));
        if (pages > 1) title = trim(title + " (" + (page + 1) + "/" + pages + ")");
        Inventory inv = Bukkit.createInventory(holder, 54, trim(title));
        holder.inv = inv;

        int start = page * 45;
        for (int i = 0; i < 45 && start + i < all.size(); i++) {
            Title t = all.get(start + i);
            inv.setItem(i, shopBook(player, t));
            holder.slots.put(i, t.id);
        }
        if (all.isEmpty()) {
            inv.setItem(22, item(Material.BARRIER, "&cNo titles yet!",
                    "&7The server owner can add them in",
                    "&fplugins/SMPtitlesplugin/config.yml",
                    "&7— there are examples in there."));
        }

        ItemStack pane = item(Material.STAINED_GLASS_PANE, (short) 7, "&7");
        for (int i = 45; i < 54; i++) inv.setItem(i, pane);
        inv.setItem(48, item(Material.GOLD_INGOT, "&aYour Balance",
                "&f" + plugin.getEconomy().format(plugin.getEconomy().get(player))));
        inv.setItem(49, item(Material.CHEST, "&eYour Titles",
                "&7Owned: &f" + plugin.getTitles().ownedBy(player).size(),
                "", "&eClick &7to view and equip them"));
        if (page > 0) inv.setItem(52, item(Material.ARROW, "&e◀ Previous Page"));
        if (page < pages - 1) inv.setItem(53, item(Material.ARROW, "&eNext Page ▶"));

        player.openInventory(inv);
    }

    private ItemStack shopBook(Player player, Title t) {
        boolean owned = plugin.getTitles().owns(player, t.id);
        boolean equipped = t.id.equalsIgnoreCase(
                String.valueOf(plugin.getTitles().equippedId(player.getUniqueId())));
        List<String> lore = new ArrayList<String>();
        lore.add(plugin.color("&7Above your head it shows:"));
        lore.add(plugin.color("   " + t.display));
        lore.add("");
        if (equipped) {
            lore.add(plugin.color("&a&lEQUIPPED"));
            lore.add(plugin.color("&eClick &7to take it off"));
        } else if (owned) {
            lore.add(plugin.color("&aYou own this title"));
            lore.add(plugin.color("&eClick &7to equip it"));
        } else {
            lore.add(plugin.color("&7Price: &a" + plugin.getEconomy().format(t.price)));
            lore.add(plugin.color("&eClick &7to buy it"));
        }
        return book(t.display, lore);
    }

    // ===== your titles (the chest) =====

    public void openMine(Player player) {
        MineHolder holder = new MineHolder();
        Inventory inv = Bukkit.createInventory(holder, 54, plugin.color(
                plugin.getConfig().getString("gui.mine-title", "&8Your Titles")));
        holder.inv = inv;

        String equippedId = plugin.getTitles().equippedId(player.getUniqueId());
        int slot = 0;
        for (Title t : plugin.getTitles().ownedBy(player)) {
            if (slot >= 45) break;
            List<String> lore = new ArrayList<String>();
            lore.add(plugin.color("&7Above your head it shows:"));
            lore.add(plugin.color("   " + t.display));
            lore.add("");
            if (t.id.equalsIgnoreCase(String.valueOf(equippedId))) {
                lore.add(plugin.color("&a&lEQUIPPED"));
                lore.add(plugin.color("&eClick &7to take it off"));
            } else {
                lore.add(plugin.color("&eClick &7to equip it"));
            }
            inv.setItem(slot, book(t.display, lore));
            holder.slots.put(slot, t.id);
            slot++;
        }
        if (slot == 0) {
            inv.setItem(22, item(Material.BARRIER, "&cYou don't own any titles yet",
                    "&7Buy them in the titles shop!"));
        }

        ItemStack pane = item(Material.STAINED_GLASS_PANE, (short) 7, "&7");
        for (int i = 45; i < 54; i++) inv.setItem(i, pane);
        inv.setItem(48, item(Material.BARRIER, "&cRemove my title",
                "&7Take off whatever you're wearing"));
        inv.setItem(49, item(Material.ARROW, "&e◀ Back to the shop"));

        player.openInventory(inv);
    }

    // ===== confirm buying =====

    private void openConfirm(Player player, Title t, int page) {
        ConfirmHolder holder = new ConfirmHolder();
        holder.titleId = t.id;
        holder.page = page;
        Inventory inv = Bukkit.createInventory(holder, 27, plugin.color(
                plugin.getConfig().getString("gui.confirm-title", "&8Buy this title?")));
        holder.inv = inv;

        ItemStack pane = item(Material.STAINED_GLASS_PANE, (short) 7, "&7");
        for (int i = 0; i < 27; i++) inv.setItem(i, pane);
        inv.setItem(11, item(Material.WOOL, (short) 5, "&a&lBUY IT",
                "&7Costs &a" + plugin.getEconomy().format(t.price)));
        inv.setItem(13, book(t.display, new ArrayList<String>()));
        inv.setItem(15, item(Material.WOOL, (short) 14, "&c&lCANCEL"));

        player.openInventory(inv);
    }

    // ===== clicks =====

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof ShopHolder) {
            event.setCancelled(true);
            ShopHolder shop = (ShopHolder) holder;
            int slot = event.getRawSlot();
            if (slot < 0 || slot >= 54) return;
            if (slot < 45) {
                String id = shop.slots.get(slot);
                if (id == null) return;
                Title t = plugin.getTitles().get(id);
                if (t == null) return;
                if (plugin.getTitles().owns(player, id)) {
                    toggleEquip(player, t);
                    openShop(player, shop.page);
                } else {
                    openConfirm(player, t, shop.page);
                }
            } else if (slot == 49) {
                openMine(player);
            } else if (slot == 52 && isArrow(event.getCurrentItem())) {
                openShop(player, shop.page - 1);
            } else if (slot == 53 && isArrow(event.getCurrentItem())) {
                openShop(player, shop.page + 1);
            }
            return;
        }

        if (holder instanceof MineHolder) {
            event.setCancelled(true);
            MineHolder mine = (MineHolder) holder;
            int slot = event.getRawSlot();
            if (slot == 49) {
                openShop(player, 0);
                return;
            }
            if (slot == 48) {
                plugin.getTitles().setEquipped(player.getUniqueId(), null);
                plugin.getHeads().apply(player, null);
                player.sendMessage(plugin.msg("unequipped"));
                openMine(player);
                return;
            }
            String id = mine.slots.get(slot);
            if (id != null) {
                Title t = plugin.getTitles().get(id);
                if (t != null) toggleEquip(player, t);
                openMine(player);
            }
            return;
        }

        if (holder instanceof ConfirmHolder) {
            event.setCancelled(true);
            ConfirmHolder confirm = (ConfirmHolder) holder;
            int slot = event.getRawSlot();
            if (slot == 11) {
                buy(player, confirm.titleId, confirm.page);
            } else if (slot == 15) {
                openShop(player, confirm.page);
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof ShopHolder || holder instanceof MineHolder
                || holder instanceof ConfirmHolder) {
            for (int raw : event.getRawSlots()) {
                if (raw < event.getInventory().getSize()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    private void toggleEquip(Player player, Title t) {
        String current = plugin.getTitles().equippedId(player.getUniqueId());
        if (t.id.equalsIgnoreCase(String.valueOf(current))) {
            plugin.getTitles().setEquipped(player.getUniqueId(), null);
            plugin.getHeads().apply(player, null);
            player.sendMessage(plugin.msg("unequipped"));
        } else {
            plugin.getTitles().setEquipped(player.getUniqueId(), t.id);
            plugin.getHeads().apply(player, t.display);
            player.sendMessage(plugin.msg("equipped").replace("%title%", t.display));
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1f);
        }
    }

    private void buy(Player player, String id, int page) {
        Title t = plugin.getTitles().get(id);
        if (t == null) {
            openShop(player, page);
            return;
        }
        if (plugin.getTitles().owns(player, id)) {
            openShop(player, page);
            return;
        }
        if (!plugin.getEconomy().take(player, t.price)) {
            player.sendMessage(plugin.msg("not-enough"));
            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
            openShop(player, page);
            return;
        }
        plugin.getTitles().addOwned(player.getUniqueId(), id);
        player.sendMessage(plugin.msg("bought").replace("%title%", t.display));
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1f);
        // put it on right away — that's clearly what they wanted
        plugin.getTitles().setEquipped(player.getUniqueId(), id);
        plugin.getHeads().apply(player, t.display);
        openShop(player, page);
    }

    // ===== helpers =====

    private ItemStack book(String name, List<String> lore) {
        ItemStack stack = new ItemStack(Material.ENCHANTED_BOOK, 1);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(plugin.color("&r") + name);
        if (!lore.isEmpty()) meta.setLore(lore);
        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack item(Material material, String name, String... lore) {
        return item(material, (short) 0, name, lore);
    }

    private ItemStack item(Material material, short data, String name, String... lore) {
        ItemStack stack = new ItemStack(material, 1, data);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(plugin.color(name));
        if (lore.length > 0) {
            List<String> lines = new ArrayList<String>();
            for (String line : lore) lines.add(plugin.color(line));
            meta.setLore(lines);
        }
        stack.setItemMeta(meta);
        return stack;
    }

    private boolean isArrow(ItemStack stack) {
        return stack != null && stack.getType() == Material.ARROW;
    }

    private String trim(String s) {
        return s.length() <= 32 ? s : s.substring(0, 32);
    }
}

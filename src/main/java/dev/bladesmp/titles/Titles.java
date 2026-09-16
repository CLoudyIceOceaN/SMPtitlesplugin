package dev.bladesmp.titles;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The titles from config.yml, plus who owns and wears what
 * (saved in plugins/SMPtitlesplugin/players.yml).
 */
public class Titles {

    /** One title from the config. */
    public static class Title {
        public final String id;
        public final String display;   // colored, font applied — ready to show
        public final double price;
        public final boolean buyable;  // false = event/giveaway reward only
        public final List<String> description;

        public Title(String id, String display, double price,
                     boolean buyable, List<String> description) {
            this.id = id;
            this.display = display;
            this.price = price;
            this.buyable = buyable;
            this.description = description;
        }
    }

    private final TitlesPlugin plugin;
    private final File file;
    private final List<Title> all = new ArrayList<Title>();
    private final Map<UUID, Set<String>> owned = new HashMap<UUID, Set<String>>();
    private final Map<UUID, String> equipped = new HashMap<UUID, String>();
    // Mobs that open the titles menu when right-clicked
    private final Set<UUID> npcs = new HashSet<UUID>();

    public Titles(TitlesPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");
        loadTitles();
        loadPlayers();
    }

    /** Reads the titles list out of config.yml. */
    public void loadTitles() {
        all.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("titles");
        if (section == null) return;
        for (String id : section.getKeys(false)) {
            ConfigurationSection s = section.getConfigurationSection(id);
            if (s == null) continue;
            String raw = s.getString("name", id);
            String styled = applyFont(raw, s.getString("font", "normal"));
            String display = s.getBoolean("rainbow", false)
                    ? rainbow(styled, s.getBoolean("bold", false))
                    : plugin.color(styled);
            List<String> description = new ArrayList<String>();
            for (String line : s.getStringList("description")) {
                description.add(plugin.color(line));
            }
            all.add(new Title(id, display, s.getDouble("price", 0),
                    s.getBoolean("buyable", true), description));
        }
    }

    private void loadPlayers() {
        if (!file.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String key : yaml.getKeys(false)) {
            if (key.equals("npcs")) continue;
            try {
                UUID id = UUID.fromString(key);
                owned.put(id, new HashSet<String>(yaml.getStringList(key + ".owned")));
                String eq = yaml.getString(key + ".equipped");
                if (eq != null) equipped.put(id, eq);
            } catch (IllegalArgumentException ignored) {
            }
        }
        for (String id : yaml.getStringList("npcs")) {
            try {
                npcs.add(UUID.fromString(id));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        Set<UUID> everyone = new HashSet<UUID>(owned.keySet());
        everyone.addAll(equipped.keySet());
        for (UUID id : everyone) {
            Set<String> titles = owned.get(id);
            yaml.set(id + ".owned", titles == null
                    ? new ArrayList<String>() : new ArrayList<String>(titles));
            yaml.set(id + ".equipped", equipped.get(id));
        }
        List<String> npcList = new ArrayList<String>();
        for (UUID id : npcs) npcList.add(id.toString());
        yaml.set("npcs", npcList);
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save titles: " + e.getMessage());
        }
    }

    public List<Title> all() { return all; }

    public Title get(String id) {
        for (Title t : all) {
            if (t.id.equalsIgnoreCase(id)) return t;
        }
        return null;
    }

    public boolean owns(Player player, String id) {
        Set<String> set = owned.get(player.getUniqueId());
        return set != null && set.contains(id);
    }

    public void addOwned(UUID player, String id) {
        Set<String> set = owned.get(player);
        if (set == null) {
            set = new HashSet<String>();
            owned.put(player, set);
        }
        set.add(id);
        save();
    }

    /** Take a title away (also takes it off their head if worn). */
    public void removeOwned(UUID player, String id) {
        Set<String> set = owned.get(player);
        if (set != null) set.remove(id);
        if (id.equalsIgnoreCase(String.valueOf(equipped.get(player)))) {
            equipped.remove(player);
        }
        save();
    }

    public List<Title> ownedBy(Player player) {
        List<Title> list = new ArrayList<Title>();
        for (Title t : all) {
            if (owns(player, t.id)) list.add(t);
        }
        return list;
    }

    public String equippedId(UUID player) {
        return equipped.get(player);
    }

    /** The title a player is wearing, or null (also null if it was deleted). */
    public Title equippedTitle(UUID player) {
        String id = equipped.get(player);
        return id == null ? null : get(id);
    }

    public void setEquipped(UUID player, String id) {
        if (id == null) equipped.remove(player);
        else equipped.put(player, id);
        save();
    }

    // ===== npc links =====

    public boolean isNpc(UUID entity) {
        return npcs.contains(entity);
    }

    /** Links or unlinks a mob. Returns true if it is now linked. */
    public boolean toggleNpc(UUID entity) {
        boolean nowLinked;
        if (npcs.contains(entity)) {
            npcs.remove(entity);
            nowLinked = false;
        } else {
            npcs.add(entity);
            nowLinked = true;
        }
        save();
        return nowLinked;
    }

    // ===== fonts =====
    // Turns normal letters into fancy Minecraft-friendly alphabets.
    // Color codes (&6 etc.) are skipped so they keep working.

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String SMALLCAPS = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢ";
    private static final char[] RAINBOW = {'c', '6', 'e', 'a', 'b', 'd'};

    /** Colors every letter a different color. Existing color codes are dropped. */
    public static String rainbow(String text, boolean bold) {
        StringBuilder out = new StringBuilder();
        int color = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ((c == '&' || c == '§') && i + 1 < text.length()) {
                i++; // rainbow picks the colors — skip any written codes
                continue;
            }
            if (c == ' ') {
                out.append(' ');
                continue;
            }
            out.append('§').append(RAINBOW[color++ % RAINBOW.length]);
            if (bold) out.append('§').append('l');
            out.append(c);
        }
        return out.toString();
    }

    public static String applyFont(String text, String font) {
        if (font == null || font.equalsIgnoreCase("normal")) return text;
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ((c == '&' || c == '§') && i + 1 < text.length()) {
                out.append(c).append(text.charAt(i + 1)); // keep color codes
                i++;
                continue;
            }
            out.append(convert(c, font.toLowerCase()));
        }
        return out.toString();
    }

    private static String convert(char c, String font) {
        char lower = Character.toLowerCase(c);
        int idx = LOWER.indexOf(lower);
        if (font.equals("smallcaps")) {
            return idx < 0 ? String.valueOf(c) : String.valueOf(SMALLCAPS.charAt(idx));
        }
        if (font.equals("fullwidth")) {
            if (c >= 'a' && c <= 'z') return String.valueOf((char) (0xFF41 + c - 'a'));
            if (c >= 'A' && c <= 'Z') return String.valueOf((char) (0xFF21 + c - 'A'));
            if (c >= '0' && c <= '9') return String.valueOf((char) (0xFF10 + c - '0'));
            return String.valueOf(c);
        }
        if (font.equals("circled")) {
            if (c >= 'a' && c <= 'z') return String.valueOf((char) (0x24D0 + c - 'a'));
            if (c >= 'A' && c <= 'Z') return String.valueOf((char) (0x24B6 + c - 'A'));
            if (c >= '1' && c <= '9') return String.valueOf((char) (0x2460 + c - '1'));
            if (c == '0') return "⓪";
            return String.valueOf(c);
        }
        return String.valueOf(c);
    }
}

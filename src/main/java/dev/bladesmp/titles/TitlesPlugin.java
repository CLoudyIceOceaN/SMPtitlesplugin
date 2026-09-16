package dev.bladesmp.titles;

import dev.bladesmp.money.Economy;
import dev.bladesmp.money.MoneyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TitlesPlugin extends JavaPlugin {

    private Economy economy;
    private Titles titles;
    private HeadTitle heads;
    private TitleGui gui;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        MoneyPlugin money = (MoneyPlugin) Bukkit.getPluginManager().getPlugin("SMPmoneyplugin");
        if (money == null) {
            getLogger().severe("SMPmoneyplugin is missing! Install it first.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        economy = money.getEconomy();

        titles = new Titles(this);
        heads = new HeadTitle(this);
        gui = new TitleGui(this);
        Bukkit.getPluginManager().registerEvents(heads, this);
        Bukkit.getPluginManager().registerEvents(gui, this);

        getCommand("titles").setExecutor(new Commands(this));

        heads.cleanOrphans();       // remove leftover floating titles from a crash
        heads.startKeeper();        // keeps titles glued above heads
        for (Player p : Bukkit.getOnlinePlayers()) {
            heads.applyEquipped(p);
        }
        getLogger().info("Titles are ready!");
    }

    @Override
    public void onDisable() {
        if (heads != null) heads.removeAll();
        if (titles != null) titles.save();
    }

    public Economy getEconomy() { return economy; }
    public Titles getTitles() { return titles; }
    public HeadTitle getHeads() { return heads; }
    public TitleGui getGui() { return gui; }

    public String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public String msg(String key) {
        String prefix = getConfig().getString("messages.prefix", "&d&lTitles &8» &f");
        return color(prefix + getConfig().getString("messages." + key, key));
    }
}

package com.gmail.bobason01;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class MMOItemsEquipExpansion extends JavaPlugin {
    @Override
    public void onEnable() {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().severe("PlaceholderAPI missing");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (!Bukkit.getPluginManager().isPluginEnabled("MythicLib")) {
            getLogger().severe("MythicLib missing");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (!Bukkit.getPluginManager().isPluginEnabled("MMOItems")) {
            getLogger().severe("MMOItems missing");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (!Bukkit.getPluginManager().isPluginEnabled("MMOInventory")) {
            getLogger().severe("MMOInventory missing");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        new MMOItemsPlaceholder().register();
        getLogger().info("MMOItemsEquip registered");
    }
}

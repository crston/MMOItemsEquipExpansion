package com.gmail.bobason01.mmoitemsequipexpansion;

import org.bukkit.plugin.java.JavaPlugin;

public class MMOItemsEquipExpansion extends JavaPlugin {

    @Override
    public void onEnable() {
        if (!getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().severe("PlaceholderAPI is not installed! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!getServer().getPluginManager().isPluginEnabled("MMOItems")) {
            getLogger().severe("MMOItems is not installed! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        new MMOItemsPlaceholder(this).register();
    }
}
package com.gmail.bobason01;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MMOItemsEquipExpansion extends JavaPlugin {

    // 플러그인 활성화 시 실행
    @Override
    public void onEnable() {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().severe("PlaceholderAPI is not installed! Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (!Bukkit.getPluginManager().isPluginEnabled("MMOItems")) {
            getLogger().severe("MMOItems is not installed! Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // MMOItemsPlaceholder 인스턴스를 생성하고 등록합니다.
        new MMOItemsPlaceholder(this).register();
    }
}
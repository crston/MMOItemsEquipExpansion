package com.gmail.bobason01.mmoitemsequipexpansion;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.manager.StatManager;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import java.util.Map;
import java.util.HashMap;
import java.util.Locale;

public class MMOItemsPlaceholder extends PlaceholderExpansion {

    private final Map<String, ItemStat<?, ?>> itemStatsCache = new HashMap<>();

    public MMOItemsPlaceholder(MMOItemsEquipExpansion MMOItemsEquipExpansion) {
    }

    @Override
    public @NotNull String getIdentifier() {
        return "mmoitemsequip";
    }

    @Override
    public @NotNull String getAuthor() {
        return "crston";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean register() {
        boolean registered = super.register();
        if (registered) {
            cacheItemStats();
        }
        return registered;
    }

    private void cacheItemStats() {
        StatManager statManager = MMOItems.plugin.getStats();
        for (ItemStat<?, ?> itemStat : statManager.getAll()) {
            itemStatsCache.put(itemStat.getId(), itemStat);
        }
    }

    private String getMMOItemCheckStat(Player player, EquipmentSlot slot, String statName) {
        ItemStack itemStack = getItem(player, slot);

        if (itemStack == null) {
            return "0.0";
        }

        NBTItem nbtItem = NBTItem.get(itemStack);
        if (!nbtItem.hasType()) {
            return "0.0";
        }

        Type type = Type.get(nbtItem.getType());
        if (type == null) {
            return "0.0";
        }

        MMOItem mmoItem = MMOItems.plugin.getMMOItem(type, nbtItem.getString("MMOITEMS_ITEM_ID"));
        if (mmoItem == null) {
            return "0.0";
        }

        ItemStat<?, ?> itemStat = itemStatsCache.get(statName);
        if (itemStat == null || !mmoItem.hasData(itemStat)) {
            return "0.0";
        }

        Object statData = mmoItem.getData(itemStat);
        return statData != null ? statData.toString() : "Invalid Stat Type";
    }

    private double getStatValue(ItemStack item, String stat) {
        NBTItem nbtItem = NBTItem.get(item);
        if (!nbtItem.hasType()) {
            return 0.0;
        }

        Type type = Type.get(nbtItem.getType());
        if (type == null) {
            return 0.0;
        }

        MMOItem mmoItem = MMOItems.plugin.getMMOItem(type, nbtItem.getString("MMOITEMS_ITEM_ID"));
        if (mmoItem == null) {
            return 0.0;
        }

        ItemStat<?, ?> itemStat = itemStatsCache.get(stat.toUpperCase(Locale.ROOT));
        if (itemStat == null || !mmoItem.hasData(itemStat)) {
            return 0.0;
        }

        Object statData = mmoItem.getData(itemStat);
        return statData instanceof Number ? ((Number) statData).doubleValue() : 0.0;
    }

    private ItemStack getItem(Player player, EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> player.getInventory().getHelmet();
            case CHEST -> player.getInventory().getChestplate();
            case LEGS -> player.getInventory().getLeggings();
            case FEET -> player.getInventory().getBoots();
            case HAND -> player.getInventory().getItemInMainHand();
            case OFF_HAND -> player.getInventory().getItemInOffHand();
            default -> null;
        };
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || !player.isOnline()) {
            return null;
        }

        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer == null) {
            return null;
        }

        EquipmentSlot slot = null;
        String statName = null;

        if (params.startsWith("head_")) {
            slot = EquipmentSlot.HEAD;
            statName = params.substring(5);
        } else if (params.startsWith("chest_")) {
            slot = EquipmentSlot.CHEST;
            statName = params.substring(6);
        } else if (params.startsWith("legs_")) {
            slot = EquipmentSlot.LEGS;
            statName = params.substring(5);
        } else if (params.startsWith("hand_")) {
            slot = EquipmentSlot.HAND;
            statName = params.substring(5);
        } else if (params.startsWith("offhand_")) {
            slot = EquipmentSlot.OFF_HAND;
            statName = params.substring(8);
        } else if (params.startsWith("feet_")) {
            slot = EquipmentSlot.FEET;
            statName = params.substring(5);
        }

        if (slot != null) {
            return getMMOItemCheckStat(onlinePlayer, slot, statName.toUpperCase(Locale.ROOT));
        }

        return "Invalid Placeholder";
    }
}
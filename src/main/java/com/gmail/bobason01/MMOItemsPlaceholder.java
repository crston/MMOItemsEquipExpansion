package com.gmail.bobason01;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.inventory.MMOInventory;
import net.Indyuce.inventory.inventory.CustomInventory;
import net.Indyuce.inventory.inventory.Inventory;
import net.Indyuce.inventory.inventory.slot.CustomSlot;
import net.Indyuce.inventory.player.CustomInventoryData;
import net.Indyuce.inventory.player.PlayerData;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MMOItemsPlaceholder extends PlaceholderExpansion {

    private static final String ZERO = "0.0";
    private final Map<String, ItemStat<?, ?>> itemStatsCache = new HashMap<>();

    public MMOItemsPlaceholder(MMOItemsEquipExpansion plugin) {}

    @Override
    public @NotNull String getIdentifier() { return "mmoitemsequip"; }

    @Override
    public @NotNull String getAuthor() { return "crston"; }

    @Override
    public @NotNull String getVersion() { return "2.0"; }

    @Override
    public boolean persist() { return true; }

    @Override
    public boolean register() {
        if (super.register()) {
            cacheItemStats();
            return true;
        }
        return false;
    }

    private void cacheItemStats() {
        for (ItemStat<?, ?> stat : MMOItems.plugin.getStats().getAll()) {
            itemStatsCache.put(stat.getId().toUpperCase(Locale.ROOT), stat);
        }
    }

    private String extractStatFromItem(ItemStack itemStack, String statName) {
        if (itemStack == null || itemStack.getType().isAir()) return ZERO;

        final NBTItem nbt = NBTItem.get(itemStack);
        if (!nbt.hasType()) return ZERO;

        final String itemId = nbt.getString("MMOITEMS_ITEM_ID");
        if (itemId == null || itemId.isEmpty()) return ZERO;

        final var type = MMOItems.plugin.getTypes().get(nbt.getType());
        if (type == null) return ZERO;

        final MMOItem mmoItem = MMOItems.plugin.getMMOItem(type, itemId);
        if (mmoItem == null) return ZERO;

        final String key = statName.toUpperCase(Locale.ROOT);
        final ItemStat<?, ?> stat = itemStatsCache.get(key);
        if (stat == null || !mmoItem.hasData(stat)) return ZERO;

        final Object statData = mmoItem.getData(stat);
        return statData != null ? statData.toString() : ZERO;
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

    private String getBasicSlotStat(Player player, EquipmentSlot slot, String statName) {
        return extractStatFromItem(getItem(player, slot), statName);
    }

    private String getCustomSlotStat(Player player, String inventoryId, String slotIndexStr, String statName) {
        try {
            final int index = Integer.parseInt(slotIndexStr);
            final PlayerData data = MMOInventory.plugin.getDataManager().get(player);
            final CustomInventory custom = MMOInventory.plugin.getInventoryManager().getCustom(inventoryId);
            if (custom == null) return ZERO;

            final CustomInventoryData customData = data.getCustom(custom);
            if (customData == null) return ZERO;

            final Inventory inventory = customData.getInventory();
            final CustomSlot slot = inventory.getSlot(index);
            if (slot == null) return ZERO;

            return extractStatFromItem(customData.getItem(slot), statName);
        } catch (Exception ignored) {
            return ZERO;
        }
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (!offlinePlayer.isOnline()) return null;

        final Player player = offlinePlayer.getPlayer();
        if (player == null) return null;

        // 기본 슬롯
        if (params.startsWith("head_")) return getBasicSlotStat(player, EquipmentSlot.HEAD, params.substring(5));
        if (params.startsWith("chest_")) return getBasicSlotStat(player, EquipmentSlot.CHEST, params.substring(6));
        if (params.startsWith("legs_")) return getBasicSlotStat(player, EquipmentSlot.LEGS, params.substring(5));
        if (params.startsWith("feet_")) return getBasicSlotStat(player, EquipmentSlot.FEET, params.substring(5));
        if (params.startsWith("hand_")) return getBasicSlotStat(player, EquipmentSlot.HAND, params.substring(5));
        if (params.startsWith("offhand_")) return getBasicSlotStat(player, EquipmentSlot.OFF_HAND, params.substring(8));

        // 커스텀 슬롯
        final int colon = params.indexOf(':');
        final int underscore = params.indexOf('_');
        if (colon > 0 && underscore > colon) {
            String inventoryId = params.substring(0, colon);
            String slotIndex = params.substring(colon + 1, underscore);
            String stat = params.substring(underscore + 1);
            return getCustomSlotStat(player, inventoryId, slotIndex, stat);
        }

        return "Invalid Placeholder";
    }
}

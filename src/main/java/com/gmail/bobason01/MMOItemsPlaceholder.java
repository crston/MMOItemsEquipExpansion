package com.gmail.bobason01;

import io.lumine.mythic.lib.api.item.NBTItem;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public final class MMOItemsPlaceholder extends PlaceholderExpansion {

    private static final String ZERO = "0";

    @Override
    public @NotNull String getIdentifier() { return "mmoitemsequip"; }
    @Override
    public @NotNull String getAuthor() { return "crston"; }
    @Override
    public @NotNull String getVersion() { return "5.0"; }
    @Override
    public boolean persist() { return true; }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (!offlinePlayer.isOnline()) return ZERO;
        Player player = offlinePlayer.getPlayer();
        if (player == null || params.isEmpty()) return ZERO;

        String p = params.toLowerCase(Locale.ROOT);
        if (fast(p, "hand_") || fast(p, "mainhand_"))
            return basic(player, EquipmentSlot.HAND, subAfter(p));
        if (fast(p, "offhand_") || fast(p, "off_hand_"))
            return basic(player, EquipmentSlot.OFF_HAND, subAfter(p));
        if (fast(p, "head_") || fast(p, "helmet_"))
            return basic(player, EquipmentSlot.HEAD, subAfter(p));
        if (fast(p, "chest_") || fast(p, "chestplate_"))
            return basic(player, EquipmentSlot.CHEST, subAfter(p));
        if (fast(p, "legs_") || fast(p, "leggings_"))
            return basic(player, EquipmentSlot.LEGS, subAfter(p));
        if (fast(p, "feet_") || fast(p, "boots_"))
            return basic(player, EquipmentSlot.FEET, subAfter(p));
        return ZERO;
    }

    private String basic(Player p, EquipmentSlot slot, String statId) {
        ItemStack it = switch (slot) {
            case HAND -> p.getInventory().getItemInMainHand();
            case OFF_HAND -> p.getInventory().getItemInOffHand();
            case HEAD -> p.getInventory().getHelmet();
            case CHEST -> p.getInventory().getChestplate();
            case LEGS -> p.getInventory().getLeggings();
            case FEET -> p.getInventory().getBoots();
            default -> null;
        };
        return statOf(it, statId);
    }

    private String statOf(ItemStack item, String statId) {
        if (item == null || item.getType().isAir()) return ZERO;
        NBTItem nbt = NBTItem.get(item);
        String s = statId.toLowerCase(Locale.ROOT);

        // ---- PublicBukkitValues ----
        String pub = nbt.getString("PublicBukkitValues");
        if (pub != null && !pub.isEmpty()) {
            if (s.equals("durability")) {
                double val = parse(pub, "mmoitemsdurability:curr");
                if (val > 0) return strip(val);
            }
            if (s.equals("max_durability")) {
                double val = parse(pub, "mmoitemsdurability:max");
                if (val > 0) return strip(val);
            }
        }

        // ---- MMOItems legacy keys ----
        if (s.equals("durability")) {
            double d = nbt.getDouble("MMOITEMS_DURABILITY");
            if (d > 0) return strip(d);
            d = nbt.getDouble("MMOITEMS_ITEM_DAMAGE");
            if (d > 0) return strip(d);
            d = nbt.getDouble("MMOITEMS_ITEM_DURABILITY");
            if (d > 0) return strip(d);
        }
        if (s.equals("max_durability")) {
            double d = nbt.getDouble("MMOITEMS_MAX_ITEM_DAMAGE");
            if (d > 0) return strip(d);
            d = nbt.getDouble("MMOITEMS_MAX_ITEM_DURABILITY");
            if (d > 0) return strip(d);
        }

        // ---- Vanilla fallback ----
        if (s.equals("max_durability")) {
            int vanilla = item.getType().getMaxDurability();
            if (vanilla > 0) return Integer.toString(vanilla);
        }

        return ZERO;
    }

    private static double parse(String json, String key) {
        int i = json.indexOf(key);
        if (i < 0) return 0;
        int colon = json.indexOf(':', i + key.length());
        if (colon < 0) return 0;
        int end = json.indexOf(',', colon);
        if (end < 0) end = json.indexOf('}', colon);
        if (end < 0) return 0;
        try {
            String raw = json.substring(colon + 1, end).replaceAll("[^0-9.\\-]", "");
            return Double.parseDouble(raw);
        } catch (Exception e) {
            return 0;
        }
    }

    private static String strip(double d) {
        if (d == 0) return ZERO;
        String s = Double.toString(d);
        return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
    }

    private static boolean fast(String s, String prefix) {
        int len = prefix.length();
        if (s.length() < len) return false;
        for (int i = 0; i < len; i++)
            if (s.charAt(i) != prefix.charAt(i)) return false;
        return true;
    }

    private static String subAfter(String s) {
        int i = s.indexOf('_');
        return (i > 0 && i < s.length() - 1) ? s.substring(i + 1) : "";
    }
}

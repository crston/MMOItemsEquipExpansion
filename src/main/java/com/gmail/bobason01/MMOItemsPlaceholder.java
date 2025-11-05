package com.gmail.bobason01;

import io.lumine.mythic.lib.api.item.NBTItem;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class MMOItemsPlaceholder extends PlaceholderExpansion implements Listener {

    private static final String ZERO = "0";
    private final JavaPlugin plugin;
    private final NamespacedKey currentKey;
    private final NamespacedKey maxKey;

    public MMOItemsPlaceholder(JavaPlugin plugin) {
        this.plugin = plugin;
        this.currentKey = new NamespacedKey(plugin, "current_durability");
        this.maxKey = new NamespacedKey(plugin, "max_durability");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public @NotNull String getIdentifier() { return "durability"; }

    @Override
    public @NotNull String getAuthor() { return "crston"; }

    @Override
    public @NotNull String getVersion() { return "1.0"; }

    @Override
    public boolean persist() { return true; }

    // ====================================================================
    // 🧠 이벤트 리스너: 아이템 손상 시 현재 durability 갱신
    // ====================================================================
    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType().isAir()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer data = meta.getPersistentDataContainer();

        // 1️⃣ MMOITEMS 최대 내구도 확인
        NBTItem nbt = NBTItem.get(item);
        double max = nbt.hasTag("MMOITEMS_MAX_ITEM_DAMAGE")
                ? nbt.getDouble("MMOITEMS_MAX_ITEM_DAMAGE")
                : item.getType().getMaxDurability();

        data.set(maxKey, PersistentDataType.DOUBLE, max);

        // 2️⃣ 현재 내구도 추정
        double current;
        if (nbt.hasTag("MMOITEMS_DURABILITY")) {
            current = nbt.getDouble("MMOITEMS_DURABILITY");
        } else if (meta instanceof Damageable damageable) {
            current = Math.max(0, max - damageable.getDamage());
        } else {
            current = max;
        }

        // 3️⃣ 손상된 만큼 감소
        current = Math.max(0, current - event.getDamage());
        data.set(currentKey, PersistentDataType.DOUBLE, current);

        item.setItemMeta(meta);
    }

    // ====================================================================
    // ⚙️ PDC 및 NBT에서 내구도 읽기
    // ====================================================================
    private String getDurability(ItemStack item, String mode) {
        if (item == null || item.getType().isAir()) return ZERO;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return ZERO;

        PersistentDataContainer data = meta.getPersistentDataContainer();

        double max = 0;
        double current = -1;

        // PDC에 저장된 값이 있으면 우선 사용
        if (data.has(maxKey, PersistentDataType.DOUBLE))
            max = data.get(maxKey, PersistentDataType.DOUBLE);
        if (data.has(currentKey, PersistentDataType.DOUBLE))
            current = data.get(currentKey, PersistentDataType.DOUBLE);

        // 없으면 NBT 기반 fallback
        if (max <= 0) {
            NBTItem nbt = NBTItem.get(item);
            if (nbt.hasTag("MMOITEMS_MAX_ITEM_DAMAGE"))
                max = nbt.getDouble("MMOITEMS_MAX_ITEM_DAMAGE");
            else
                max = item.getType().getMaxDurability();
        }
        if (current < 0) {
            if (meta instanceof Damageable damageable)
                current = Math.max(0, max - damageable.getDamage());
            else
                current = max;
        }

        if (max <= 0) return ZERO;

        // 비율 계산
        switch (mode) {
            case "durability":
                return String.valueOf((int) current);
            case "max":
                return String.valueOf((int) max);
            case "damage":
                return String.valueOf((int) (max - current));
            case "percent":
                return String.format(Locale.US, "%.1f", (current / max) * 100.0);
            default:
                return ZERO;
        }
    }

    private static ItemStack getItem(Player player, EquipmentSlot slot) {
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
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (!offlinePlayer.isOnline()) return null;
        Player player = offlinePlayer.getPlayer();
        if (player == null) return null;

        String lower = params.toLowerCase(Locale.ROOT);
        EquipmentSlot slot = null;
        String mode = "durability";

        if (lower.contains("_max_durability")) mode = "max";
        else if (lower.contains("_damage")) mode = "damage";
        else if (lower.contains("_percent")) mode = "percent";

        String prefix = lower
                .replace("_max_durability", "")
                .replace("_durability", "")
                .replace("_damage", "")
                .replace("_percent", "");

        switch (prefix) {
            case "head" -> slot = EquipmentSlot.HEAD;
            case "chest" -> slot = EquipmentSlot.CHEST;
            case "legs" -> slot = EquipmentSlot.LEGS;
            case "feet" -> slot = EquipmentSlot.FEET;
            case "hand" -> slot = EquipmentSlot.HAND;
            case "offhand" -> slot = EquipmentSlot.OFF_HAND;
        }

        if (slot == null) return "Invalid Slot";
        return getDurability(getItem(player, slot), mode);
    }
}

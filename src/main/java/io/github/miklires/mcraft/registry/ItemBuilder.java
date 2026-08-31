package io.github.miklires.mcraft.registry;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import io.github.miklires.mcraft.model.CustomItem;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static NamespacedKey idKey;
    private static NamespacedKey versionKey;

    public static void init(Plugin plugin) {
        idKey = new NamespacedKey(plugin, "item_id");
        versionKey = new NamespacedKey(plugin, "item_version");
    }

    public static NamespacedKey getIdKey() {
        return idKey;
    }

    public static ItemStack build(CustomItem ci, int amount) {
        ItemStack stack = new ItemStack(ci.getMaterial(), Math.max(1, amount));
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;

        if (ci.getDisplayName() != null && !ci.getDisplayName().isBlank()) {
            meta.displayName(MM.deserialize(ci.getDisplayName())
                    .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        }
        if (!ci.getLore().isEmpty()) {
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            for (String line : ci.getLore()) {
                lore.add(MM.deserialize(line)
                        .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
            }
            meta.lore(lore);
        }
        for (var e : ci.getEnchantments().entrySet()) {
            Enchantment ench = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(e.getKey()));
            if (ench != null) meta.addEnchant(ench, e.getValue(), true);
        }
        if (ci.getItemModel() != null && !ci.getItemModel().isBlank()) {
            NamespacedKey model = NamespacedKey.fromString(ci.getItemModel());
            if (model != null) meta.setItemModel(model);
        }
        meta.setUnbreakable(ci.isUnbreakable());
        for (String flagName : ci.getItemFlags()) {
            try { meta.addItemFlags(ItemFlag.valueOf(flagName.toUpperCase(java.util.Locale.ROOT))); }
            catch (IllegalArgumentException ignored) {}
        }
        meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, ci.getId());
        meta.getPersistentDataContainer().set(versionKey, PersistentDataType.INTEGER, ci.getVersion());

        stack.setItemMeta(meta);
        return stack;
    }

    public static String readCustomId(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return null;
        ItemMeta meta = stack.getItemMeta();
        return meta.getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
    }

    public static int readVersion(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return 0;
        Integer value = stack.getItemMeta().getPersistentDataContainer().get(versionKey, PersistentDataType.INTEGER);
        return value == null ? 0 : value;
    }
}


package io.github.miklires.mcraft.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiButton {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static NamespacedKey actionKey;
    private static NamespacedKey payloadKey;

    public static void init(Plugin plugin) {
        actionKey = new NamespacedKey(plugin, "gui_action");
        payloadKey = new NamespacedKey(plugin, "gui_payload");
    }

    public static NamespacedKey getActionKey() { return actionKey; }
    public static NamespacedKey getPayloadKey() { return payloadKey; }

    public static ItemStack create(Material material, String action, String payload, String name, String... lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;

        meta.displayName(MM.deserialize(name)
                .decoration(TextDecoration.ITALIC, false));

        if (lore.length > 0) {
            List<Component> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(MM.deserialize(line).decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(loreList);
        }

        meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action);
        if (payload != null) {
            meta.getPersistentDataContainer().set(payloadKey, PersistentDataType.STRING, payload);
        }

        stack.setItemMeta(meta);
        return stack;
    }

    public static ItemStack overlay(ItemStack base, String action, String payload, String name, List<String> extraLore) {
        ItemStack stack = base.clone();
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;
        if (name != null) {
            meta.displayName(MM.deserialize(name).decoration(TextDecoration.ITALIC, false));
        }
        if (extraLore != null && !extraLore.isEmpty()) {
            List<Component> existing = meta.lore();
            List<Component> result = existing != null ? new ArrayList<>(existing) : new ArrayList<>();
            for (String line : extraLore) {
                result.add(MM.deserialize(line).decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(result);
        }
        meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action);
        if (payload != null) {
            meta.getPersistentDataContainer().set(payloadKey, PersistentDataType.STRING, payload);
        }
        stack.setItemMeta(meta);
        return stack;
    }

    public static String readAction(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return null;
        return stack.getItemMeta().getPersistentDataContainer().get(actionKey, PersistentDataType.STRING);
    }

    public static String readPayload(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return null;
        return stack.getItemMeta().getPersistentDataContainer().get(payloadKey, PersistentDataType.STRING);
    }

    public static ItemStack filler() {
        return create(Material.GRAY_STAINED_GLASS_PANE, GuiAction.NOOP, null, " ");
    }
}


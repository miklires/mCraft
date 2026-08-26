package io.github.miklires.mcraft.gui;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import io.github.miklires.mcraft.MCraft;
import io.github.miklires.mcraft.model.CustomItem;

import java.util.ArrayList;
import java.util.List;

public class ItemsListGui {

    public static final int PAGE_SIZE = 28;

    public static void open(MCraft plugin, Player player, int page) {
        List<CustomItem> all = new ArrayList<>(plugin.getItemRegistry().all());
        int totalPages = Math.max(1, (int) Math.ceil(all.size() / (double) PAGE_SIZE));
        page = Math.max(0, Math.min(page, totalPages - 1));

        var title = MiniMessage.miniMessage().deserialize(
                "<dark_green>Предметы</dark_green> <gray>(" + (page + 1) + "/" + totalPages + ")");
        Inventory inv = Bukkit.createInventory(new GuiHolder(GuiHolder.GuiScreen.ITEMS_LIST), 54, title);

        for (int i = 0; i < 9; i++) inv.setItem(i, GuiButton.filler());
        for (int i = 45; i < 54; i++) inv.setItem(i, GuiButton.filler());

        inv.setItem(0, GuiButton.create(Material.ARROW, GuiAction.BACK, null,
                "<yellow>← Назад"));

        int start = page * PAGE_SIZE;
        for (int i = 0; i < PAGE_SIZE && start + i < all.size(); i++) {
            CustomItem ci = all.get(start + i);
            int slot = 9 + (i / 7) * 9 + 1 + (i % 7);
            inv.setItem(slot, buildItemEntry(ci));
        }

        if (page > 0) {
            inv.setItem(45, GuiButton.create(Material.ARROW, GuiAction.PAGE_PREV, String.valueOf(page - 1),
                    "<yellow>← Предыдущая"));
        }
        if (page < totalPages - 1) {
            inv.setItem(53, GuiButton.create(Material.ARROW, GuiAction.PAGE_NEXT, String.valueOf(page + 1),
                    "<yellow>Следующая →"));
        }

        player.openInventory(inv);
    }

    private static org.bukkit.inventory.ItemStack buildItemEntry(CustomItem ci) {
        org.bukkit.inventory.ItemStack preview = ci.buildItemStack(1);
        List<String> extra = new ArrayList<>();
        extra.add("");
        extra.add("<dark_gray>ID: <gray>" + ci.getId());
        extra.add("<dark_gray>Материал: <gray>" + ci.getMaterial().name().toLowerCase());
        extra.add("");
        extra.add("<yellow>ЛКМ — просмотр");
        extra.add("<yellow>ПКМ — выдать 1 шт");
        extra.add("<yellow>Shift+ПКМ — выдать 64 шт");
        return GuiButton.overlay(preview, GuiAction.VIEW_ITEM, ci.getId(), null, extra);
    }
}


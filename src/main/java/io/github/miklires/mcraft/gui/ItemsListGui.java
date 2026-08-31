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
                plugin.getMessageUtil().get("gui.items-title", "page", String.valueOf(page + 1), "pages", String.valueOf(totalPages)));
        GuiHolder holder = new GuiHolder(GuiHolder.GuiScreen.ITEMS_LIST);
        Inventory inv = Bukkit.createInventory(holder, 54, title);
        holder.bind(inv);

        for (int i = 0; i < 9; i++) inv.setItem(i, GuiButton.filler());
        for (int i = 45; i < 54; i++) inv.setItem(i, GuiButton.filler());

        inv.setItem(0, GuiButton.create(Material.ARROW, GuiAction.BACK, null,
                plugin.getMessageUtil().get("gui.back")));

        int start = page * PAGE_SIZE;
        for (int i = 0; i < PAGE_SIZE && start + i < all.size(); i++) {
            CustomItem ci = all.get(start + i);
            int slot = 9 + (i / 7) * 9 + 1 + (i % 7);
            inv.setItem(slot, buildItemEntry(plugin, ci));
        }

        if (page > 0) {
            inv.setItem(45, GuiButton.create(Material.ARROW, GuiAction.PAGE_PREV, String.valueOf(page - 1),
                    plugin.getMessageUtil().get("gui.previous")));
        }
        if (page < totalPages - 1) {
            inv.setItem(53, GuiButton.create(Material.ARROW, GuiAction.PAGE_NEXT, String.valueOf(page + 1),
                    plugin.getMessageUtil().get("gui.next")));
        }

        player.openInventory(inv);
    }

    private static org.bukkit.inventory.ItemStack buildItemEntry(MCraft plugin, CustomItem ci) {
        org.bukkit.inventory.ItemStack preview = ci.buildItemStack(1);
        List<String> extra = new ArrayList<>();
        extra.add("");
        extra.add("<dark_gray>ID: <gray>" + ci.getId());
        extra.add(plugin.getMessageUtil().get("gui.material", "material", ci.getMaterial().name().toLowerCase()));
        extra.add("");
        extra.add(plugin.getMessageUtil().get("gui.left-view"));
        extra.add(plugin.getMessageUtil().get("gui.right-give-one"));
        extra.add(plugin.getMessageUtil().get("gui.shift-right-give-stack"));
        return GuiButton.overlay(preview, GuiAction.VIEW_ITEM, ci.getId(), null, extra);
    }
}


package io.github.miklires.mcraft.gui;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import io.github.miklires.mcraft.MCraft;
import io.github.miklires.mcraft.model.CustomItem;

public class ItemViewGui {

    public static void open(MCraft plugin, Player player, String itemId) {
        CustomItem ci = plugin.getItemRegistry().get(itemId);
        if (ci == null) {
            ItemsListGui.open(plugin, player, 0);
            return;
        }

        var title = MiniMessage.miniMessage().deserialize("<dark_green>Предмет:</dark_green> <white>" + ci.getId());
        Inventory inv = Bukkit.createInventory(new GuiHolder(GuiHolder.GuiScreen.ITEM_VIEW), 27, title);

        for (int i = 0; i < 27; i++) inv.setItem(i, GuiButton.filler());

        inv.setItem(0, GuiButton.create(Material.ARROW, GuiAction.BACK, null, "<yellow>← Назад"));

        inv.setItem(13, ci.buildItemStack(1));

        inv.setItem(11, GuiButton.create(Material.LIME_DYE, GuiAction.GIVE_ITEM, ci.getId() + ":1",
                "<green>Выдать 1 шт"));
        inv.setItem(15, GuiButton.create(Material.LIME_CONCRETE, GuiAction.GIVE_ITEM, ci.getId() + ":64",
                "<green>Выдать 64 шт"));

        player.openInventory(inv);
    }
}


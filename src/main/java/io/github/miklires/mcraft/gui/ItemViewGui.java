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

        var title = MiniMessage.miniMessage().deserialize(plugin.getMessageUtil().get("gui.item-title", "id", ci.getId()));
        GuiHolder holder = new GuiHolder(GuiHolder.GuiScreen.ITEM_VIEW);
        Inventory inv = Bukkit.createInventory(holder, 27, title);
        holder.bind(inv);

        for (int i = 0; i < 27; i++) inv.setItem(i, GuiButton.filler());

        inv.setItem(0, GuiButton.create(Material.ARROW, GuiAction.BACK, null, plugin.getMessageUtil().get("gui.back")));

        inv.setItem(13, ci.buildItemStack(1));

        inv.setItem(11, GuiButton.create(Material.LIME_DYE, GuiAction.GIVE_ITEM, ci.getId() + ":1",
                plugin.getMessageUtil().get("gui.give-one")));
        inv.setItem(15, GuiButton.create(Material.LIME_CONCRETE, GuiAction.GIVE_ITEM, ci.getId() + ":64",
                plugin.getMessageUtil().get("gui.give-stack")));

        player.openInventory(inv);
    }
}


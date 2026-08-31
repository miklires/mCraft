package io.github.miklires.mcraft.gui;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import io.github.miklires.mcraft.MCraft;

public class MainMenuGui {

    public static void open(MCraft plugin, Player player) {
        var title = MiniMessage.miniMessage().deserialize(plugin.getMessageUtil().get("gui.main-title"));
        GuiHolder holder = new GuiHolder(GuiHolder.GuiScreen.MAIN_MENU);
        Inventory inv = Bukkit.createInventory(holder, 27, title);
        holder.bind(inv);

        for (int i = 0; i < 27; i++) inv.setItem(i, GuiButton.filler());

        inv.setItem(11, GuiButton.create(
                Material.CHEST,
                GuiAction.OPEN_ITEMS,
                null,
                plugin.getMessageUtil().get("gui.items"),
                plugin.getMessageUtil().get("gui.total", "count", String.valueOf(plugin.getItemRegistry().all().size())),
                "",
                plugin.getMessageUtil().get("gui.click-open")
        ));

        inv.setItem(15, GuiButton.create(
                Material.CRAFTING_TABLE,
                GuiAction.OPEN_RECIPES,
                null,
                plugin.getMessageUtil().get("gui.recipes"),
                plugin.getMessageUtil().get("gui.total", "count", String.valueOf(plugin.getRecipeRegistry().all().size())),
                "",
                plugin.getMessageUtil().get("gui.click-open")
        ));

        player.openInventory(inv);
    }
}


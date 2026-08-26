package io.github.miklires.mcraft.gui;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import io.github.miklires.mcraft.MCraft;

public class MainMenuGui {

    public static void open(MCraft plugin, Player player) {
        var title = MiniMessage.miniMessage().deserialize("<dark_green><b>mCraft</b></dark_green>");
        Inventory inv = Bukkit.createInventory(new GuiHolder(GuiHolder.GuiScreen.MAIN_MENU), 27, title);

        for (int i = 0; i < 27; i++) inv.setItem(i, GuiButton.filler());

        inv.setItem(11, GuiButton.create(
                Material.CHEST,
                GuiAction.OPEN_ITEMS,
                null,
                "<gold><b>Кастомные предметы</b></gold>",
                "<gray>Всего: <white>" + plugin.getItemRegistry().all().size(),
                "",
                "<yellow>Клик чтобы открыть"
        ));

        inv.setItem(15, GuiButton.create(
                Material.CRAFTING_TABLE,
                GuiAction.OPEN_RECIPES,
                null,
                "<gold><b>Рецепты</b></gold>",
                "<gray>Всего: <white>" + plugin.getRecipeRegistry().all().size(),
                "",
                "<yellow>Клик чтобы открыть"
        ));

        player.openInventory(inv);
    }
}


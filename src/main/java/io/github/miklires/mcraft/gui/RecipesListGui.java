package io.github.miklires.mcraft.gui;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import io.github.miklires.mcraft.MCraft;
import io.github.miklires.mcraft.model.CustomItem;
import io.github.miklires.mcraft.model.CustomRecipe;

import java.util.ArrayList;
import java.util.List;

public class RecipesListGui {

    public static final int PAGE_SIZE = 28;

    public static void open(MCraft plugin, Player player, int page) {
        List<CustomRecipe> all = new ArrayList<>(plugin.getRecipeRegistry().all());
        int totalPages = Math.max(1, (int) Math.ceil(all.size() / (double) PAGE_SIZE));
        page = Math.max(0, Math.min(page, totalPages - 1));

        var title = MiniMessage.miniMessage().deserialize(
                "<dark_green>Рецепты</dark_green> <gray>(" + (page + 1) + "/" + totalPages + ")");
        Inventory inv = Bukkit.createInventory(new GuiHolder(GuiHolder.GuiScreen.RECIPES_LIST), 54, title);

        for (int i = 0; i < 9; i++) inv.setItem(i, GuiButton.filler());
        for (int i = 45; i < 54; i++) inv.setItem(i, GuiButton.filler());

        inv.setItem(0, GuiButton.create(Material.ARROW, GuiAction.BACK, null,
                "<yellow>← Назад"));
        inv.setItem(4, GuiButton.create(Material.EMERALD, GuiAction.RECIPE_CREATE, null,
                "<green><b>+ Создать рецепт</b>"));

        int start = page * PAGE_SIZE;
        for (int i = 0; i < PAGE_SIZE && start + i < all.size(); i++) {
            CustomRecipe r = all.get(start + i);
            int slot = 9 + (i / 7) * 9 + 1 + (i % 7);
            inv.setItem(slot, buildRecipeEntry(plugin, r));
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

    private static ItemStack buildRecipeEntry(MCraft plugin, CustomRecipe r) {
        ItemStack preview;
        if (r.isResultCustom()) {
            CustomItem ci = plugin.getItemRegistry().get(r.getResultRefCustomId());
            preview = ci != null ? ci.buildItemStack(r.getResultAmount()) : new ItemStack(Material.BARRIER);
        } else {
            preview = new ItemStack(r.getResultVanilla() != null ? r.getResultVanilla() : Material.BARRIER,
                    r.getResultAmount());
        }

        List<String> extra = new ArrayList<>();
        extra.add("");
        extra.add("<dark_gray>ID: <gray>" + r.getId());
        extra.add("<dark_gray>Тип: <gray>" + (r.getType().name().equals("SHAPED") ? "С формой" : "Без формы"));
        extra.add("");
        extra.add("<yellow>ЛКМ — просмотр");
        return GuiButton.overlay(preview, GuiAction.VIEW_RECIPE, r.getId(), null, extra);
    }
}


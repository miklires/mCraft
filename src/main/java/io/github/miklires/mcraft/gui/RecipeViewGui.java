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
import io.github.miklires.mcraft.model.RecipeIngredient;
import io.github.miklires.mcraft.model.RecipeType;

import java.util.ArrayList;
import java.util.List;

public class RecipeViewGui {

    private static final int[] CRAFT_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private static final int RESULT_SLOT = 24;

    public static void open(MCraft plugin, Player player, String recipeId) {
        CustomRecipe r = plugin.getRecipeRegistry().get(recipeId);
        if (r == null) {
            RecipesListGui.open(plugin, player, 0);
            return;
        }

        var title = MiniMessage.miniMessage().deserialize("<dark_green>Рецепт:</dark_green> <white>" + r.getId());
        Inventory inv = Bukkit.createInventory(new GuiHolder(GuiHolder.GuiScreen.RECIPE_VIEW), 45, title);

        for (int i = 0; i < 45; i++) inv.setItem(i, GuiButton.filler());
        inv.setItem(0, GuiButton.create(Material.ARROW, GuiAction.BACK, null, "<yellow>← Назад"));
        inv.setItem(7, GuiButton.create(Material.WRITABLE_BOOK, GuiAction.RECIPE_EDIT, r.getId(),
                "<yellow>Редактировать"));
        inv.setItem(8, GuiButton.create(Material.PAPER, GuiAction.NOOP, null,
                "<gold>Информация",
                "<dark_gray>ID: <gray>" + r.getId(),
                "<dark_gray>Тип: <gray>" + (r.getType() == RecipeType.SHAPED ? "С формой" : "Без формы")
        ));
        inv.setItem(44, GuiButton.create(Material.RED_CONCRETE, GuiAction.RECIPE_DELETE, r.getId(),
                "<red><b>Удалить рецепт</b>",
                "",
                "<gray>Клик чтобы подтвердить"));

        if (r.getType() == RecipeType.SHAPED) {
            for (int i = 0; i < 9; i++) {
                RecipeIngredient ing = r.getSlot(i);
                inv.setItem(CRAFT_SLOTS[i], renderIngredient(plugin, ing));
            }
        } else {
            int n = Math.min(9, r.getIngredients().size());
            for (int i = 0; i < n; i++) {
                inv.setItem(CRAFT_SLOTS[i], renderIngredient(plugin, r.getIngredients().get(i)));
            }
            for (int i = n; i < 9; i++) inv.setItem(CRAFT_SLOTS[i], GuiButton.filler());
        }

        inv.setItem(22, GuiButton.create(Material.ARROW, GuiAction.NOOP, null, "<yellow>→"));
        inv.setItem(RESULT_SLOT, buildResult(plugin, r));

        player.openInventory(inv);
    }

    private static ItemStack renderIngredient(MCraft plugin, RecipeIngredient ing) {
        if (ing == null) return new ItemStack(Material.AIR);
        if (ing.isCustom()) {
            CustomItem ci = plugin.getItemRegistry().get(ing.getCustomId());
            if (ci != null) return ci.buildItemStack(1);
            return GuiButton.create(Material.BARRIER, GuiAction.NOOP, null,
                    "<red>Предмет не найден",
                    "<gray>ID: " + ing.getCustomId());
        }
        ItemStack s = new ItemStack(ing.getMaterial());
        List<String> lore = new ArrayList<>();
        lore.add("<dark_gray>Ванильный");
        return GuiButton.overlay(s, GuiAction.NOOP, null, null, lore);
    }

    private static ItemStack buildResult(MCraft plugin, CustomRecipe r) {
        if (r.isResultCustom()) {
            CustomItem ci = plugin.getItemRegistry().get(r.getResultRefCustomId());
            if (ci != null) return ci.buildItemStack(r.getResultAmount());
            return GuiButton.create(Material.BARRIER, GuiAction.NOOP, null, "<red>Результат не найден");
        }
        if (r.getResultVanilla() != null) {
            return new ItemStack(r.getResultVanilla(), r.getResultAmount());
        }
        return new ItemStack(Material.BARRIER);
    }
}


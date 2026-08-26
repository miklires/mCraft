package io.github.miklires.mcraft.gui;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import io.github.miklires.mcraft.MCraft;
import io.github.miklires.mcraft.model.CustomRecipe;
import io.github.miklires.mcraft.model.RecipeIngredient;
import io.github.miklires.mcraft.model.RecipeType;
import io.github.miklires.mcraft.registry.ItemBuilder;

import java.util.HashSet;
import java.util.Set;

public class RecipeEditGui {

    public static final int[] CRAFT_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    public static final int RESULT_SLOT = 24;
    public static final int TYPE_BUTTON_SLOT = 33;
    public static final int PRIORITY_BUTTON_SLOT = 42;
    public static final int SAVE_BUTTON_SLOT = 44;
    public static final int CANCEL_BUTTON_SLOT = 36;

    public static void openNew(MCraft plugin, Player player) {
        open(plugin, player, null);
    }

    public static void open(MCraft plugin, Player player, String editRecipeId) {
        Set<Integer> editable = new HashSet<>();
        for (int s : CRAFT_SLOTS) editable.add(s);
        editable.add(RESULT_SLOT);

        GuiHolder holder = new GuiHolder(GuiHolder.GuiScreen.RECIPE_EDIT, editable);
        holder.setEditingRecipeId(editRecipeId);

        String titleText = editRecipeId == null
                ? "<dark_green>Новый рецепт</dark_green>"
                : "<dark_green>Рецепт:</dark_green> <white>" + editRecipeId;
        var title = MiniMessage.miniMessage().deserialize(titleText);
        Inventory inv = Bukkit.createInventory(holder, 45, title);

        for (int i = 0; i < 45; i++) inv.setItem(i, GuiButton.filler());

        for (int s : CRAFT_SLOTS) inv.setItem(s, null);
        inv.setItem(RESULT_SLOT, null);

        inv.setItem(22, GuiButton.create(Material.ARROW, GuiAction.NOOP, null, "<yellow>→"));

        inv.setItem(CANCEL_BUTTON_SLOT, GuiButton.create(Material.BARRIER, GuiAction.BACK, null,
                "<red>Отмена",
                "<gray>Изменения не будут сохранены"));

        CustomRecipe existing = editRecipeId != null ? plugin.getRecipeRegistry().get(editRecipeId) : null;
        RecipeType type = existing != null ? existing.getType() : RecipeType.SHAPED;
        boolean priority = existing != null && existing.isOverrideVanilla();

        if (existing != null) {
            prefill(plugin, inv, existing);
        }

        inv.setItem(TYPE_BUTTON_SLOT, buildTypeButton(type));
        inv.setItem(PRIORITY_BUTTON_SLOT, buildPriorityButton(priority));
        inv.setItem(SAVE_BUTTON_SLOT, GuiButton.create(Material.EMERALD_BLOCK, GuiAction.RECIPE_SAVE, null,
                "<green><b>Сохранить рецепт</b>"));

        player.openInventory(inv);
    }

    private static void prefill(MCraft plugin, Inventory inv, CustomRecipe r) {
        if (r.getType() == RecipeType.SHAPED) {
            for (int i = 0; i < 9; i++) {
                RecipeIngredient ing = r.getSlot(i);
                if (ing != null) inv.setItem(CRAFT_SLOTS[i], renderIngredient(plugin, ing));
            }
        } else {
            int n = Math.min(9, r.getIngredients().size());
            for (int i = 0; i < n; i++) {
                inv.setItem(CRAFT_SLOTS[i], renderIngredient(plugin, r.getIngredients().get(i)));
            }
        }

        if (r.isResultCustom()) {
            var ci = plugin.getItemRegistry().get(r.getResultRefCustomId());
            if (ci != null) inv.setItem(RESULT_SLOT, ci.buildItemStack(r.getResultAmount()));
        } else if (r.getResultVanilla() != null) {
            inv.setItem(RESULT_SLOT, new ItemStack(r.getResultVanilla(), r.getResultAmount()));
        }
    }

    private static ItemStack renderIngredient(MCraft plugin, RecipeIngredient ing) {
        if (ing == null) return null;
        if (ing.isCustom()) {
            var ci = plugin.getItemRegistry().get(ing.getCustomId());
            if (ci != null) return ci.buildItemStack(1);
            return new ItemStack(ing.getMaterial());
        }
        return new ItemStack(ing.getMaterial());
    }

    public static ItemStack buildTypeButton(RecipeType current) {
        String label = current == RecipeType.SHAPED ? "С формой" : "Без формы";
        String next = current == RecipeType.SHAPED ? "Без формы" : "С формой";
        Material icon = current == RecipeType.SHAPED ? Material.CRAFTING_TABLE : Material.CHEST;
        return GuiButton.create(icon, GuiAction.RECIPE_TOGGLE_TYPE, current.name(),
                "<yellow>Тип: <white>" + label,
                "",
                "<gray>Клик чтобы переключить на <white>" + next,
                "",
                "<dark_gray>С формой = позиция важна",
                "<dark_gray>Без формы = любое расположение");
    }

    public static RecipeType readType(Inventory inv) {
        ItemStack btn = inv.getItem(TYPE_BUTTON_SLOT);
        if (btn == null) return RecipeType.SHAPED;
        String payload = GuiButton.readPayload(btn);
        if (payload == null) return RecipeType.SHAPED;
        try {
            return RecipeType.valueOf(payload);
        } catch (IllegalArgumentException e) {
            return RecipeType.SHAPED;
        }
    }

    public static ItemStack buildPriorityButton(boolean override) {
        Material icon = override ? Material.LIME_DYE : Material.GRAY_DYE;
        String state = override ? "<green>Включён" : "<red>Выключен";
        String hint = override
                ? "<gray>При конфликте с ванильным — работает наш"
                : "<gray>При конфликте — работает ванильный (наш не сохранится)";
        return GuiButton.create(icon, GuiAction.RECIPE_TOGGLE_PRIORITY,
                String.valueOf(override),
                "<yellow>Приоритет над ванильным: " + state,
                "",
                hint,
                "",
                "<gray>Клик чтобы переключить");
    }

    public static boolean readPriority(Inventory inv) {
        ItemStack btn = inv.getItem(PRIORITY_BUTTON_SLOT);
        if (btn == null) return false;
        String payload = GuiButton.readPayload(btn);
        return "true".equalsIgnoreCase(payload);
    }

    public static RecipeIngredient buildIngredient(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return null;
        String customId = ItemBuilder.readCustomId(stack);
        if (customId != null) {
            return RecipeIngredient.custom(customId, stack.getType());
        }
        return RecipeIngredient.vanilla(stack.getType());
    }
}


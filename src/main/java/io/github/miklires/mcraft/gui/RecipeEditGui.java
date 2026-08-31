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
                ? plugin.getMessageUtil().get("gui.new-recipe-title")
                : plugin.getMessageUtil().get("gui.recipe-title", "id", editRecipeId);
        var title = MiniMessage.miniMessage().deserialize(titleText);
        Inventory inv = Bukkit.createInventory(holder, 45, title);
        holder.bind(inv);

        for (int i = 0; i < 45; i++) inv.setItem(i, GuiButton.filler());

        for (int s : CRAFT_SLOTS) inv.setItem(s, null);
        inv.setItem(RESULT_SLOT, null);

        inv.setItem(22, GuiButton.create(Material.ARROW, GuiAction.NOOP, null, "<yellow>→"));

        inv.setItem(CANCEL_BUTTON_SLOT, GuiButton.create(Material.BARRIER, GuiAction.BACK, null,
                plugin.getMessageUtil().get("gui.cancel"),
                plugin.getMessageUtil().get("gui.not-saved")));

        CustomRecipe existing = editRecipeId != null ? plugin.getRecipeRegistry().get(editRecipeId) : null;
        RecipeType type = existing != null ? existing.getType() : RecipeType.SHAPED;
        boolean priority = existing != null && existing.isOverrideVanilla();

        if (existing != null) {
            prefill(plugin, inv, existing);
        }

        inv.setItem(TYPE_BUTTON_SLOT, buildTypeButton(plugin, type));
        inv.setItem(PRIORITY_BUTTON_SLOT, buildPriorityButton(plugin, priority));
        inv.setItem(SAVE_BUTTON_SLOT, GuiButton.create(Material.EMERALD_BLOCK, GuiAction.RECIPE_SAVE, null,
                plugin.getMessageUtil().get("gui.save-recipe")));

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

    public static ItemStack buildTypeButton(MCraft plugin, RecipeType current) {
        String label = plugin.getMessageUtil().get(current == RecipeType.SHAPED ? "gui.shaped" : "gui.shapeless");
        String next = plugin.getMessageUtil().get(current == RecipeType.SHAPED ? "gui.shapeless" : "gui.shaped");
        Material icon = current == RecipeType.SHAPED ? Material.CRAFTING_TABLE : Material.CHEST;
        return GuiButton.create(icon, GuiAction.RECIPE_TOGGLE_TYPE, current.name(),
                plugin.getMessageUtil().get("gui.type", "type", label),
                "",
                plugin.getMessageUtil().get("gui.click-switch", "type", next),
                "",
                plugin.getMessageUtil().get("gui.shaped-help"),
                plugin.getMessageUtil().get("gui.shapeless-help"));
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

    public static ItemStack buildPriorityButton(MCraft plugin, boolean override) {
        Material icon = override ? Material.LIME_DYE : Material.GRAY_DYE;
        String state = plugin.getMessageUtil().get(override ? "gui.enabled" : "gui.disabled");
        String hint = override
                ? plugin.getMessageUtil().get("gui.priority-on-help")
                : plugin.getMessageUtil().get("gui.priority-off-help");
        return GuiButton.create(icon, GuiAction.RECIPE_TOGGLE_PRIORITY,
                String.valueOf(override),
                plugin.getMessageUtil().get("gui.priority", "state", state),
                "",
                hint,
                "",
                plugin.getMessageUtil().get("gui.click-toggle"));
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


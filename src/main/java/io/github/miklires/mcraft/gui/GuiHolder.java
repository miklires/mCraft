package io.github.miklires.mcraft.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class GuiHolder implements InventoryHolder {

    private final GuiScreen screen;
    private final Set<Integer> editableSlots;
    private String editingRecipeId;

    public GuiHolder(GuiScreen screen) {
        this(screen, Set.of());
    }

    public GuiHolder(GuiScreen screen, Set<Integer> editableSlots) {
        this.screen = screen;
        this.editableSlots = editableSlots;
    }

    public GuiScreen getScreen() {
        return screen;
    }

    public boolean isEditable(int slot) {
        return editableSlots.contains(slot);
    }

    public String getEditingRecipeId() {
        return editingRecipeId;
    }

    public void setEditingRecipeId(String editingRecipeId) {
        this.editingRecipeId = editingRecipeId;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        throw new UnsupportedOperationException("Inventory bound externally");
    }

    public enum GuiScreen {
        MAIN_MENU,
        ITEMS_LIST,
        ITEM_VIEW,
        RECIPES_LIST,
        RECIPE_VIEW,
        RECIPE_EDIT
    }
}


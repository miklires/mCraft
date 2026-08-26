package io.github.miklires.mcraft.listener;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import io.github.miklires.mcraft.MCraft;
import io.github.miklires.mcraft.model.CustomRecipe;
import io.github.miklires.mcraft.model.RecipeIngredient;
import io.github.miklires.mcraft.registry.ItemMatcher;

public class CraftingListener implements Listener {

    private final MCraft plugin;

    public CraftingListener(MCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();
        if (recipe == null) return;

        CraftingInventory inv = event.getInventory();

        if (recipe instanceof Keyed keyed
                && keyed.getKey().getNamespace().equals(plugin.getName().toLowerCase())) {
            CustomRecipe ours = plugin.getRecipeRegistry().get(keyed.getKey().getKey());
            if (ours == null) return;
            if (!validateIngredients(inv, ours, recipe)) {
                inv.setResult(null);
            }
            return;
        }

        for (CustomRecipe ours : plugin.getRecipeRegistry().candidates(inv.getMatrix())) {
            if (!ours.isOverrideVanilla()) continue;
            if (validateIngredients(inv, ours, ours.getType() == io.github.miklires.mcraft.model.RecipeType.SHAPED
                    ? newShapedDummy() : newShapelessDummy())) {
                inv.setResult(buildOurResult(ours));
                return;
            }
        }
    }

    private Recipe newShapedDummy() {
        return new ShapedRecipe(new NamespacedKey(plugin, "_dummy_shaped"),
                new ItemStack(org.bukkit.Material.AIR));
    }

    private Recipe newShapelessDummy() {
        return new ShapelessRecipe(new NamespacedKey(plugin, "_dummy_shapeless"),
                new ItemStack(org.bukkit.Material.AIR));
    }

    private ItemStack buildOurResult(CustomRecipe r) {
        if (r.isResultCustom()) {
            var ci = plugin.getItemRegistry().get(r.getResultRefCustomId());
            if (ci == null) return null;
            return ci.buildItemStack(r.getResultAmount());
        }
        if (r.getResultVanilla() == null) return null;
        return new ItemStack(r.getResultVanilla(), r.getResultAmount());
    }

    private boolean validateIngredients(CraftingInventory inv, CustomRecipe ours, Recipe recipe) {
        ItemStack[] matrix = inv.getMatrix();

        if (recipe instanceof ShapedRecipe) {
            return validateShaped(matrix, ours);
        }
        if (recipe instanceof ShapelessRecipe) {
            return validateShapeless(matrix, ours);
        }
        return true;
    }

    private boolean validateShaped(ItemStack[] matrix, CustomRecipe ours) {
        int gridSize = matrix.length == 4 ? 2 : 3;
        if (gridSize == 2) {
            for (int row = 0; row <= 1; row++) {
                for (int col = 0; col <= 1; col++) {
                    if (!shapedMatches2x2InRecipe(matrix, ours, row, col)) {
                        continue;
                    }
                    return true;
                }
            }
            return false;
        }
        for (int rowOffset = 0; rowOffset <= 3 - getRecipeRows(ours); rowOffset++) {
            for (int colOffset = 0; colOffset <= 3 - getRecipeCols(ours); colOffset++) {
                if (shapedMatchesAt(matrix, ours, rowOffset, colOffset)) return true;
            }
        }
        return false;
    }

    private int getRecipeRows(CustomRecipe r) {
        int rows = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (r.getSlot(row * 3 + col) != null) {
                    if (row + 1 > rows) rows = row + 1;
                }
            }
        }
        return rows == 0 ? 1 : rows;
    }

    private int getRecipeCols(CustomRecipe r) {
        int cols = 0;
        for (int col = 0; col < 3; col++) {
            for (int row = 0; row < 3; row++) {
                if (r.getSlot(row * 3 + col) != null) {
                    if (col + 1 > cols) cols = col + 1;
                }
            }
        }
        return cols == 0 ? 1 : cols;
    }

    private boolean shapedMatchesAt(ItemStack[] matrix, CustomRecipe r, int rowOffset, int colOffset) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                RecipeIngredient expected = r.getSlot(row * 3 + col);
                int matrixIdx = (row + rowOffset) * 3 + (col + colOffset);
                if (matrixIdx >= matrix.length) return false;
                ItemStack actual = matrix[matrixIdx];
                if (!ItemMatcher.matches(actual, expected)) return false;
            }
        }
        return true;
    }

    private boolean shapedMatches2x2InRecipe(ItemStack[] matrix, CustomRecipe r, int rowStart, int colStart) {
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                RecipeIngredient expected = r.getSlot((row + rowStart) * 3 + (col + colStart));
                ItemStack actual = matrix[row * 2 + col];
                if (!ItemMatcher.matches(actual, expected)) return false;
            }
        }
        return true;
    }

    private boolean validateShapeless(ItemStack[] matrix, CustomRecipe r) {
        java.util.List<RecipeIngredient> needed = new java.util.ArrayList<>(r.getIngredients());
        for (ItemStack stack : matrix) {
            if (stack == null || stack.getType().isAir()) continue;
            boolean matched = false;
            for (int i = 0; i < needed.size(); i++) {
                if (ItemMatcher.matches(stack, needed.get(i))) {
                    needed.remove(i);
                    matched = true;
                    break;
                }
            }
            if (!matched) return false;
        }
        return needed.isEmpty();
    }
}


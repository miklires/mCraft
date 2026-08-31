package io.github.miklires.mcraft.listener;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.entity.Player;
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
            Player player = event.getView().getPlayer() instanceof Player p ? p : null;
            if (!validateIngredients(inv, ours, recipe) || !canCraft(player, ours)) {
                inv.setResult(null);
            }
            return;
        }

        for (CustomRecipe ours : plugin.getRecipeRegistry().candidates(inv.getMatrix())) {
            if (!ours.isOverrideVanilla()) continue;
            Player player = event.getView().getPlayer() instanceof Player p ? p : null;
            if (canCraft(player, ours) && validateIngredients(inv, ours, ours.getType() == io.github.miklires.mcraft.model.RecipeType.SHAPED
                    ? newShapedDummy() : newShapelessDummy())) {
                inv.setResult(buildOurResult(ours));
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (!(event.getRecipe() instanceof Keyed keyed)
                || !keyed.getKey().getNamespace().equals(plugin.getName().toLowerCase(java.util.Locale.ROOT))) return;
        CustomRecipe recipe = plugin.getRecipeRegistry().get(keyed.getKey().getKey());
        Player player = event.getWhoClicked() instanceof Player p ? p : null;
        if (recipe == null || !canCraft(player, recipe)
                || !validateIngredients(event.getInventory(), recipe, event.getRecipe())) {
            event.setCancelled(true);
            event.getInventory().setResult(null);
            if (player != null) player.sendMessage(plugin.getMessageUtil().component("recipe.conditions-denied"));
        }
    }

    private boolean canCraft(Player player, CustomRecipe recipe) {
        if (player == null) return recipe.isAllowAutomation();
        if (recipe.getPermission() != null && !player.hasPermission(recipe.getPermission())) return false;
        if (player.getLevel() < recipe.getMinimumLevel()) return false;
        return recipe.getWorlds().isEmpty()
                || recipe.getWorlds().contains(player.getWorld().getName().toLowerCase(java.util.Locale.ROOT));
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
        int minRow = 3, minCol = 3, maxRow = -1, maxCol = -1;
        for (int i = 0; i < 9; i++) if (ours.getSlot(i) != null) {
            minRow = Math.min(minRow, i / 3); maxRow = Math.max(maxRow, i / 3);
            minCol = Math.min(minCol, i % 3); maxCol = Math.max(maxCol, i % 3);
        }
        if (maxRow < 0) return false;
        int height = maxRow - minRow + 1, width = maxCol - minCol + 1;
        if (height > gridSize || width > gridSize) return false;
        for (int rowOffset = 0; rowOffset <= gridSize - height; rowOffset++) {
            for (int colOffset = 0; colOffset <= gridSize - width; colOffset++) {
                if (shapedMatchesAt(matrix, gridSize, ours, minRow, minCol, height, width, rowOffset, colOffset)) return true;
            }
        }
        return false;
    }

    private boolean shapedMatchesAt(ItemStack[] matrix, int gridSize, CustomRecipe r,
                                    int minRow, int minCol, int height, int width,
                                    int rowOffset, int colOffset) {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                int rr = row - rowOffset, rc = col - colOffset;
                RecipeIngredient expected = rr >= 0 && rr < height && rc >= 0 && rc < width
                        ? r.getSlot((minRow + rr) * 3 + minCol + rc) : null;
                ItemStack actual = matrix[row * gridSize + col];
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


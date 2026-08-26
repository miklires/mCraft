package io.github.miklires.mcraft.registry;

import org.bukkit.inventory.ItemStack;
import io.github.miklires.mcraft.model.RecipeIngredient;

public class ItemMatcher {

    public static boolean matches(ItemStack stack, RecipeIngredient ingredient) {
        if (ingredient == null) return stack == null || stack.getType().isAir();
        if (stack == null || stack.getType().isAir()) return false;
        if (stack.getType() != ingredient.getMaterial()) return false;

        if (ingredient.isCustom()) {
            String id = ItemBuilder.readCustomId(stack);
            return ingredient.getCustomId().equals(id);
        }
        String id = ItemBuilder.readCustomId(stack);
        return id == null;
    }
}


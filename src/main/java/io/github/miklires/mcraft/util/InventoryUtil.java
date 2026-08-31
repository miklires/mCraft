package io.github.miklires.mcraft.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class InventoryUtil {
    private InventoryUtil() {}

    /** Gives an item without silently deleting overflow. */
    public static void giveOrDrop(Player player, ItemStack stack) {
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(stack);
        leftovers.values().forEach(leftover ->
                player.getWorld().dropItemNaturally(player.getLocation(), leftover));
    }
}

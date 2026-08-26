package io.github.miklires.mcraft.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GuiCloseListener implements Listener {

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof GuiHolder holder)) return;
        if (holder.getScreen() != GuiHolder.GuiScreen.RECIPE_EDIT) return;
        if (!(event.getPlayer() instanceof Player player)) return;

        Inventory top = event.getView().getTopInventory();
        for (int s : RecipeEditGui.CRAFT_SLOTS) {
            ItemStack stack = top.getItem(s);
            if (stack != null && !stack.getType().isAir()) {
                player.getInventory().addItem(stack).forEach((idx, leftover) ->
                        player.getWorld().dropItemNaturally(player.getLocation(), leftover));
                top.setItem(s, null);
            }
        }
        ItemStack result = top.getItem(RecipeEditGui.RESULT_SLOT);
        if (result != null && !result.getType().isAir()) {
            player.getInventory().addItem(result).forEach((idx, leftover) ->
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover));
            top.setItem(RecipeEditGui.RESULT_SLOT, null);
        }
    }
}


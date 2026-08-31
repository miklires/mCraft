package io.github.miklires.mcraft.api;

import io.github.miklires.mcraft.model.CustomItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.Collection;
import java.util.Optional;

public interface MCraftAPI {
    Optional<CustomItem> getItem(String id);
    Collection<String> getItemIds();
    Collection<CustomItem> getItemsByTag(String tag);
    Optional<String> getId(ItemStack stack);
    boolean give(Player player, String id, int amount);
    ItemStack upgrade(ItemStack stack);
}

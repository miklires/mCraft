package io.github.miklires.mcraft.api;

import io.github.miklires.mcraft.MCraft;
import io.github.miklires.mcraft.model.CustomItem;
import io.github.miklires.mcraft.registry.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.*;

public final class DefaultMCraftAPI implements MCraftAPI {
    private final MCraft plugin;
    public DefaultMCraftAPI(MCraft plugin) { this.plugin = plugin; }
    public Optional<CustomItem> getItem(String id) { return Optional.ofNullable(plugin.getItemRegistry().get(id)); }
    public Collection<String> getItemIds() { return plugin.getItemRegistry().all().stream().map(CustomItem::getId).toList(); }
    public Optional<String> getId(ItemStack stack) { return Optional.ofNullable(ItemBuilder.readCustomId(stack)); }
    public boolean give(Player player, String id, int amount) { CustomItem item=plugin.getItemRegistry().get(id); if(item==null||amount<1)return false; player.getInventory().addItem(item.buildItemStack(amount)); return true; }
    public ItemStack upgrade(ItemStack stack) { String id=ItemBuilder.readCustomId(stack); CustomItem item=id==null?null:plugin.getItemRegistry().get(id); if(item==null||ItemBuilder.readVersion(stack)>=item.getVersion())return stack; return item.buildItemStack(stack.getAmount()); }
}

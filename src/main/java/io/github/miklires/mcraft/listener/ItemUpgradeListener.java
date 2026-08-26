package io.github.miklires.mcraft.listener;
import io.github.miklires.mcraft.MCraft;
import io.github.miklires.mcraft.registry.ItemBuilder;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
public final class ItemUpgradeListener implements Listener {
    private final MCraft plugin;
    public ItemUpgradeListener(MCraft plugin){this.plugin=plugin;}
    @EventHandler public void onHeld(PlayerItemHeldEvent event){ ItemStack stack=event.getPlayer().getInventory().getItem(event.getNewSlot()); String id=ItemBuilder.readCustomId(stack); var item=id==null?null:plugin.getItemRegistry().get(id); if(item!=null&&ItemBuilder.readVersion(stack)<item.getVersion()) event.getPlayer().getInventory().setItem(event.getNewSlot(),item.buildItemStack(stack.getAmount())); }
}

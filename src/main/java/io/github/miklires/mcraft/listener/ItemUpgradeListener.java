package io.github.miklires.mcraft.listener;
import io.github.miklires.mcraft.MCraft;
import io.github.miklires.mcraft.registry.ItemBuilder;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import io.github.miklires.mcraft.util.ItemUpgradeUtil;
public final class ItemUpgradeListener implements Listener {
    private final MCraft plugin;
    public ItemUpgradeListener(MCraft plugin){this.plugin=plugin;}
    @EventHandler public void onHeld(PlayerItemHeldEvent event){ int slot=event.getNewSlot(); event.getPlayer().getInventory().setItem(slot, ItemUpgradeUtil.upgrade(plugin,event.getPlayer().getInventory().getItem(slot))); }
    @EventHandler public void onJoin(PlayerJoinEvent event){
        var inventory=event.getPlayer().getInventory();
        for(int slot=0;slot<inventory.getSize();slot++) inventory.setItem(slot,ItemUpgradeUtil.upgrade(plugin,inventory.getItem(slot)));
    }
    @EventHandler(ignoreCancelled=true) public void onPickup(EntityPickupItemEvent event){
        if(event.getEntity() instanceof Player) event.getItem().setItemStack(ItemUpgradeUtil.upgrade(plugin,event.getItem().getItemStack()));
    }
}

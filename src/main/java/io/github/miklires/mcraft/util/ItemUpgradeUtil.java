package io.github.miklires.mcraft.util;

import io.github.miklires.mcraft.MCraft;
import io.github.miklires.mcraft.registry.ItemBuilder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.Repairable;

public final class ItemUpgradeUtil {
    private ItemUpgradeUtil() {}

    public static ItemStack upgrade(MCraft plugin, ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return stack;
        String id = ItemBuilder.readCustomId(stack);
        var definition = id == null ? null : plugin.getItemRegistry().get(id);
        if (definition == null || ItemBuilder.readVersion(stack) >= definition.getVersion()) return stack;
        ItemStack upgraded = definition.buildItemStack(stack.getAmount());
        if (stack.getItemMeta() instanceof Damageable oldDamage && upgraded.getItemMeta() instanceof Damageable newDamage) {
            newDamage.setDamage(Math.min(oldDamage.getDamage(), upgraded.getType().getMaxDurability()));
            if (oldDamage instanceof Repairable oldRepair && newDamage instanceof Repairable newRepair) {
                newRepair.setRepairCost(oldRepair.getRepairCost());
            }
            upgraded.setItemMeta(newDamage);
        }
        return upgraded;
    }
}

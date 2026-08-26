package io.github.miklires.mcraft.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import io.github.miklires.mcraft.MCraft;
import io.github.miklires.mcraft.model.CustomItem;

public class MCraftCommand implements CommandExecutor {

    private final MCraft plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public MCraftCommand(MCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        String prefix = plugin.getMessageUtil().prefix();

        if (args.length == 0) {
            if (!sender.hasPermission("mcraft.command.mcraft")) {
                sender.sendMessage(mm.deserialize(prefix + plugin.getMessageUtil().get("common.no-permission")));
                return true;
            }
            if (!(sender instanceof Player player)) {
                sender.sendMessage(mm.deserialize(prefix + plugin.getMessageUtil().get("command.gui-player-only")));
                return true;
            }
            io.github.miklires.mcraft.gui.MainMenuGui.open(plugin, player);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("mcraft.command.reload")) {
                sender.sendMessage(mm.deserialize(prefix + plugin.getMessageUtil().get("common.no-permission")));
                return true;
            }
            plugin.reloadConfig();
            plugin.getMessageUtil().reload();
            plugin.getItemRegistry().load();
            plugin.getRecipeRegistry().load();
            sender.sendMessage(mm.deserialize(prefix + plugin.getMessageUtil().get("common.reloaded")));
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("mcraft.command.give")) {
                sender.sendMessage(mm.deserialize(prefix + plugin.getMessageUtil().get("common.no-permission")));
                return true;
            }
            if (!(sender instanceof Player player)) {
                sender.sendMessage(mm.deserialize(prefix + plugin.getMessageUtil().get("common.player-only")));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(mm.deserialize(prefix + plugin.getMessageUtil().get("command.usage-give")));
                return true;
            }
            CustomItem ci = plugin.getItemRegistry().get(args[1]);
            if (ci == null) {
                sender.sendMessage(mm.deserialize(prefix + plugin.getMessageUtil().get("item.not-found")));
                return true;
            }
            int amount = 1;
            if (args.length >= 3) {
                try { amount = Math.max(1, Integer.parseInt(args[2])); } catch (NumberFormatException ignored) {}
            }
            player.getInventory().addItem(ci.buildItemStack(amount));
            sender.sendMessage(mm.deserialize(prefix + plugin.getMessageUtil().get("item.given",
                    "id", ci.getId(),
                    "amount", String.valueOf(amount))));
            return true;
        }

        sender.sendMessage(mm.deserialize(prefix + plugin.getMessageUtil().get("command.unknown-sub")));
        return true;
    }
}


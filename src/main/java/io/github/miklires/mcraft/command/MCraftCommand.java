package io.github.miklires.mcraft.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.TabExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import io.github.miklires.mcraft.MCraft;
import io.github.miklires.mcraft.model.CustomItem;
import io.github.miklires.mcraft.util.InventoryUtil;
import java.util.List;
import java.util.Locale;

public class MCraftCommand implements TabExecutor {

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
            plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
                plugin.reloadConfig();
                plugin.getMessageUtil().reload();
                plugin.getItemRegistry().load();
                plugin.getRecipeRegistry().load();
                sender.sendMessage(plugin.getMessageUtil().component("common.reloaded"));
            });
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("mcraft.command.give")) {
                sender.sendMessage(mm.deserialize(prefix + plugin.getMessageUtil().get("common.no-permission")));
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
                try { amount = Integer.parseInt(args[2]); }
                catch (NumberFormatException ignored) { amount = -1; }
            }
            int maximum = Math.max(1, plugin.getConfig().getInt("commands.max-give-amount", 2304));
            if (amount < 1 || amount > maximum) {
                sender.sendMessage(plugin.getMessageUtil().component("command.invalid-amount", "max", String.valueOf(maximum)));
                return true;
            }
            Player player = args.length >= 4 ? Bukkit.getPlayerExact(args[3])
                    : sender instanceof Player p ? p : null;
            if (player == null) {
                sender.sendMessage(plugin.getMessageUtil().component("command.player-not-found"));
                return true;
            }
            InventoryUtil.giveOrDrop(player, ci.buildItemStack(amount));
            sender.sendMessage(mm.deserialize(prefix + plugin.getMessageUtil().get("item.given",
                    "id", ci.getId(),
                    "amount", String.valueOf(amount))));
            return true;
        }

        if (args[0].equalsIgnoreCase("item") || args[0].equalsIgnoreCase("recipe")) {
            if (!sender.hasPermission("mcraft.command.mcraft")) {
                sender.sendMessage(plugin.getMessageUtil().component("common.no-permission"));
                return true;
            }
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getMessageUtil().component("common.player-only"));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(plugin.getMessageUtil().component("command.usage-view", "type", args[0].toLowerCase(Locale.ROOT)));
                return true;
            }
            if (args[0].equalsIgnoreCase("item")) io.github.miklires.mcraft.gui.ItemViewGui.open(plugin, player, args[1]);
            else io.github.miklires.mcraft.gui.RecipeViewGui.open(plugin, player, args[1]);
            return true;
        }

        sender.sendMessage(mm.deserialize(prefix + plugin.getMessageUtil().get("command.unknown-sub")));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        List<String> choices;
        if (args.length == 1) choices = List.of("give", "item", "recipe", "reload");
        else if (args.length == 2 && args[0].equalsIgnoreCase("give")) choices = plugin.getItemRegistry().all().stream().map(CustomItem::getId).toList();
        else if (args.length == 2 && args[0].equalsIgnoreCase("item")) choices = plugin.getItemRegistry().all().stream().map(CustomItem::getId).toList();
        else if (args.length == 2 && args[0].equalsIgnoreCase("recipe")) choices = plugin.getRecipeRegistry().all().stream().map(io.github.miklires.mcraft.model.CustomRecipe::getId).toList();
        else if (args.length == 3 && args[0].equalsIgnoreCase("give")) choices = List.of("1", "16", "64");
        else if (args.length == 4 && args[0].equalsIgnoreCase("give")) choices = Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        else return List.of();
        String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
        return choices.stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(prefix)).sorted().toList();
    }
}


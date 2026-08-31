package io.github.miklires.mcraft;

import org.bukkit.plugin.java.JavaPlugin;
import io.github.miklires.mcraft.command.MCraftCommand;
import io.github.miklires.mcraft.listener.CraftingListener;
import io.github.miklires.mcraft.registry.ItemBuilder;
import io.github.miklires.mcraft.registry.ItemRegistry;
import io.github.miklires.mcraft.registry.RecipeRegistry;
import io.github.miklires.mcraft.api.DefaultMCraftAPI;
import io.github.miklires.mcraft.api.MCraftAPI;
import org.bstats.bukkit.Metrics;

public class MCraft extends JavaPlugin {

    private ItemRegistry itemRegistry;
    private RecipeRegistry recipeRegistry;
    private io.github.miklires.mcraft.util.MessageUtil messageUtil;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveLanguage("en_US");
        saveLanguage("ru_RU");
        messageUtil = new io.github.miklires.mcraft.util.MessageUtil(this);
        ItemBuilder.init(this);
        io.github.miklires.mcraft.gui.GuiButton.init(this);

        itemRegistry = new ItemRegistry(this);
        itemRegistry.load();

        recipeRegistry = new RecipeRegistry(this);
        recipeRegistry.load();

        getServer().getPluginManager().registerEvents(new CraftingListener(this), this);
        getServer().getPluginManager().registerEvents(new io.github.miklires.mcraft.listener.ItemUpgradeListener(this), this);
        getServer().getPluginManager().registerEvents(
                new io.github.miklires.mcraft.gui.GuiClickListener(this), this);
        var command = java.util.Objects.requireNonNull(getCommand("mcraft"), "mcraft command missing from plugin.yml");
        MCraftCommand commandHandler = new MCraftCommand(this);
        command.setExecutor(commandHandler);
        command.setTabCompleter(commandHandler);
        getServer().getServicesManager().register(MCraftAPI.class, new DefaultMCraftAPI(this), this, org.bukkit.plugin.ServicePriority.Normal);
        if (getConfig().getBoolean("metrics.enabled", true) && getConfig().getInt("metrics.bstats-id", 0) > 0) new Metrics(this, getConfig().getInt("metrics.bstats-id"));
        if (getConfig().getBoolean("updates.enabled", true)) io.github.miklires.mcraft.update.UpdateChecker.checkAsync(this, getConfig().getString("updates.modrinth-project-id", ""));

        getLogger().info("mCraft enabled. Items: " + itemRegistry.all().size()
                + ", recipes: " + recipeRegistry.all().size());
    }

    @Override
    public void onDisable() {
        if (recipeRegistry != null) recipeRegistry.restoreVanilla();
        getLogger().info("mCraft disabled.");
    }

    public ItemRegistry getItemRegistry() { return itemRegistry; }
    public RecipeRegistry getRecipeRegistry() { return recipeRegistry; }
    public io.github.miklires.mcraft.util.MessageUtil getMessageUtil() { return messageUtil; }
    private void saveLanguage(String locale) { String path="lang/"+locale+".yml"; if(!new java.io.File(getDataFolder(),path).exists()) saveResource(path,false); }
}


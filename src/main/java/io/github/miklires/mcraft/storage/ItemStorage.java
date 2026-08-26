package io.github.miklires.mcraft.storage;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import io.github.miklires.mcraft.model.CustomItem;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class ItemStorage {

    private final Plugin plugin;
    private final File folder;

    public ItemStorage(Plugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "items");
        if (!folder.exists()) folder.mkdirs();
    }

    public Map<String, CustomItem> loadAll() {
        Map<String, CustomItem> result = new LinkedHashMap<>();
        File[] files = folder.listFiles((d, name) -> name.endsWith(".yml"));
        if (files == null) return result;
        for (File f : files) {
            try {
                CustomItem ci = load(f);
                if (ci != null && ci.getId() != null) result.put(ci.getId(), ci);
            } catch (Exception e) {
                plugin.getLogger().warning("Не удалось прочитать " + f.getName() + ": " + e.getMessage());
            }
        }
        return result;
    }

    private CustomItem load(File f) {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        String id = cfg.getString("id");
        String materialName = cfg.getString("material");
        if (id == null || materialName == null) return null;
        Material mat = Material.matchMaterial(materialName);
        if (mat == null) return null;

        CustomItem ci = new CustomItem(id, mat);
        ci.setDisplayName(cfg.getString("display-name"));
        ci.setLore(cfg.getStringList("lore"));
        ci.setVersion(cfg.getInt("version", 1));
        ci.setItemModel(cfg.getString("item-model"));
        ci.setUnbreakable(cfg.getBoolean("unbreakable", false));

        if (cfg.isConfigurationSection("enchantments")) {
            Map<String, Integer> enchants = new LinkedHashMap<>();
            for (String key : cfg.getConfigurationSection("enchantments").getKeys(false)) {
                enchants.put(key, cfg.getInt("enchantments." + key));
            }
            ci.setEnchantments(enchants);
        }
        return ci;
    }

    public void save(CustomItem ci) {
        File f = new File(folder, ci.getId() + ".yml");
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("id", ci.getId());
        cfg.set("material", ci.getMaterial().name());
        cfg.set("display-name", ci.getDisplayName());
        cfg.set("lore", new ArrayList<>(ci.getLore()));
        cfg.set("version", ci.getVersion());
        cfg.set("item-model", ci.getItemModel());
        cfg.set("unbreakable", ci.isUnbreakable());
        for (var e : ci.getEnchantments().entrySet()) {
            cfg.set("enchantments." + e.getKey(), e.getValue());
        }
        try {
            cfg.save(f);
        } catch (Exception e) {
            plugin.getLogger().severe("Could not save item " + ci.getId() + ": " + e.getMessage());
        }
    }

    public void delete(String id) {
        File f = new File(folder, id + ".yml");
        if (f.exists()) f.delete();
    }
}


package io.github.miklires.mcraft.storage;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import io.github.miklires.mcraft.model.CustomRecipe;
import io.github.miklires.mcraft.model.RecipeIngredient;
import io.github.miklires.mcraft.model.RecipeType;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RecipeStorage {

    private final Plugin plugin;
    private final File folder;

    public RecipeStorage(Plugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "recipes");
        if (!folder.exists()) folder.mkdirs();
    }

    public Map<String, CustomRecipe> loadAll() {
        Map<String, CustomRecipe> result = new LinkedHashMap<>();
        File[] files = folder.listFiles((d, name) -> name.endsWith(".yml"));
        if (files == null) return result;
        for (File f : files) {
            try {
                CustomRecipe r = load(f);
                if (r != null && r.getId() != null) result.put(r.getId(), r);
            } catch (Exception e) {
                plugin.getLogger().warning("Не удалось прочитать рецепт " + f.getName() + ": " + e.getMessage());
            }
        }
        return result;
    }

    private CustomRecipe load(File f) {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        String id = cfg.getString("id");
        String typeName = cfg.getString("type");
        if (id == null || typeName == null) return null;

        CustomRecipe r = new CustomRecipe(id, RecipeType.valueOf(typeName));

        if (r.getType() == RecipeType.SHAPED) {
            ConfigurationSection shape = cfg.getConfigurationSection("shape");
            if (shape != null) {
                for (int i = 0; i < 9; i++) {
                    ConfigurationSection slot = shape.getConfigurationSection(String.valueOf(i));
                    if (slot != null) r.setSlot(i, readIngredient(slot));
                }
            }
        } else {
            List<RecipeIngredient> ingredients = new ArrayList<>();
            List<Map<?, ?>> raw = cfg.getMapList("ingredients");
            for (Map<?, ?> m : raw) {
                String kind = String.valueOf(m.get("kind"));
                String mat = String.valueOf(m.get("material"));
                Material material = Material.matchMaterial(mat);
                if (material == null) continue;
                if ("CUSTOM".equals(kind)) {
                    String cid = String.valueOf(m.get("custom-id"));
                    ingredients.add(RecipeIngredient.custom(cid, material));
                } else {
                    ingredients.add(RecipeIngredient.vanilla(material));
                }
            }
            r.setIngredients(ingredients);
        }

        String resultCustom = cfg.getString("result.custom-id");
        if (resultCustom != null && !resultCustom.isBlank()) {
            r.setResultRefCustomId(resultCustom);
        } else {
            String mat = cfg.getString("result.material");
            if (mat != null) r.setResultVanilla(Material.matchMaterial(mat));
        }
        r.setResultAmount(cfg.getInt("result.amount", 1));
        r.setOverrideVanilla(cfg.getBoolean("override-vanilla", false));
        return r;
    }

    private RecipeIngredient readIngredient(ConfigurationSection s) {
        String kind = s.getString("kind", "VANILLA");
        Material mat = Material.matchMaterial(s.getString("material", "AIR"));
        if (mat == null) return null;
        if ("CUSTOM".equals(kind)) return RecipeIngredient.custom(s.getString("custom-id"), mat);
        return RecipeIngredient.vanilla(mat);
    }

    public void save(CustomRecipe r) {
        File f = new File(folder, r.getId() + ".yml");
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("id", r.getId());
        cfg.set("type", r.getType().name());

        if (r.getType() == RecipeType.SHAPED) {
            for (int i = 0; i < 9; i++) {
                RecipeIngredient slot = r.getSlot(i);
                if (slot == null) continue;
                cfg.set("shape." + i + ".kind", slot.getKind().name());
                cfg.set("shape." + i + ".material", slot.getMaterial().name());
                if (slot.isCustom()) cfg.set("shape." + i + ".custom-id", slot.getCustomId());
            }
        } else {
            List<Map<String, Object>> list = new ArrayList<>();
            for (RecipeIngredient ing : r.getIngredients()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("kind", ing.getKind().name());
                m.put("material", ing.getMaterial().name());
                if (ing.isCustom()) m.put("custom-id", ing.getCustomId());
                list.add(m);
            }
            cfg.set("ingredients", list);
        }

        if (r.isResultCustom()) {
            cfg.set("result.custom-id", r.getResultRefCustomId());
        } else if (r.getResultVanilla() != null) {
            cfg.set("result.material", r.getResultVanilla().name());
        }
        cfg.set("result.amount", r.getResultAmount());
        cfg.set("override-vanilla", r.isOverrideVanilla());

        try {
            cfg.save(f);
        } catch (Exception e) {
            plugin.getLogger().severe("Could not save recipe " + r.getId() + ": " + e.getMessage());
        }
    }

    public void delete(String id) {
        File f = new File(folder, id + ".yml");
        if (f.exists()) f.delete();
    }
}


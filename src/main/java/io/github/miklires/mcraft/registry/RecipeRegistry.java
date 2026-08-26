package io.github.miklires.mcraft.registry;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import io.github.miklires.mcraft.MCraft;
import io.github.miklires.mcraft.model.CustomRecipe;
import io.github.miklires.mcraft.model.RecipeIngredient;
import io.github.miklires.mcraft.model.RecipeType;
import io.github.miklires.mcraft.storage.RecipeStorage;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.*;

public class RecipeRegistry {

    private final MCraft plugin;
    private final RecipeStorage storage;
    private final Map<String, CustomRecipe> recipes = new LinkedHashMap<>();
    private final Map<Material, Set<CustomRecipe>> ingredientIndex = new EnumMap<>(Material.class);
    private final Map<NamespacedKey, Recipe> removedVanilla = new LinkedHashMap<>();
    private int nextId = 1;

    public RecipeRegistry(MCraft plugin) {
        this.plugin = plugin;
        this.storage = new RecipeStorage(plugin);
    }

    public void load() {
        restoreVanilla();
        for (String id : recipes.keySet()) {
            Bukkit.removeRecipe(new NamespacedKey(plugin, id));
        }
        recipes.clear();
        recipes.putAll(storage.loadAll());
        rebuildIndex();
        nextId = plugin.getConfig().getInt("defaults.next-recipe-id", 1);
        for (String id : recipes.keySet()) {
            if (id.startsWith("recipe_")) {
                try {
                    int n = Integer.parseInt(id.substring(7));
                    if (n >= nextId) nextId = n + 1;
                } catch (NumberFormatException ignored) {}
            }
        }
        for (CustomRecipe r : recipes.values()) { if (r.isOverrideVanilla()) removeVanillaConflicts(r); register(r); }
    }

    public Collection<CustomRecipe> all() {
        return recipes.values();
    }

    public CustomRecipe get(String id) {
        return recipes.get(id);
    }

    public String allocateId() {
        String id = String.format("recipe_%03d", nextId);
        nextId++;
        plugin.getConfig().set("defaults.next-recipe-id", nextId);
        plugin.saveConfig();
        return id;
    }

    public void save(CustomRecipe r) {
        CustomRecipe existing = recipes.get(r.getId());
        if (existing != null) Bukkit.removeRecipe(new NamespacedKey(plugin, r.getId()));
        recipes.put(r.getId(), r);
        rebuildIndex();
        storage.save(r);
        if (r.isOverrideVanilla()) removeVanillaConflicts(r);
        register(r);
    }

    public void delete(String id) {
        Bukkit.removeRecipe(new NamespacedKey(plugin, id));
        recipes.remove(id);
        rebuildIndex();
        storage.delete(id);
    }

    private void register(CustomRecipe r) {
        ItemStack result = buildResult(r);
        if (result == null) return;
        NamespacedKey key = new NamespacedKey(plugin, r.getId());
        Recipe bukkitRecipe;
        if (r.getType() == RecipeType.SHAPED) {
            ShapedRecipe sr = new ShapedRecipe(key, result);
            String[] rows = buildShape(r);
            sr.shape(rows[0], rows[1], rows[2]);
            applyShapedChoices(sr, r);
            bukkitRecipe = sr;
        } else {
            ShapelessRecipe sr = new ShapelessRecipe(key, result);
            for (RecipeIngredient ing : r.getIngredients()) {
                sr.addIngredient(choice(ing));
            }
            bukkitRecipe = sr;
        }
        Bukkit.addRecipe(bukkitRecipe);
    }

    private ItemStack buildResult(CustomRecipe r) {
        if (r.isResultCustom()) {
            var ci = plugin.getItemRegistry().get(r.getResultRefCustomId());
            if (ci == null) return null;
            return ci.buildItemStack(r.getResultAmount());
        }
        if (r.getResultVanilla() == null) return null;
        return new ItemStack(r.getResultVanilla(), r.getResultAmount());
    }

    private String[] buildShape(CustomRecipe r) {
        char nextChar = 'A';
        Map<String, Character> keys = new LinkedHashMap<>();
        char[][] grid = new char[3][3];
        for (int i = 0; i < 9; i++) {
            RecipeIngredient ing = r.getSlot(i);
            int row = i / 3, col = i % 3;
            if (ing == null) {
                grid[row][col] = ' ';
                continue;
            }
            String mapKey = ing.isCustom() ? "C:" + ing.getCustomId() : "V:" + ing.getMaterial().name();
            Character ch = keys.get(mapKey);
            if (ch == null) {
                ch = nextChar++;
                keys.put(mapKey, ch);
            }
            grid[row][col] = ch;
        }
        return new String[]{
                new String(grid[0]),
                new String(grid[1]),
                new String(grid[2])
        };
    }

    private void applyShapedChoices(ShapedRecipe sr, CustomRecipe r) {
        char nextChar = 'A';
        Map<String, Character> keys = new LinkedHashMap<>();
        for (int i = 0; i < 9; i++) {
            RecipeIngredient ing = r.getSlot(i);
            if (ing == null) continue;
            String mapKey = ing.isCustom() ? "C:" + ing.getCustomId() : "V:" + ing.getMaterial().name();
            if (!keys.containsKey(mapKey)) {
                char ch = nextChar++;
                keys.put(mapKey, ch);
                sr.setIngredient(ch, choice(ing));
            }
        }
    }

    private RecipeChoice choice(RecipeIngredient ingredient) {
        if (ingredient.isCustom()) {
            var item = plugin.getItemRegistry().get(ingredient.getCustomId());
            if (item != null) return new RecipeChoice.ExactChoice(item.buildItemStack(1));
        }
        return new RecipeChoice.MaterialChoice(ingredient.getMaterial());
    }

    private void rebuildIndex() {
        ingredientIndex.clear();
        for (CustomRecipe recipe : recipes.values()) {
            Collection<RecipeIngredient> ingredients = recipe.getType() == RecipeType.SHAPED ? Arrays.stream(recipe.getShape()).filter(Objects::nonNull).toList() : recipe.getIngredients();
            for (RecipeIngredient ingredient : ingredients) ingredientIndex.computeIfAbsent(ingredient.getMaterial(), ignored -> new LinkedHashSet<>()).add(recipe);
        }
    }

    public Collection<CustomRecipe> candidates(ItemStack[] matrix) {
        Set<Material> present = EnumSet.noneOf(Material.class);
        for (ItemStack stack : matrix) if (stack != null && !stack.getType().isAir()) present.add(stack.getType());
        Set<CustomRecipe> smallest = null;
        for (Material material : present) { Set<CustomRecipe> bucket = ingredientIndex.get(material); if (bucket == null) return List.of(); if (smallest == null || bucket.size() < smallest.size()) smallest = bucket; }
        return smallest == null ? List.of() : List.copyOf(smallest);
    }

    private void removeVanillaConflicts(CustomRecipe custom) {
        List<NamespacedKey> keys = new ArrayList<>();
        Iterator<Recipe> iterator = Bukkit.recipeIterator();
        while (iterator.hasNext()) { Recipe recipe = iterator.next(); if (recipe instanceof Keyed keyed && !keyed.getKey().getNamespace().equals(plugin.getName().toLowerCase(Locale.ROOT)) && conflicts(recipe, custom)) { removedVanilla.putIfAbsent(keyed.getKey(), recipe); keys.add(keyed.getKey()); } }
        keys.forEach(Bukkit::removeRecipe);
    }

    private boolean conflicts(Recipe vanilla, CustomRecipe custom) {
        if (vanilla instanceof ShapedRecipe shaped && custom.getType() == RecipeType.SHAPED) return shapedSignature(shaped).equals(customSignature(custom));
        if (vanilla instanceof ShapelessRecipe shapeless && custom.getType() == RecipeType.SHAPELESS) {
            List<Material> left = shapeless.getChoiceList().stream().map(this::firstMaterial).sorted().toList();
            List<Material> right = custom.getIngredients().stream().map(RecipeIngredient::getMaterial).sorted().toList();
            return left.equals(right);
        }
        return false;
    }
    private String shapedSignature(ShapedRecipe recipe) { StringBuilder out=new StringBuilder(); Map<Character,RecipeChoice> choices=recipe.getChoiceMap(); for(String row:recipe.getShape()){ for(char c:row.toCharArray()) out.append(c==' '?"_":firstMaterial(choices.get(c)).name()).append(','); out.append(';'); } return trimSignature(out.toString()); }
    private String customSignature(CustomRecipe recipe) { StringBuilder out=new StringBuilder(); for(int row=0;row<3;row++){ for(int col=0;col<3;col++){ RecipeIngredient i=recipe.getSlot(row*3+col); out.append(i==null?"_":i.getMaterial().name()).append(','); } out.append(';'); } return trimSignature(out.toString()); }
    private static String trimSignature(String value) { return value.replaceAll("(?:_,)+;+$", "").replaceAll("(?:_,)+;", ";"); }
    private Material firstMaterial(RecipeChoice choice) { if(choice instanceof RecipeChoice.MaterialChoice m&&!m.getChoices().isEmpty())return m.getChoices().getFirst(); if(choice instanceof RecipeChoice.ExactChoice e&&!e.getChoices().isEmpty())return e.getChoices().getFirst().getType(); return Material.AIR; }
    public void restoreVanilla() { if (removedVanilla.isEmpty()) return; for (Recipe recipe : removedVanilla.values()) Bukkit.addRecipe(recipe); removedVanilla.clear(); }
}


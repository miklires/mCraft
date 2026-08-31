package io.github.miklires.mcraft.model;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

public class CustomRecipe {

    private String id;
    private RecipeType type;
    private RecipeIngredient[] shape = new RecipeIngredient[9];
    private List<RecipeIngredient> ingredients = new ArrayList<>();
    private String resultRefCustomId;
    private org.bukkit.Material resultVanilla;
    private int resultAmount = 1;
    private boolean overrideVanilla = false;
    private String permission;
    private int minimumLevel;
    private Set<String> worlds = new LinkedHashSet<>();
    private boolean allowAutomation = true;

    public CustomRecipe() {}

    public CustomRecipe(String id, RecipeType type) {
        this.id = id;
        this.type = type;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public RecipeType getType() { return type; }
    public void setType(RecipeType type) { this.type = type; }

    public RecipeIngredient[] getShape() { return shape; }
    public void setShape(RecipeIngredient[] shape) { this.shape = shape; }
    public RecipeIngredient getSlot(int index) { return shape[index]; }
    public void setSlot(int index, RecipeIngredient ingredient) { shape[index] = ingredient; }

    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<RecipeIngredient> ingredients) {
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
    }

    public String getResultRefCustomId() { return resultRefCustomId; }
    public void setResultRefCustomId(String resultRefCustomId) { this.resultRefCustomId = resultRefCustomId; }
    public org.bukkit.Material getResultVanilla() { return resultVanilla; }
    public void setResultVanilla(org.bukkit.Material resultVanilla) { this.resultVanilla = resultVanilla; }
    public int getResultAmount() { return resultAmount; }
    public void setResultAmount(int resultAmount) { this.resultAmount = Math.max(1, resultAmount); }

    public boolean isOverrideVanilla() { return overrideVanilla; }
    public void setOverrideVanilla(boolean overrideVanilla) { this.overrideVanilla = overrideVanilla; }

    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission == null || permission.isBlank() ? null : permission.trim(); }
    public int getMinimumLevel() { return minimumLevel; }
    public void setMinimumLevel(int minimumLevel) { this.minimumLevel = Math.max(0, minimumLevel); }
    public Set<String> getWorlds() { return Set.copyOf(worlds); }
    public void setWorlds(java.util.Collection<String> worlds) {
        this.worlds = new LinkedHashSet<>();
        if (worlds != null) worlds.stream().filter(java.util.Objects::nonNull).map(String::trim)
                .filter(s -> !s.isEmpty()).map(String::toLowerCase).forEach(this.worlds::add);
    }
    public boolean isAllowAutomation() { return allowAutomation; }
    public void setAllowAutomation(boolean allowAutomation) { this.allowAutomation = allowAutomation; }

    public boolean isResultCustom() {
        return resultRefCustomId != null && !resultRefCustomId.isBlank();
    }
}


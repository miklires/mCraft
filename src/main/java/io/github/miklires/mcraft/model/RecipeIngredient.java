package io.github.miklires.mcraft.model;

import org.bukkit.Material;

public class RecipeIngredient {

    public enum Kind { VANILLA, CUSTOM }

    private final Kind kind;
    private final Material material;
    private final String customId;

    private RecipeIngredient(Kind kind, Material material, String customId) {
        this.kind = kind;
        this.material = material;
        this.customId = customId;
    }

    public static RecipeIngredient vanilla(Material material) {
        return new RecipeIngredient(Kind.VANILLA, material, null);
    }

    public static RecipeIngredient custom(String customId, Material material) {
        return new RecipeIngredient(Kind.CUSTOM, material, customId);
    }

    public Kind getKind() { return kind; }
    public Material getMaterial() { return material; }
    public String getCustomId() { return customId; }

    public boolean isCustom() { return kind == Kind.CUSTOM; }
}


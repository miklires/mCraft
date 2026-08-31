package io.github.miklires.mcraft.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;

public class CustomItem {

    private String id;
    private Material material;
    private String displayName;
    private List<String> lore = new ArrayList<>();
    private Map<String, Integer> enchantments = new LinkedHashMap<>();
    private int version = 1;
    private String itemModel;
    private boolean unbreakable;
    private Set<String> tags = new LinkedHashSet<>();
    private Set<String> itemFlags = new LinkedHashSet<>();

    public CustomItem() {}

    public CustomItem(String id, Material material) {
        this.id = id;
        this.material = material;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public List<String> getLore() { return lore; }
    public void setLore(List<String> lore) { this.lore = lore != null ? lore : new ArrayList<>(); }
    public Map<String, Integer> getEnchantments() { return enchantments; }
    public void setEnchantments(Map<String, Integer> enchantments) {
        this.enchantments = enchantments != null ? enchantments : new LinkedHashMap<>();
    }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = Math.max(1, version); }
    public String getItemModel() { return itemModel; }
    public void setItemModel(String itemModel) { this.itemModel = itemModel; }
    public boolean isUnbreakable() { return unbreakable; }
    public void setUnbreakable(boolean unbreakable) { this.unbreakable = unbreakable; }
    public Set<String> getTags() { return Set.copyOf(tags); }
    public void setTags(java.util.Collection<String> tags) { this.tags = normalized(tags); }
    public Set<String> getItemFlags() { return Set.copyOf(itemFlags); }
    public void setItemFlags(java.util.Collection<String> flags) { this.itemFlags = normalized(flags); }

    private static Set<String> normalized(java.util.Collection<String> values) {
        Set<String> result = new LinkedHashSet<>();
        if (values != null) values.stream().filter(java.util.Objects::nonNull).map(String::trim)
                .filter(s -> !s.isEmpty()).map(s -> s.toLowerCase(java.util.Locale.ROOT)).forEach(result::add);
        return result;
    }

    public ItemStack buildItemStack(int amount) {
        return io.github.miklires.mcraft.registry.ItemBuilder.build(this, amount);
    }
}


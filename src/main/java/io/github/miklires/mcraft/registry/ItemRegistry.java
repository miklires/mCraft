package io.github.miklires.mcraft.registry;

import io.github.miklires.mcraft.MCraft;
import io.github.miklires.mcraft.model.CustomItem;
import io.github.miklires.mcraft.storage.ItemStorage;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ItemRegistry {

    private final MCraft plugin;
    private final ItemStorage storage;
    private final Map<String, CustomItem> items = new LinkedHashMap<>();
    private int nextId = 1;

    public ItemRegistry(MCraft plugin) {
        this.plugin = plugin;
        this.storage = new ItemStorage(plugin);
    }

    public void load() {
        items.clear();
        items.putAll(storage.loadAll());
        nextId = plugin.getConfig().getInt("defaults.next-item-id", 1);
        for (String id : items.keySet()) {
            if (id.startsWith("item_")) {
                try {
                    int n = Integer.parseInt(id.substring(5));
                    if (n >= nextId) nextId = n + 1;
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    public Collection<CustomItem> all() {
        return items.values();
    }

    public CustomItem get(String id) {
        return items.get(id);
    }

    public String allocateId() {
        String id = String.format("item_%03d", nextId);
        nextId++;
        plugin.getConfig().set("defaults.next-item-id", nextId);
        plugin.saveConfig();
        return id;
    }

    public void save(CustomItem ci) {
        items.put(ci.getId(), ci);
        storage.save(ci);
    }

    public void delete(String id) {
        items.remove(id);
        storage.delete(id);
    }
}


package io.github.miklires.mcraft.storage;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Path;

final class StorageFolder {
    private StorageFolder() {}

    static File resolve(Plugin plugin, String configKey, String fallback) {
        String configured = plugin instanceof JavaPlugin javaPlugin
                ? javaPlugin.getConfig().getString(configKey, fallback) : fallback;
        if (configured == null || configured.isBlank()) configured = fallback;
        Path root = plugin.getDataFolder().toPath().toAbsolutePath().normalize();
        Path candidate = root.resolve(configured).normalize();
        if (!candidate.startsWith(root) || candidate.equals(root)) {
            plugin.getLogger().warning("Unsafe storage path at " + configKey + "; using " + fallback + '.');
            candidate = root.resolve(fallback);
        }
        File folder = candidate.toFile();
        if (!folder.exists() && !folder.mkdirs()) {
            throw new IllegalStateException("Could not create storage folder " + folder);
        }
        if (!folder.isDirectory()) throw new IllegalStateException("Storage path is not a directory: " + folder);
        return folder;
    }
}

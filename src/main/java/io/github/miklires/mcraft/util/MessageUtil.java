package io.github.miklires.mcraft.util;

import org.bukkit.configuration.file.YamlConfiguration;
import io.github.miklires.mcraft.MCraft;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MessageUtil {

    private final MCraft plugin;
    private YamlConfiguration messages;

    public MessageUtil(MCraft plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        String path = "lang/" + plugin.getConfig().getString("language", "en_US") + ".yml";
        File file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) plugin.saveResource(path, false);
        messages = YamlConfiguration.loadConfiguration(file);
        var def = plugin.getResource(path);
        if (def != null) {
            messages.setDefaults(YamlConfiguration.loadConfiguration(
                    new InputStreamReader(def, StandardCharsets.UTF_8)));
        }
    }

    public String get(String key) {
        String s = messages.getString(key);
        return s != null ? s : key;
    }

    public String get(String key, String... replacements) {
        String s = get(key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            s = s.replace("{" + replacements[i] + "}", replacements[i + 1]);
        }
        return s;
    }

    public String prefix() {
        return get("prefix");
    }
}


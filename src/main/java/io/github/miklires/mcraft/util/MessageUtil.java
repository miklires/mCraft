package io.github.miklires.mcraft.util;

import org.bukkit.configuration.file.YamlConfiguration;
import io.github.miklires.mcraft.MCraft;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class MessageUtil {

    private static final Pattern SAFE_LOCALE = Pattern.compile("[a-z]{2}_[A-Z]{2}");
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final MCraft plugin;
    private YamlConfiguration messages;

    public MessageUtil(MCraft plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        String locale = plugin.getConfig().getString("language", "en_US");
        if (locale == null || !SAFE_LOCALE.matcher(locale).matches()
                || plugin.getResource("lang/" + locale + ".yml") == null) {
            plugin.getLogger().warning("Unsupported language '" + locale + "'; using en_US.");
            locale = "en_US";
        }
        String path = "lang/" + locale + ".yml";
        File file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) plugin.saveResource(path, false);
        messages = YamlConfiguration.loadConfiguration(file);
        var def = plugin.getResource(path);
        if (def != null) {
            messages.setDefaults(YamlConfiguration.loadConfiguration(
                    new InputStreamReader(def, StandardCharsets.UTF_8)));
            messages.options().copyDefaults(true);
            try { messages.save(file); }
            catch (java.io.IOException e) { plugin.getLogger().warning("Could not update language defaults: " + e.getMessage()); }
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

    public Component component(String key, String... replacements) {
        java.util.List<TagResolver.Single> tags = new java.util.ArrayList<>();
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            tags.add(Placeholder.unparsed(replacements[i].toLowerCase(Locale.ROOT), replacements[i + 1]));
        }
        return MINI_MESSAGE.deserialize(prefix() + get(key).replace('{', '<').replace('}', '>'),
                TagResolver.resolver(tags));
    }
}


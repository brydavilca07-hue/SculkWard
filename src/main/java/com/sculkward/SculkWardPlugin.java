package com.sculkward;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class SculkWardPlugin extends JavaPlugin {

    private ItemManager itemManager;
    private SetBonusListener listener;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        itemManager = new ItemManager(this);
        itemManager.registerRecipes();

        listener = new SetBonusListener(this, itemManager);
        getServer().getPluginManager().registerEvents(listener, this);
        listener.startTask();

        PluginCommand command = getCommand("sculkward");
        if (command != null) {
            SculkWardCommand executor = new SculkWardCommand(this, itemManager);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }

        getLogger().info("Sculk Ward activado. Piezas registradas: " + SculkItem.values().length);
    }

    @Override
    public void onDisable() {
        if (listener != null) {
            listener.stopTask();
        }
        if (itemManager != null) {
            itemManager.unregisterRecipes();
        }
    }

    public void reloadAll() {
        reloadConfig();
        itemManager.registerRecipes();
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    /** Mensaje de config.yml con prefijo. Los pares son clave, valor para los {placeholders}. */
    public Component msg(String key, String... pairs) {
        String prefix = getConfig().getString("messages.prefix", "");
        return LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + format(key, pairs));
    }

    /** Mensaje de config.yml sin prefijo (para la action bar). */
    public Component bar(String key, String... pairs) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(format(key, pairs));
    }

    private String format(String key, String... pairs) {
        String text = getConfig().getString("messages." + key, key);
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            text = text.replace("{" + pairs[i] + "}", pairs[i + 1]);
        }
        return text;
    }
}

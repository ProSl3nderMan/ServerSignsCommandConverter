package me.prosl3nderman.serversignscommandconverter;

import de.czymm.serversigns.ServerSignsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public final class ServerSignsCommandConverter extends JavaPlugin {

    public ServerSignsPlugin SSP;
    public Boolean debug = false;

    @Override
    public void onEnable() {
        getCommand("SSCC").setExecutor(new SSCCCommand(this));

        doConfig();

        setupServerSigns();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void doConfig() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                getLogger().info("Config.yml not found, creating!");
                saveDefaultConfig();
            } else {
                getLogger().info("Config.yml found, loading!");
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void srConfig() {
        saveConfig();
        reloadConfig();
    }

    private void setupServerSigns() {
        Plugin serverSignsPlugin = Bukkit.getPluginManager().getPlugin("ServerSigns");
        if (serverSignsPlugin.isEnabled() && (serverSignsPlugin instanceof ServerSignsPlugin)) {
            this.SSP = (ServerSignsPlugin) serverSignsPlugin;
        } else {
            // Disable the plugin
            Bukkit.getLogger().log(Level.SEVERE, "serverSignsPlugin not installed! Disabling ServerSignsCommandConverter.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    public void toggleDebug() {
        if (debug)
            debug = false;
        else
            debug = true;
    }
}

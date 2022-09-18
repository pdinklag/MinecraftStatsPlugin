package de.pdinklag.mcstats;

import java.io.File;
import java.io.IOException;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Sets up MinecraftStats in the plugin's data folder and schedules regular
 * updates via a MinecraftStatsUpdateTask.
 */
public final class MinecraftStatsPlugin extends JavaPlugin {
    private static final String WEB_SUBFOLDER_NAME = "mcstats";
    private static final String CONFIG_JSON_FILE_NAME = "config.json";

    private static final String DYNMAP_PLUGIN_NAME = "dynmap";
    private static final String DYNMAP_WEB_FOLDER_NAME = "web";

    private static final long TICKS_PER_SECOND = 20;
    private static final long TICKS_PER_MINUTE = 60 * TICKS_PER_SECOND;

    private long updateInterval;

    private File dataFolder;
    private File repositoryFolder;
    private File configJsonFile;

    private UpdateTask updater;

    File getRepositoryFolder() {
        return repositoryFolder;
    }

    File getConfigJsonFile() {
        return configJsonFile;
    }

    void onRepositoryInitialized() {
        getLogger().info("Initialized successfully");

        // schedule updater
        updater = new UpdateTask(this);
        updater.runTaskTimerAsynchronously(this, 0, updateInterval);
    }

    private void setConfigTarget(String target) {
        getConfig().set("target", target);
        saveConfig();
    }

    @Override
    public void onEnable() {
        //
        saveDefaultConfig();

        // try and find a webserver
        String target = getConfig().getString("target", null);
        if (target == null) {
            // detect a plugin known to have a webserver
            Plugin dynmapPlugin = getServer().getPluginManager().getPlugin(DYNMAP_PLUGIN_NAME);
            if (dynmapPlugin != null) {
                File dynmapTarget = new File(new File(dynmapPlugin.getDataFolder(), DYNMAP_WEB_FOLDER_NAME),
                        WEB_SUBFOLDER_NAME);

                target = dynmapTarget.getAbsolutePath();
                getLogger().info("Found dynmap -- setting target to \"" + target + "\"");
                setConfigTarget(target);
            }
        }

        if (target == null) {
            getLogger().warning("no target specified or detected");
            return;
        }

        repositoryFolder = new File(target).getAbsoluteFile();

        // load config
        updateInterval = getConfig().getLong("updateInterval") * TICKS_PER_MINUTE;

        // init data folder
        dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        // generate config.json
        configJsonFile = new File(dataFolder, CONFIG_JSON_FILE_NAME);
        try {
            ConfigJsonGenerator.generate(this, configJsonFile);
        } catch (IOException ex) {
            getLogger().warning(ex.getMessage());
            return;
        }

        // run repository init task (async)
        new RepositoryInitTask(this).runTaskAsynchronously(this);
    }

    @Override
    public void onDisable() {
        if (updater != null) {
            updater.cancel();
        }
    }
}

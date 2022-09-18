package de.pdinklag.mcstats;

import java.io.File;
import java.io.IOException;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Sets up MinecraftStats in the plugin's data folder and schedules regular
 * updates via a MinecraftStatsUpdateTask.
 */
public final class MinecraftStatsPlugin extends JavaPlugin {
    private static final String REPO_FOLDER_NAME = "mcstats";
    private static final String CONFIG_JSON_FILE_NAME = "config.json";

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

    @Override
    public void onEnable() {
        // load config
        saveDefaultConfig();
        updateInterval = getConfig().getLong("updateInterval") * TICKS_PER_MINUTE;

        // init directories
        dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        repositoryFolder = new File(dataFolder, REPO_FOLDER_NAME);
        configJsonFile = new File(dataFolder, CONFIG_JSON_FILE_NAME);

        // generate config.json
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

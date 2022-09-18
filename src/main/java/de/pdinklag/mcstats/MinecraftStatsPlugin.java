package de.pdinklag.mcstats;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Sets up MinecraftStats in the plugin's data folder and schedules regular
 * updates via a MinecraftStatsUpdateTask.
 */
public final class MinecraftStatsPlugin extends JavaPlugin {
    private static final String[] PYTHON3_PROBE_PATHS = new String[] {
            "python3",
            "/usr/bin/python3",
            "/usr/local/bin/python3" };
    private static final String[] GIT_PROBE_PATHS = new String[] {
            "git",
            "/usr/bin/git",
            "/usr/local/bin/git" };

    private static final String WEB_SUBFOLDER_NAME = "mcstats";
    private static final String CONFIG_JSON_FILE_NAME = "config.json";

    private static final String DYNMAP_PLUGIN_NAME = "dynmap";
    private static final String DYNMAP_WEB_FOLDER_NAME = "web";

    private static final long TICKS_PER_SECOND = 20;
    private static final long TICKS_PER_MINUTE = 60 * TICKS_PER_SECOND;

    private long updateInterval;

    private String python3Binary;
    private String gitBinary;

    private File dataFolder;
    private File repositoryFolder;
    private File configJsonFile;

    private UpdateTask updater;

    String getPython3Binary() {
        return python3Binary;
    }

    String getGitBinary() {
        return gitBinary;
    }

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
        //
        Configuration config = getConfig();
        boolean configModified = false;
        saveDefaultConfig();

        // probe for Python3
        python3Binary = config.getString("binaries.python3", null);
        if (python3Binary == null) {
            python3Binary = BinaryProber.probe(PYTHON3_PROBE_PATHS);
            if (python3Binary != null) {
                getLogger().info("found python3: " + python3Binary);
                config.set("binaries.python3", python3Binary);
                configModified = true;
            }
        }

        if (python3Binary == null) {
            getLogger().warning("failed to locate python3 binary -- please specify it manually in config.yml");
            getLogger().info("DEBUG: $PATH=" + System.getenv("PATH"));
            return;
        }

        // probe for git
        gitBinary = config.getString("binaries.git", null);
        if (gitBinary == null) {
            gitBinary = BinaryProber.probe(GIT_PROBE_PATHS);
            if (gitBinary != null) {
                getLogger().info("found git: " + gitBinary);
                config.set("binaries.git", gitBinary);
                configModified = true;
            }
        }

        if (gitBinary == null) {
            getLogger().warning("failed to locate git binary -- please specify it manually in config.yml");
            getLogger().info("DEBUG: $PATH=" + System.getenv("PATH"));
            return;
        }

        // try and find a webserver
        String target = config.getString("target", null);
        if (target == null) {
            // detect a plugin known to have a webserver
            Plugin dynmapPlugin = getServer().getPluginManager().getPlugin(DYNMAP_PLUGIN_NAME);
            if (dynmapPlugin != null) {
                File dynmapTarget = new File(new File(dynmapPlugin.getDataFolder(), DYNMAP_WEB_FOLDER_NAME),
                        WEB_SUBFOLDER_NAME);

                target = dynmapTarget.getAbsolutePath();
                getLogger().info("Found dynmap -- setting target to \"" + target + "\"");
                config.set("target", target);
                configModified = true;
            }
        }

        if (target == null) {
            getLogger().warning("no target specified or detected");
            return;
        }

        repositoryFolder = new File(target).getAbsoluteFile();

        // maybe save config
        if (configModified) {
            saveConfig();
        }

        // settings
        updateInterval = config.getLong("updateInterval") * TICKS_PER_MINUTE;

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

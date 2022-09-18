package de.pdinklag.mcstats;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Sets up MinecraftStats in the plugin's data folder and schedules regular updates via a MinecraftStatsUpdateTask.
 */
public final class MinecraftStatsPlugin extends JavaPlugin {
    private static final String REPO_URL = "https://github.com/pdinklag/MinecraftStats.git";
    private static final String REPO_DIR = "mcstats";
    private static final String CONFIG_TEMPLATE = "config-template.json";
    private static final String CONFIG_FILE = "config.json";
    
    private static final long TICKS_PER_SECOND = 20;

    private static String escapeBackslashesForPython(String s) {
        // hats off to Mr. Hatter for finding this
        return s.replace("\\", "\\/");
    }

    static String readStreamFully(InputStream in) {
        return new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
    }

    private long updateInterval = 5 * 60 * TICKS_PER_SECOND; // TODO: make configurable
    
    private File dataFolder;
    private File repoFolder;
    private File configFile;
    
    private UpdateTask updater;
    
    File getRepoFolder() {
        return repoFolder;
    }
    
    File getConfigFile() {
        return configFile;
    }
    
    @Override
    public void onEnable() {
        try {
            // make sure the data folder exists
            dataFolder = getDataFolder();
            if(!dataFolder.exists()) dataFolder.mkdir();
            
            // test for .git repository
            repoFolder = new File(dataFolder, REPO_DIR);
            if(repoFolder.exists()) {
                // update
                getLogger().info("updating repository ...");
                Process p = Runtime.getRuntime().exec(new String[] { "git", "pull" }, new String[0], repoFolder);
                p.waitFor(); // TODO: wait asynchronously?
            } else {
                // clone
                getLogger().info("cloning repository ...");
                // TODO: clone a specific branch that is maintained to be compatible to the plugin
                Process p = Runtime.getRuntime().exec(new String[] { "git", "clone", REPO_URL, REPO_DIR }, new String[0], dataFolder);
                p.waitFor(); // TODO: wait asynchronously?
            }
            
            configFile = new File(dataFolder, CONFIG_FILE);
            if(!configFile.exists()) {
                // create config
                getLogger().info("creating config ...");
                
                // read template
                String configTemplate;
                {
                    InputStream in = getClass().getClassLoader().getResourceAsStream(CONFIG_TEMPLATE);
                    configTemplate = readStreamFully(in);
                    in.close();
                }
                
                // replace settings
                String config = configTemplate
                    .replace("@SERVER_SOURCE_PATH@", escapeBackslashesForPython(Paths.get("").toAbsolutePath().toString()))
                    .replace("@SERVER_SOURCE_WORLD@", getServer().getWorlds().get(0).getName());

                // write config
                Files.write(configFile.toPath(), config.getBytes());
            }
            
            getLogger().info("initialized successfully");

            // schedule updater
            updater = new UpdateTask(this);
            updater.runTaskTimerAsynchronously(this, 0, updateInterval);
        } catch(Exception ex) {
            getLogger().warning(ex.getMessage());
        }
    }
    
    @Override
    public void onDisable() {
        if(updater != null) updater.cancel();
    }
}

package de.pdinklag.mcstats;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.bukkit.scheduler.BukkitRunnable;

/**
 * Runs MinecraftStats' update.py script and writes the result to log files.
 */
public final class MinecraftStatsUpdateTask extends BukkitRunnable {
    private static final String UPDATE_SCRIPT = "update.py";
    private static final String UPDATE_LOG = "update.log";
    private static final String UPDATE_ERROR_LOG = "update-error.log";
    
    private final MinecraftStatsPlugin plugin;
    
    private boolean updating = false;
    
    public MinecraftStatsUpdateTask(MinecraftStatsPlugin plugin) {
        this.plugin = plugin;
    }
    
    private void writeLog(String filename, String contents) throws IOException {
        Files.write(new File(plugin.getDataFolder(), filename).toPath(), contents.getBytes());
    }
    
    @Override
    public void run() {
        if(updating) return; // still updating, seems to take longer
        
        updating = true;

        plugin.getLogger().info("Running update...");
        try {
            Process p = Runtime.getRuntime().exec(new String[] {
                "python3",
                UPDATE_SCRIPT,
                plugin.getConfigFile().getAbsolutePath()
            }, new String[0], plugin.getRepoFolder());
            
            p.waitFor();
            
            {
                InputStream stdout = p.getInputStream();
                writeLog(UPDATE_LOG, MinecraftStatsPlugin.readStreamFully(stdout));
                stdout.close();
            }
            {
                InputStream stderr = p.getErrorStream();
                writeLog(UPDATE_ERROR_LOG, MinecraftStatsPlugin.readStreamFully(stderr));
                stderr.close();
            }
        } catch(Exception ex) {
            plugin.getLogger().warning(ex.getMessage());
        }
        
        updating = false;
    }
}

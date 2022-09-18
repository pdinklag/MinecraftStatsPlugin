package de.pdinklag.mcstats;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.bukkit.scheduler.BukkitRunnable;

/**
 * Runs MinecraftStats' update.py script and writes the result to log files.
 */
public final class UpdateTask extends BukkitRunnable {
    private static final String UPDATE_SCRIPT = "update.py";
    private static final String UPDATE_LOG = "update.log";
    private static final String UPDATE_ERROR_LOG = "update-error.log";

    private final MinecraftStatsPlugin plugin;

    private boolean updating = false;

    public UpdateTask(MinecraftStatsPlugin plugin) {
        this.plugin = plugin;
    }

    private void writeLog(String filename, String contents) throws IOException {
        Files.write(new File(plugin.getDataFolder(), filename).toPath(), contents.getBytes());
    }

    @Override
    public void run() {
        if (updating) {
            return; // still updating, seems to take longer
        }

        updating = true;

        plugin.getLogger().info("Running update ...");
        try {
            Process p = Runtime.getRuntime().exec(
                    new String[] { "python3", UPDATE_SCRIPT, plugin.getConfigJsonFile().getAbsolutePath() },
                    new String[0],
                    plugin.getRepositoryFolder());

            int returnCode = p.waitFor();
            {
                InputStream stdout = p.getInputStream();
                writeLog(UPDATE_LOG, Utility.readStreamFully(stdout));
                stdout.close();
            }
            {
                InputStream stderr = p.getErrorStream();
                writeLog(UPDATE_ERROR_LOG, Utility.readStreamFully(stderr));
                stderr.close();
            }

            if (returnCode == 0) {
                plugin.getLogger().info("Update completed -- check update logs for details");
            } else {
                plugin.getLogger().warning("update.py returned with error code "
                        + returnCode
                        + " -- check update logs for details");
            }
        } catch (Exception ex) {
            plugin.getLogger().warning(ex.getMessage());
        }

        updating = false;
    }
}

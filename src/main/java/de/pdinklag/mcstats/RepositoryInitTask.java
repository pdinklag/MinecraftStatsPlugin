package de.pdinklag.mcstats;

import java.io.File;
import java.io.InputStream;

import org.bukkit.scheduler.BukkitRunnable;

/**
 * Initializes the MinecraftStats repository.
 */
public final class RepositoryInitTask extends BukkitRunnable {
    private static final String REPOSITORY_URL = "https://github.com/pdinklag/MinecraftStats.git";

    private final MinecraftStatsPlugin plugin;

    public RepositoryInitTask(MinecraftStatsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        File repoFolder = plugin.getRepositoryFolder();
        plugin.getLogger().info("Initializing repository ...");
        Process p;
        try {
            if (repoFolder.exists()) {
                // update
                p = Runtime.getRuntime().exec(
                        new String[] { plugin.getGitBinary(), "pull" },
                        new String[0],
                        repoFolder);
            } else {
                // clone
                p = Runtime.getRuntime().exec(
                        new String[] { plugin.getGitBinary(), "clone", REPOSITORY_URL, repoFolder.getAbsolutePath() },
                        new String[0]);
            }

            int returnCode = p.waitFor();
            if(returnCode == 0) {
                plugin.onRepositoryInitialized();
            } else {
                InputStream stderr = p.getErrorStream();
                plugin.getLogger().warning(Utility.readStreamFully(stderr));
                stderr.close();
            }
        } catch (Exception ex) {
            plugin.getLogger().warning(ex.getMessage());
        }
    }
}

package de.pdinklag.mcstats;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.bukkit.plugin.Plugin;

/**
 * Generates the config.json file for MinecraftStats.
 */
public class ConfigJsonGenerator {
    private static final String CONFIG_TEMPLATE = "config-template.json";

    private static String escapeBackslashesForPython(String s) {
        // hats off to Mr. Hatter for finding this
        return s.replace("\\", "\\/");
    }

    public static void generate(Plugin plugin, File configJsonFile) throws IOException {
        if (!configJsonFile.exists()) {
            // read template
            String configTemplate;
            {
                InputStream in = plugin.getClass().getClassLoader().getResourceAsStream(CONFIG_TEMPLATE);
                configTemplate = Utility.readStreamFully(in);
                in.close();
            }

            // replace settings
            String config = configTemplate
                    .replace("@SERVER_SOURCE_PATH@",
                            escapeBackslashesForPython(Paths.get("").toAbsolutePath().toString()))
                    .replace("@SERVER_SOURCE_WORLD@",
                            plugin.getServer().getWorlds().get(0).getName());

            // write config
            Files.write(configJsonFile.toPath(), config.getBytes());
        }
    }
}

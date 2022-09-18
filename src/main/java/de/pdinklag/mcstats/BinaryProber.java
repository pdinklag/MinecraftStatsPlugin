package de.pdinklag.mcstats;

public class BinaryProber {
    private static final String[] FLAGS_VERSION = new String[] { "--version" };

    public static String probe(String[] paths, String[] flags) {
        String[] cmd = new String[flags.length + 1];
        System.arraycopy(flags, 0, cmd, 1, flags.length);

        for (String path : paths) {
            cmd[0] = path;

            try {
                Process p = Runtime.getRuntime().exec(cmd);
                int returnCode = p.waitFor();

                if (returnCode == 0) {
                    return path;
                }
            } catch (Exception ex) {
                // nope
            }
        }
        return null;
    }

    public static String probe(String[] paths) {
        return probe(paths, FLAGS_VERSION);
    }
}

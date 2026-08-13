package me.matl114.logitech.utils;

import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import me.matl114.logitech.ConfigLoader;

public final class FeatureConfig {
    private FeatureConfig() {}

    private static Config source(String path) {
        if (ConfigLoader.CONFIG != null && ConfigLoader.CONFIG.contains(path)) return ConfigLoader.CONFIG;
        if (ConfigLoader.INNERCONFIG != null && ConfigLoader.INNERCONFIG.contains(path)) return ConfigLoader.INNERCONFIG;
        return null;
    }

    public static boolean getBoolean(String path, boolean fallback) {
        Config config = source(path);
        return config == null ? fallback : config.getBoolean(path);
    }

    public static int getInt(String path, int fallback) {
        Config config = source(path);
        return config == null ? fallback : config.getInt(path);
    }

    public static double getDouble(String path, double fallback) {
        Config config = source(path);
        return config == null ? fallback : config.getDouble(path);
    }

    public static List<String> getStringList(String path) {
        Config config = source(path);
        return config == null ? Collections.emptyList() : config.getStringList(path);
    }

    public static Set<String> getKeys(String path) {
        Config config = source(path);
        return config == null ? Collections.emptySet() : config.getKeys(path);
    }
}

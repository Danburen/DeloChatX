package org.nitrowater.velochatx.manager;

import org.waterwood.plugin.velocity.util.MethodBase;

@Deprecated
public class LogManager extends MethodBase {
    private static boolean LOG_TO_CONSOLE = true;
    private static boolean LOG_COLOR_CONVERT = true;

    public static void initialize(){
        LOG_TO_CONSOLE = getConfigs().getBoolean("log-text.enable",true);
        LOG_COLOR_CONVERT = getConfigs().getBoolean("log-text.convert",true);
    }

    public static boolean isLogColorConvert() {
        return LOG_COLOR_CONVERT;
    }

    public static boolean isLogToConsole() {
        return LOG_TO_CONSOLE;
    }
}

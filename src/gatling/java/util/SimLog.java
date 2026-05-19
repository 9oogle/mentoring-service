package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class SimLog {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private SimLog() {}

    public static void setup(String fmt, Object... args) { log("SETUP", fmt, args); }
    public static void info (String fmt, Object... args) { log("INFO ", fmt, args); }
    public static void warn (String fmt, Object... args) { log("WARN ", fmt, args); }
    public static void error(String fmt, Object... args) { log("ERROR", fmt, args); }

    private static void log(String level, String fmt, Object... args) {
        System.out.printf("[%s] [%s] %s%n",
                LocalDateTime.now().format(FMT),
                level,
                args.length == 0 ? fmt : String.format(fmt, args));
    }
}
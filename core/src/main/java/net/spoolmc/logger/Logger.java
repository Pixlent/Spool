package net.spoolmc.logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    public static void debug(String location, String message) {
        printf(location, Level.DEBUG, message);
    }

    public static void setup(String location, String message) {
        printf(location, Level.SETUP, message);
    }

    public static void info(String location, String message) {
        printf(location, Level.INFO, message);
    }

    public static void warn(String location, String message) {
        printf(location, Level.WARN, message);
    }

    public static void error(String location, String message) {
        printf(location, Level.ERROR, message);
    }

    private static void printf(String location, Level level, String message) {
        System.out.println(preparePrefix(location, level)
        + prepareLevelColor(level)
        + message
        + Color.RESET);
    }

    private static String preparePrefix(String location, Level level) {
        return "["
                + Color.YELLOW
                + prepareDate()
                + " - "
                + location
                + "/"
                + level.name()
                + Color.WHITE
                + "] ";
    }

    private static Color prepareLevelColor(Level level) {
        return switch (level) {
            case DEBUG -> Color.BLUE;
            case SETUP -> Color.MAGENTA;
            case INFO -> Color.CYAN;
            case WARN -> Color.YELLOW;
            case ERROR -> Color.RED;
        };
    }

    private static String prepareDate() {
        // LocalDateTime object with the date and time
        LocalDateTime dateTime = LocalDateTime.now();

        // Define a custom date and time format pattern
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        // Return the formatted time
        return dateTime.format(formatter);
    }
}

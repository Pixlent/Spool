package net.spoolmc.logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private final String origin;
    public Logger(String origin) {
        this.origin = origin;
    }

    public void debug(String message) {
        printf(Level.DEBUG, message);
    }

    public void setup(String message) {
        printf(Level.SETUP, message);
    }

    public void info(String message) {
        printf(Level.INFO, message);
    }

    public void warn(String message) {
        printf(Level.WARN, message);
    }

    public void error(String message) {
        printf(Level.ERROR, message);
    }

    private void printf(Level level, String message) {
        System.out.println(preparePrefix(level)
        + prepareLevelColor(level)
        + message
        + Color.RESET);
    }

    private String preparePrefix(Level level) {
        return "["
                + Color.YELLOW
                + prepareDate()
                + " - "
                + origin
                + "/"
                + level.name()
                + Color.WHITE
                + "] ";
    }

    private Color prepareLevelColor(Level level) {
        return switch (level) {
            case DEBUG -> Color.BLUE;
            case SETUP -> Color.MAGENTA;
            case INFO -> Color.CYAN;
            case WARN -> Color.YELLOW;
            case ERROR -> Color.RED;
        };
    }

    private String prepareDate() {
        // LocalDateTime object with the date and time
        LocalDateTime dateTime = LocalDateTime.now();

        // Define a custom date and time format pattern
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        // Return the formatted time
        return dateTime.format(formatter);
    }
}

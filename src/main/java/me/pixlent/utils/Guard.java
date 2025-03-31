package me.pixlent.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

/**
 * A utility class to clean up exceptions
 */
public class Guard {
    /**
     * A utility method to cleanly do try catch without the ugliness that comes with it
     *
     * @param error The error message that will display in console if an error happens to occur
     * @param runnable The code you want to try and catch
     */
    public static void tryCatch(String error, ExceptionalRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            handleException(e, error);
        }
    }

    /**
     * Executes a code block supplied by a {@link Supplier}, capturing and handling any exceptions
     * that may occur during execution. If an exception occurs, it is logged,
     * and this method returns {@code null}.
     *
     * @param <T>      the type of result returned by the {@code Supplier}
     * @param supplier a {@code Supplier} representing the code to execute
     * @return the result of executing the code block, or {@code null} if an exception occurs
     * @see Supplier
     */
    public static <T> T tryCatchReturn(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    private static void handleException(Exception e, String error) {
        System.out.println(error + "\n" + e.getMessage());
        saveLogFile(e);
    }

    private static void handleException(Exception e) {
        System.out.println(e.getMessage());
        saveLogFile(e);
    }

    private static void saveLogFile(Exception e) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String timestamp = LocalDateTime.now().format(formatter);
        final var log = FileUtils.getBasePath().resolve("logs/" + timestamp + ".txt").toFile();
        Guard.tryCatch("Couldn't create error file", log::createNewFile);

        if (log.mkdirs()) System.out.println("Couldn't create directories for " + log.getPath());

        FileUtils.writeFile(log, e.getMessage());
    }

    @FunctionalInterface
    public interface ExceptionalRunnable {
        void run() throws Exception;
    }
}
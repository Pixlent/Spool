package net.spoolmc.Guard;

import net.spoolmc.logger.Logger;

import java.util.function.Supplier;

/**
 * A utility class to clean up exceptions
 */
public class Guard {
    private static final Logger logger = new Logger("Guard");

    /**
     * A utility method to cleanly do try catch without the ugliness that comes with it
     *
     * @param runnable The code you want to try and catch
     */
    public static void tryCatch(ExceptionalRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A utility method to cleanly do try catch without the ugliness that comes with it
     *
     * @param error The error message that will display in console if an error would happen to occur
     * @param runnable The code you want to try and catch
     */
    public static void tryCatch(String error, ExceptionalRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            logger.error(error);
            e.printStackTrace();
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
            e.printStackTrace();
            return null;
        }
    }

    @FunctionalInterface
    private interface ExceptionalRunnable {
        void run() throws Exception;
    }
}

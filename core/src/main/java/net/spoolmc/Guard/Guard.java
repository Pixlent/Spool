package net.spoolmc.Guard;

import net.spoolmc.logger.Logger;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

public class Guard {
    private static final Logger logger = new Logger("Guard");

    public static void tryCatch(ExceptionalRunnable runnable) {
        try {
            runnable.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    public interface ExceptionalRunnable {
        void run() throws IOException;
    }
}

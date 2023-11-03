package net.spoolmc;

/**
 * A utility class to time how long something takes to execute
 * Initialize the object to begin, and run {@link #finished()}
 */
public class ExecutionTimer {
    private final long startTime = System.nanoTime();
    private long endTime;

    /**
     * Run method when your execution has finished
     *
     * @return The time used for execution in milliseconds
     */
    public long finished() {
        return (System.nanoTime() - startTime) / 1_000_000;
    }
}

package me.pixlent.demo.api;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import org.graalvm.polyglot.Value;

public class SystemApi {

    public long millis() {
        return System.currentTimeMillis();
    }

    public long nanos() {
        return System.nanoTime();
    }

    public void schedule(Value callback, TaskSchedule delay) {
        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            try { callback.execute(); } catch (Exception _) {}
            return null;
        }, delay);
    }

    public void log(String message) {
        System.out.println("[LOG] " + message);
    }
}

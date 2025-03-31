package me.pixlent.demo;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;

import java.io.IOException;
import java.nio.file.*;

public class Watcher {
    public void watch(Path directory, TaskSchedule schedule, Runnable callback) {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

            MinecraftServer.getSchedulerManager().buildTask(() -> {
                WatchKey key = watchService.poll();
                if (key != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        callback.run();
                    }
                    key.reset();
                }
            }).repeat(schedule).schedule();

        } catch (IOException e) {
            System.err.println("Error watching directory: " + e.getMessage());
        }
    }
}

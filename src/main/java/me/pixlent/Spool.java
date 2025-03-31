package me.pixlent;

import me.pixlent.signal.AbstractSignal;
import me.pixlent.signal.SignalListener;
import me.pixlent.utils.FileUtils;
import org.graalvm.polyglot.Value;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Spool {
    private static Spool spool = null;
    private SpoolEngine spoolEngine = null;

    private final List<Path> indexers = new ArrayList<>();
    private final Map<String, Map<String, Object>> repos = new HashMap<>();
    private final Map<String, String> packages = new HashMap<>();

    private final ReloadSignal signal = new ReloadSignal();

    private Spool() {

    }

    public static Spool hook() {
        if (spool == null) spool = new Spool();
        return spool;
    }

    public void registerRepo(String repoName, Map<String, Object> repoObjects) {
        repos.put(repoName, repoObjects);
    }

    public void registerPackage(String identifier, String path) {
        packages.put(identifier, path);
    }

    public void index(Path directory) {
        indexers.add(directory);
    }

    public Value execute(String js) {
        return spoolEngine.execute(js);
    }

    public Value getClassAsValue(String path) {
        return spoolEngine.requireClass(path);
    }

    public void reload() {
        signal.emit(new ReloadSignal.ReloadEvent(ReloadSignal.LoadState.PRE));
        if (spoolEngine != null) spoolEngine.context.close();
        spoolEngine = new SpoolEngine(repos, packages);
        indexers.forEach(directory -> FileUtils.searchDirectoryDeep(directory).forEach(file -> {
            if (file.getName().endsWith(".js")) spoolEngine.execute(file);
        }));
        signal.emit(new ReloadSignal.ReloadEvent(ReloadSignal.LoadState.POST));
    }

    public void subscribeToReload(SignalListener<ReloadSignal.ReloadEvent> signalListener) {
        signal.subscribe(signalListener);
    }

    public void unsubscribeToReload(SignalListener<ReloadSignal.ReloadEvent> signalListener) {
        signal.unsubscribe(signalListener);
    }

    public static class ReloadSignal extends AbstractSignal<ReloadSignal.ReloadEvent> {
        public record ReloadEvent(LoadState state) { }
        public enum LoadState {
            PRE,
            POST
        }
    }
}
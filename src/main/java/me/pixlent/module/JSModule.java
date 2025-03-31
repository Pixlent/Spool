package me.pixlent.module;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import me.pixlent.SpoolApi;
import me.pixlent.utils.ValueUtils;
import me.pixlent.utils.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class JSModule extends SpoolModule {
    private final Path root;
    private final Manifest manifest;

    public JSModule(Path root, Manifest manifest) {
        this.root = root;
        this.manifest = manifest;

        module.putMember("exports", ValueUtils.emptyObject());
    }

    public JSModule(File manifest) {
        this.root = manifest.toPath().getParent();
        this.manifest = new Gson().fromJson(FileUtils.readFile(manifest), Manifest.class);
    }

    public void initialize() {
        // initialize all entry-points
        manifest.getEntryPoints(root).forEach(file -> SpoolApi.INSTANCE.execute(file, Map.of(
                "module", module,
                "exports", ValueUtils.emptyObject(),
                "require", RequireApi.makeRequire(file.toPath().getParent())
        )));
    }

    public record Manifest(String name, @SerializedName("entry_points") List<String> entryPoints) {
        public List<File> getEntryPoints(Path root) {
            return entryPoints.stream()
                    .map(root::resolve)   // Resolve path relative to root
                    .map(Path::normalize) // Normalize to remove ".."
                    .filter(resolvedPath -> resolvedPath.startsWith(root)) // Security check
                    .map(Path::toFile)
                    .collect(Collectors.toList()); // Collect valid paths into a list
        }
    }
}

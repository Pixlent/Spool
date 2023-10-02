package net.spoolmc;

import lombok.Getter;
import net.spoolmc.Guard.Guard;
import net.spoolmc.file.FileManager;
import net.spoolmc.logger.Logger;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.io.File;

public class Script {
    @Getter private final File file;
    private final Value function;
    private final Logger logger = new Logger("Script");

    Script(File file, Context engine) {
        this.file = file;
        String functionContent = "() => {\n" + FileManager.readFile(file) + "\n}";

        function = Guard.tryCatchReturn(() -> engine.eval("js", functionContent));
    }

    public Value execute(Object... arguments) {
        if (function == null) {
            logger.error("Function " + file.getPath() + " is null");
            return null;
        }
        return function.execute(arguments);
    }
}

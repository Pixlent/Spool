package net.spoolmc;

import lombok.Getter;
import net.spoolmc.logger.Logger;
import org.graalvm.polyglot.Value;

import java.io.File;

public class Script {
    @Getter private final File file;
    private final Value function;
    private final Logger logger = new Logger("Script");

    Script(File file, Compiler compiler) {
        this.file = file;
        function = compiler.compile(file);
    }

    public Value execute(Object context) {
        if (function == null) {
            logger.error("Function " + file.getPath() + " is null");
            return null;
        }
        return function.execute(context);
    }
}

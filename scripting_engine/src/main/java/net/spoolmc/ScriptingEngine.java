package net.spoolmc;

import lombok.Getter;
import net.spoolmc.logger.Logger;
import org.graalvm.polyglot.Engine;

public class ScriptingEngine {
    private static final Logger logger = new Logger("ScriptingEngine");
    @Getter private static final Compiler compiler = new Compiler("js");

    public static void addDefaultBinding(String identifier, Object value) {
        compiler.putMember(identifier, value);
    }
}

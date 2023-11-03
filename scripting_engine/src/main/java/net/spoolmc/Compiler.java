package net.spoolmc;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

import java.io.File;

public class Compiler {
    private final Context context;
    private final String language;

    Compiler(String language) {
        this.language = language;

        this.context = Guard.tryCatchReturn(() -> Context.newBuilder("js")
                .allowAllAccess(true)
                .allowHostAccess(HostAccess.ALL)
                .allowHostClassLookup(className -> true)
                .allowNativeAccess(true)
                .allowHostClassLoading(true)
                .allowInnerContextOptions(true)
                .build());
    }

    public void putMember(String identifier, Object value) {
        context.getBindings(language).putMember(identifier, value);
    }

    public Value compile(String function) {
        String functionContent = "(context) => {\n" + function + "\n}";
        return Guard.tryCatchReturn(() -> context.eval(language, functionContent));
    }

    public Value compile(File function) {
        String functionContent = "(context) => {\n" + FileManager.readFile(function) + "\n}";
        return Guard.tryCatchReturn(() -> context.eval(language, functionContent));
    }
}

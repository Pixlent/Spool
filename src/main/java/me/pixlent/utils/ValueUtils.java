package me.pixlent.utils;

import me.pixlent.SpoolApi;
import net.kyori.adventure.Adventure;
import net.kyori.adventure.text.Component;
import org.graalvm.polyglot.Value;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValueUtils {
    private static final Value javaTypeFunction =
            SpoolApi.INSTANCE.privileged.eval(SpoolApi.INSTANCE.language, "(type) => {return Java.type(type)}");

    public static Value emptyObject() {
        return SpoolApi.INSTANCE.privileged.eval("js", "({})");
    }

    public static Map<String, Value> packageClasses(String packageName) {
        List<String> entries = ReflectionUtils.findClassNamesInPackage(packageName);
        Map<String, Value> classes = new HashMap<>();

        entries.forEach(c -> {
            c = c.replaceFirst(packageName + ".", "");
            try {
                classes.put(c, type(packageName + "." + c));
            } catch (Exception _) {}
        });
        return classes;
    }

    public static Value type(String type) {
        return javaTypeFunction.execute(type);
    }
}

package me.pixlent.module;

import me.pixlent.utils.ValueUtils;
import org.graalvm.polyglot.Value;

public class SpoolModule {
    public final Value module = ValueUtils.emptyObject();

    public SpoolModule() {
        module.putMember("exports", ValueUtils.emptyObject());
    }

    public void export(String identifier, Object object) {
        module.getMember("exports").putMember(identifier, object);
    }

    public Value getExports() {
        return module.getMember("exports");
    }
}

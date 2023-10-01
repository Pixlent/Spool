package net.spoolmc;

import lombok.Getter;

import java.io.File;

public class Script {
    @Getter private final File file;

    Script(File file) {
        this.file = file;
    }

    public void execute() {

    }
}

package net.spoolmc.data;

import java.util.List;

public record ManifestDisplay (
        String name,
        String description,
        List<String> authors,
        String group
) {}

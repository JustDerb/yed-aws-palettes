package com.justinderby.yed;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Section {

    private final List<Icon> icons;
    private final String version;
    private final String url;

    public Section(List<Icon> icons, String version, String url) {
        this.icons = Objects.requireNonNull(icons);
        this.version = version;
        this.url = url;
    }

    public List<Icon> getIcons() {
        return Collections.unmodifiableList(this.icons);
    }

    public Optional<String> getVersion() {
        return Optional.ofNullable(this.version);
    }

    public Optional<String> getUrl() {
        return Optional.ofNullable(this.url);
    }
}

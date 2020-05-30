package com.justinderby.yed;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class MustacheSectionTemplate {

    private final MustacheFactory mustacheFactory;
    private final InputStream template;

    public MustacheSectionTemplate(InputStream template) {
        this(template, new DefaultMustacheFactory());
    }

    public MustacheSectionTemplate(InputStream template, MustacheFactory mustacheFactory) {
        this.template = Objects.requireNonNull(template);
        this.mustacheFactory = Objects.requireNonNull(mustacheFactory);
    }

    private Map<String, String> transformToNodes(Icon icon, int id, int resourceId) {
        Map<String, String> node = new HashMap<>();
        // Generate consistent UUIDs based on the icon name
        node.put("uuid", UUID.nameUUIDFromBytes(icon.getName().getBytes(StandardCharsets.UTF_8)).toString());
        node.put("tooltip", icon.getName());
        node.put("filename", icon.getName());
        node.put("id", String.valueOf(id));
        node.put("resourceId", String.valueOf(resourceId));
        node.put("height", String.valueOf(icon.getHeight()));
        node.put("width", String.valueOf(icon.getWidth()));
        return node;
    }

    private Map<String, String> transformToResources(Icon icon, int id) throws IOException {
        Map<String, String> node = new HashMap<>();
        node.put("id", String.valueOf(id));
        node.put("encodedContent", icon.toXMLString());
        return node;
    }

    public void render(File file, Section section) throws IOException {
        final List<Map<String, String>> resources = new ArrayList<>(section.getIcons().size());
        final List<Map<String, String>> nodes = new ArrayList<>(section.getIcons().size());
        int id = 0;
        int resourceId = 1;
        for (Icon icon : section.getIcons()) {
            resources.add(transformToResources(icon, resourceId));
            nodes.add(transformToNodes(icon, id, resourceId));
            id++;
            resourceId++;
        }

        Map<String, Object> context = new HashMap<>();
        context.put("resources", resources);
        context.put("nodes", nodes);
        context.put("asi-version", section.getVersion().orElse("unknown"));
        context.put("asi-url", section.getUrl().orElse("unknown"));

        try (Reader in = new InputStreamReader(this.template);
             Writer out = new FileWriter(file)) {
            Mustache mustache = this.mustacheFactory.compile(in, file.getName());
            mustache.execute(out, context);
        }
    }

}

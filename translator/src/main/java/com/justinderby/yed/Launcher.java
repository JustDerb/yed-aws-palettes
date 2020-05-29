package com.justinderby.yed;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.swing.svg.JSVGComponent;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;

import javax.swing.JFrame;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Launcher {

    @Option(name = "-o", aliases = {"--out"}, required = true, usage = "Output .graphml file")
    private File out;

    @Option(name = "-v", aliases = {"--version"}, usage = "Version of AWS Simple Icons")
    private String version;

    @Option(name = "-u", aliases = {"--url"}, usage = "URL of AWS Simple Icons")
    private String url;

    @Argument(metaVar = "svgs", multiValued = true, required = true, usage = "List of SVG to transform")
    private List<File> svgs = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        new Launcher().doMain(args);
    }

    private void printErrorAndQuit(CmdLineParser parser, String message) {
        String name = new File(getClass().getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath())
                .getName();
        System.err.println();
        System.err.println(name + parser.printExample(OptionHandlerFilter.REQUIRED));
        parser.printUsage(System.err);
        System.exit(1);
    }

    public void doMain(String[] args) throws IOException {
        final CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            printErrorAndQuit(parser, e.getMessage());
            return;
        }

        if (this.svgs.isEmpty()) {
            printErrorAndQuit(parser, "No input SVGs given");
            return;
        }

        boolean unknownFiles = false;
        for (File file : this.svgs) {
            if (!file.isFile() || !file.canRead()) {
                System.err.println("No such file " + file.toString());
                unknownFiles = true;
            }
        }
        if (unknownFiles) {
            printErrorAndQuit(parser, "Found unknown files");
            return;
        }

        final List<Icon> icons = this.svgs.parallelStream()
                .map(Icon::fromFile)
                .collect(Collectors.toList());

        final Section section = new Section(icons, this.version, this.url);
        try (final InputStream templateResource = Objects.requireNonNull(
                getClass().getResourceAsStream("/templates/section.mustache"),
                "Template not found in JAR!")) {
            final MustacheSectionTemplate template = new MustacheSectionTemplate(templateResource);
            template.render(this.out, section);
        }
    }
}

package com.justinderby.yed;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.swing.svg.JSVGComponent;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Icon {

    private static final int MAX_HEIGHT_WIDTH = 60;

    private final SVGDocument document;
    private final String realName;
    private final String name;
    private Rectangle2D bounds;

    public Icon(SVGDocument document, String realName, String name) {
        this.document = Objects.requireNonNull(document);
        this.realName = realName;
        this.name = name;
    }

    public SVGGraphics2D getGraphics() {
        SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(this.document);
        // We don't need a comment
        ctx.setComment(null);
        return new SVGGraphics2D(ctx, false);
    }

    public String getRealName() {
        return this.realName;
    }

    public String getName() {
        return this.name;
    }

    private Rectangle2D getBounds() {
        if (this.bounds != null) {
            return this.bounds;
        }

        final CompletableFuture<Rectangle2D> bounds = new CompletableFuture<>();
        final JSVGComponent component = new JSVGComponent();
        try {
            component.addGVTTreeBuilderListener(new GVTTreeBuilderAdapter() {
                public void gvtBuildCompleted(GVTTreeBuilderEvent event) {
                    bounds.complete(event.getGVTRoot().getBounds());
                }
            });
            component.setSVGDocument(this.document);
            try {
                this.bounds = bounds.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        } finally {
            component.dispose();
        }
        return this.bounds;
    }

    public Dimension getDimension(int maxHeightWidth) {
        Rectangle2D bounds = getBounds();
        double scaleFactor = Math.min(
                maxHeightWidth / bounds.getWidth(),
                maxHeightWidth / bounds.getHeight());
        return new Dimension(
                (int)Math.min(Math.ceil(bounds.getWidth() * scaleFactor), maxHeightWidth),
                (int)Math.min(Math.ceil(bounds.getHeight() * scaleFactor), maxHeightWidth));
    }

    public int getHeight() {
        return (int)getDimension(MAX_HEIGHT_WIDTH).getHeight();
    }

    public int getWidth() {
        return (int)getDimension(MAX_HEIGHT_WIDTH).getWidth();
    }

    public String toXMLString() throws IOException {
        try (StringWriter writer = new StringWriter()) {
            this.getGraphics().stream(document.getRootElement(), writer, true, true);
            return writer.toString();
        }
    }

    private static String getReadableName(String name) {
        // Remove extension
        if (name.contains(".")) {
            name = name.substring(0, name.lastIndexOf("."));
        }
        return name
                .replace("light-bg", "")
                .replace("dark-bg", "")
                .replace('-', ' ')
                .replace('_', ' ')
                .trim();
    }

    public static Icon fromFile(File svg) {
        final String parser = XMLResourceDescriptor.getXMLParserClassName();
        final SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
        try {
            return new Icon(factory.createSVGDocument(svg.getAbsolutePath()), svg.getName(), getReadableName(svg.getName()));
        } catch (IOException e) {
            throw new RuntimeException("Error parsing " + svg.getAbsolutePath(), e);
        }
    }
}

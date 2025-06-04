package com.game.resource;

import java.util.*;

import javax.imageio.ImageIO;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class MapPreviewManager {
    private static final Map<String, WeakReference<BufferedImage>> previewCache = new HashMap<>();
    private static final int PREVIEW_WIDTH = 200;
    private static final int PREVIEW_HEIGHT = 150;

    public static BufferedImage getMapPreview(String previewPath) {
        WeakReference<BufferedImage> ref = previewCache.get(previewPath);
        BufferedImage preview = (ref != null) ? ref.get() : null;

        if (preview == null) {
            try {
                BufferedImage originalImage = ImageIO.read(
                    MapPreviewManager.class.getClassLoader().getResource(previewPath)
                );
                // Scale image once and cache it
                preview = createScaledPreview(originalImage);
                previewCache.put(previewPath, new WeakReference<>(preview));
            } catch (IOException e) {
                System.err.println("Error loading preview: " + previewPath);
                return null;
            }
        }
        return preview;
    }

    private static BufferedImage createScaledPreview(BufferedImage original) {
        BufferedImage scaled = new BufferedImage(
            PREVIEW_WIDTH, PREVIEW_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaled.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, PREVIEW_WIDTH, PREVIEW_HEIGHT, null);
        g2d.dispose();
        return scaled;
    }

    public static void clearCache() {
        previewCache.clear();
        System.gc();
    }
}
package com.game.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class SpriteSheetBuilder {
    private List<BufferedImage> frames;
    private int spriteWidth;
    private int spriteHeight;
    private int columns;
    private String outputPath;

    public SpriteSheetBuilder(int spriteWidth, int spriteHeight) {
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        this.frames = new ArrayList<>();
    }

    /**
     * Add a single frame to the sprite sheet
     */
    public void addFrame(String imagePath) throws IOException {
        BufferedImage frame = ImageIO.read(new File(imagePath));
        frames.add(frame);
    }

    /**
     * Add multiple frames from a directory
     */
    public void addFramesFromDirectory(String resourcePath, int frameCount) throws IOException {
        for (int i = 0; i < frameCount; i++) {
            String filePath = resourcePath + (i + 1) + ".png";
            URL url = getClass().getClassLoader().getResource(filePath);
            
            if (url == null) {
                throw new IOException("Không tìm thấy file: " + filePath);
            }

            try (InputStream is = getClass().getClassLoader().getResourceAsStream(filePath)) {
                if (is != null) {
                    BufferedImage frame = ImageIO.read(is);
                    frames.add(frame);
                    System.out.println("Added frame " + (i + 1));
                }
            }
        }
    }

    /**
     * Set the number of columns for the sprite sheet
     */
    public void setColumns(int columns) {
        this.columns = columns;
    }

    /**
     * Build and save the sprite sheet
     */
    public void buildSpriteSheet(String outputPath) throws IOException {
        if (frames.isEmpty()) {
            throw new IllegalStateException("No frames added to sprite sheet");
        }

        if (columns <= 0) {
            columns = (int) Math.ceil(Math.sqrt(frames.size()));
        }

        int rows = (int) Math.ceil(frames.size() / (double) columns);
        
        BufferedImage spriteSheet = new BufferedImage(
            spriteWidth * columns,
            spriteHeight * rows,
            BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2d = spriteSheet.createGraphics();

        for (int i = 0; i < frames.size(); i++) {
            int row = i / columns;
            int col = i % columns;
            
            BufferedImage frame = frames.get(i);
            g2d.drawImage(
                frame,
                col * spriteWidth,
                row * spriteHeight,
                spriteWidth,
                spriteHeight,
                null
            );
        }

        g2d.dispose();
        ImageIO.write(spriteSheet, "PNG", new File(outputPath));
    }

    /**
     * Usage example
     */
    public static void main(String[] args) {
        try {
            SpriteSheetBuilder builder = new SpriteSheetBuilder(100, 100);
            
            // Read from resources folder
            builder.addFramesFromDirectory("assets/Enemy/Frost_Guardian/Skill/", 10);
            
            // Save to output directory
            String outputPath = "target/buff.png";
            builder.setColumns(10);
            builder.buildSpriteSheet(outputPath);
            
            System.out.println("Sprite sheet created successfully!");
            System.out.println("Saved to: " + outputPath);
            
        } catch (IOException e) {
            System.err.println("Error creating sprite sheet: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
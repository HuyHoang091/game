package com.game.resource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class AnimationLoader {
    public static BufferedImage[] loadAnimations(String folder, int frameCount) throws IOException {
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = loadImage(folder + (i+1) + ".png");
        }
        return frames;
    }

    public static BufferedImage[] loadSkillFrames(String folderPath, int count, int width, int height) throws IOException {
        BufferedImage[] frames = new BufferedImage[count];
        for (int i = 0; i < count; i++) {
            String filePath = folderPath + "/" + (i + 1) + ".png";
            URL url = AnimationLoader.class.getClassLoader().getResource(filePath);
            if (url == null) {
                throw new IOException("Không tìm thấy file: " + filePath);
            }

            File file;
            try {
                file = new File(url.toURI());
            } catch (URISyntaxException e) {
                throw new IOException("Lỗi chuyển đổi URL thành URI: " + url, e);
            }

            if (!file.exists()) {
                throw new IOException("Không tìm thấy file: " + file.getAbsolutePath());
            }
    
            BufferedImage original = ImageIO.read(file);
            Image scaled = original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    
            Graphics2D g2d = resized.createGraphics();
            g2d.drawImage(scaled, 0, 0, null);
            g2d.dispose();
    
            frames[i] = resized;
        }
        return frames;
    }

    public static BufferedImage loadImage(String path) throws IOException {
        URL url = AnimationLoader.class.getClassLoader().getResource(path);
        if (url == null) {
            throw new IOException("Cannot find resource: " + path);
        }
        return ImageIO.read(url);
    }
}
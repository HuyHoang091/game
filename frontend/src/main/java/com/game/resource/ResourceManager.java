package com.game.resource;

import java.util.*;

import javax.imageio.ImageIO;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class ResourceManager {
    private static Map<String, BufferedImage[]> animationCache = new HashMap<>();
    
    public static BufferedImage[] getEnemyAnimation(String type, String animation) {
        String key = type + "_" + animation;
        if (!animationCache.containsKey(key)) {
            try {
                String path = "/assets/Enemy/" + type + "/" + animation + ".png";
                InputStream is = ResourceManager.class.getResourceAsStream(path);
                
                if (is == null) {
                    System.err.println("Không tìm thấy file ảnh: " + path);
                    return null;
                }

                BufferedImage spriteSheet = ImageIO.read(is);
                int frameCount = getFrameCount(animation);
                int width = spriteSheet.getWidth() / frameCount;
                int height = spriteSheet.getHeight();

                BufferedImage[] frames = new BufferedImage[frameCount];
                for (int i = 0; i < frameCount; i++) {
                    frames[i] = spriteSheet.getSubimage(i * width, 0, width, height);
                }

                animationCache.put(key, frames);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return animationCache.get(key);
    }


    public static BufferedImage[] getPlayerAnimation(String type, String animation, int count) {
        String key = type + "_" + animation;
        if (!animationCache.containsKey(key)) {
            try {
                String path = "/" + type + "/" + animation + ".png";
                int frameCount = count;
                InputStream is = ResourceManager.class.getResourceAsStream(path);
                
                if (is == null) {
                    System.err.println("Không tìm thấy file ảnh: " + path);
                    return null;
                }

                BufferedImage spriteSheet = ImageIO.read(is);
                int width = spriteSheet.getWidth() / frameCount;
                int height = spriteSheet.getHeight();
                
                BufferedImage[] frames = new BufferedImage[frameCount];
                for (int i = 0; i < frameCount; i++) {
                    frames[i] = spriteSheet.getSubimage(i * width, 0, width, height);
                }

                animationCache.put(key, frames);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return animationCache.get(key);
    }

    public static BufferedImage[] getEffectAnimation(String type, String animation) {
        String key = type + "_" + animation;
        if (!animationCache.containsKey(key)) {
            try {
                String path = type + "/" + animation + ".png";
                int frameCount = 10;
                int size = 496;
                
                animationCache.put(key, 
                    AnimationManager.loadAnimationFromSheet(path, frameCount, size, size)
                );
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return animationCache.get(key);
    }

    private static int getFrameCount(String animation) {
        return switch(animation) {
            case "runL" -> 6;
            case "runR" -> 6;
            case "attackL" -> 11;
            case "attackR" -> 11;
            case "buff" -> 10;
            case "skill" -> 16;
            case "idle" -> 9;
            case "die" -> 23;
            default -> 6;
        };
    }

    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        return resizedImage;
    }

    public static void clearAnimationCache() {
        animationCache.clear();
    }
}
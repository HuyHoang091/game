package com.game.resource;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class AnimationManager {
    public static BufferedImage[] loadAnimationFromSheet(String sheetPath, int frameCount, 
                                                       int frameWidth, int frameHeight) throws IOException {
        BufferedImage[] frames = new BufferedImage[frameCount];
        
        // Load sprite sheet
        BufferedImage spriteSheet = ImageIO.read(
            AnimationManager.class.getClassLoader().getResource(sheetPath)
        );
        
        // Extract individual frames
        for (int i = 0; i < frameCount; i++) {
            frames[i] = spriteSheet.getSubimage(
                i * frameWidth,  // x position
                0,              // y position (assuming single row)
                frameWidth,     // width of frame
                frameHeight     // height of frame
            );
        }
        
        return frames;
    }
}
package com.game.resource;

import java.util.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ResourceManager {
    private static Map<String, BufferedImage[]> animationCache = new HashMap<>();
    
    public static BufferedImage[] getEnemyAnimations(String enemyType, String animationType) {
        String key = enemyType + "_" + animationType;
        if (!animationCache.containsKey(key)) {
            try {
                BufferedImage[] frames = AnimationLoader.loadAnimations(
                    "assets/Enemy/" + enemyType + "/" + animationType + "/",
                    getFrameCount(animationType)
                );
                animationCache.put(key, frames);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return animationCache.get(key);
    }

    public static BufferedImage[] getSkillAnimations(String enemyType, String animationType) {
        String key = enemyType + "_" + animationType;
        if (!animationCache.containsKey(key)) {
            try {
                BufferedImage[] frames = AnimationLoader.loadAnimations(
                    enemyType + "/",
                    getFrameCount(animationType)
                );
                animationCache.put(key, frames);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return animationCache.get(key);
    }

    private static int getFrameCount(String animationType) {
        switch(animationType) {
            case "Left": return 6;
            case "Right": return 6;
            case "Attack/Left": return 11;
            case "Attack/Right": return 11;
            case "Buff": return 10;
            case "Skill": return 16;
            case "Idle": return 9;
            case "Die": return 23;
            default: return 10;
        }
    }
}
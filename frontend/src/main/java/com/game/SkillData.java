package com.game;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SkillData {
    public BufferedImage[] frames;
    public int damage, manaCost;
    public Color debugColor;

    public SkillData(BufferedImage[] frames, int damage, int manaCost, Color debugColor) {
        this.frames = frames;
        this.damage = damage;
        this.manaCost = manaCost;
        this.debugColor = debugColor;
    }
}
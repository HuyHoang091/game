package com.game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;

public class DamageEffect {
    private int x, y;
    private String text;
    private int duration;
    private int yOffset;
    private Color color;
    private boolean isCrit;
    
    public DamageEffect(int x, int y, Long damage, boolean isCrit) {
        this.x = x;
        this.y = y;
        this.text = damage.toString();
        this.duration = 60; // Tồn tại 60 frames
        this.yOffset = 0;
        this.isCrit = isCrit;
        this.color = isCrit ? Color.RED : Color.WHITE;
    }
    
    public void update() {
        duration--;
        yOffset -= 1; // Di chuyển lên trên
    }
    
    public void draw(Graphics g, int camX, int camY) {
        if (duration > 0) {
            // Lưu font và color hiện tại
            Font originalFont = g.getFont();
            Color originalColor = g.getColor();

            int alpha = Math.min(255, duration * 5);
            Color effectColor = new Color(
                color.getRed(),
                color.getGreen(), 
                color.getBlue(),
                alpha
            );
            
            // Set font và color cho damage effect
            g.setFont(new Font("Arial", isCrit ? Font.BOLD : Font.PLAIN, isCrit ? 20 : 16));
            g.setColor(effectColor);
            g.drawString(text, x - camX, y - camY + yOffset);

            // Reset font và color về như cũ
            g.setFont(originalFont);
            g.setColor(originalColor);
        }
    }
    
    public boolean isExpired() {
        return duration <= 0;
    }
}
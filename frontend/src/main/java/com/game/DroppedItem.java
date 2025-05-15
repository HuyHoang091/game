package com.game;

import java.awt.Image;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.AlphaComposite;
import javax.swing.ImageIcon;
import com.game.data.GameData;
import com.game.model.GameItem;
import java.net.URL;

public class DroppedItem {
    private Long itemId;
    private Long itemInstanceId;
    private int x, y;
    private boolean picked = false;
    private Image icon;
    private static final int PICKUP_RANGE = 50; // Khoảng cách để nhặt đồ

    public DroppedItem(Long itemId, int x, int y, Long itemInstanceId) {
        this.itemId = itemId;
        this.x = x;
        this.y = y;
        this.itemInstanceId = itemInstanceId;

        // Load icon with better error handling
        try {
            String iconPath = GameData.getItemIconById(itemId);
            URL iconUrl = getClass().getClassLoader().getResource(iconPath);
            if (iconUrl != null) {
                this.icon = new ImageIcon(iconUrl).getImage();
            } else {
                // Load default icon if item icon not found
                URL defaultUrl = getClass().getClassLoader().getResource("assets/items/default.png");
                if (defaultUrl != null) {
                    this.icon = new ImageIcon(defaultUrl).getImage();
                } else {
                    System.err.println("Cannot load default icon!");
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading item icon: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isNear(Player player) {
        int px = player.getX() + player.getCurrentImage().getWidth() / 2;
        int py = player.getY() + player.getCurrentImage().getHeight() / 2;
        int distance = (int) Math.sqrt(Math.pow(px - (x + 16), 2) + Math.pow(py - (y + 16), 2));
        return distance <= PICKUP_RANGE;
    }

    public void draw(Graphics g, int camX, int camY) {
        if (!picked) {
            // Vẽ item
            g.drawImage(icon, x - camX, y - camY, 32, 32, null);
            
            // Hiệu ứng phát sáng
            Graphics2D g2d = (Graphics2D) g;
            float alpha = (float)(0.3f + 0.2f * Math.sin(System.currentTimeMillis() / 300.0));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.setColor(new Color(255, 255, 255, 50));
            g2d.fillOval(x - camX - 5, y - camY - 5, 42, 42);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    // Getters
    public Long getItemId() { return itemId; }
    public Long getItemInstanceId() { return itemInstanceId; }
    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isPicked() { return picked; }
    public void setPicked(boolean picked) { this.picked = picked; }
}

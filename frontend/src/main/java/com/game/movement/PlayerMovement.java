package com.game.movement;

import java.awt.image.BufferedImage;

public class PlayerMovement {
    private int x, y;
    private int speed = 3;
    private boolean up, down, left, right, tocbien;
    private boolean wasMoving = false;
    private String direction = "right";
    private int dodgeCooldown = 250;
    private BufferedImage collisionImage;
    private final int tileSize = 64;

    public PlayerMovement(int x, int y, BufferedImage collisionImage) {
        this.x = x;
        this.y = y;
        this.collisionImage = collisionImage;
    }

    public void update() {
        if(dodgeCooldown > 0) {
            dodgeCooldown--;
        }
    }

    public void move(int currentWidth, int currentHeight) {
        int newX = x;
        int newY = y;
        boolean moving = false;

        if (up) {
            newY -= speed;
            moving = true;
            if (tocbien && dodgeCooldown <= 0) {
                newY -= 10;
                dodgeCooldown = 250;
            }
        }
        if (down) {
            newY += speed;
            moving = true;
            if (tocbien && dodgeCooldown <= 0) {
                newY += 10;
                dodgeCooldown = 250;
            }
        }
        if (left) {
            newX -= speed;
            direction = "left";
            moving = true;
            if (tocbien && dodgeCooldown <= 0) {
                newX -= 10;
                dodgeCooldown = 250;
            }
        }
        if (right) {
            newX += speed;
            direction = "right";
            moving = true;
            if (tocbien && dodgeCooldown <= 0) {
                newX += 10;
                dodgeCooldown = 250;
            }
        }
        if (tocbien && dodgeCooldown <= 0) {
            newX += 10;
            dodgeCooldown = 250;
        }


        newX = Math.max(0, Math.min(newX, collisionImage.getWidth() * tileSize - currentWidth));
        newY = Math.max(0, Math.min(newY, collisionImage.getHeight() * tileSize - currentHeight));

        if (!isBlocked(newX, y, currentWidth, currentHeight)) {
            x = newX;
        }
        if (!isBlocked(x, newY, currentWidth, currentHeight)) {
            y = newY;
        }

        wasMoving = moving;
    }
    
    public boolean isBlocked(int x, int y, int width, int height) {
        int footHeight = height / 10;
    
        for (int dx = 0; dx <= width; dx += tileSize / 2) {
            for (int dy = height - footHeight; dy < height; dy += tileSize / 2) {
                int checkX = x + dx;
                int checkY = y + dy;
    
                if (checkX < 0 || checkX >= collisionImage.getWidth() || checkY < 0 || checkY >= collisionImage.getHeight()) {
                    return true;
                }
    
                int pixel = collisionImage.getRGB(checkX, checkY);
                int alpha = (pixel >> 24) & 0xFF;
                if (alpha != 0) return true;
            }
        }
    
        return false;
    }

    // Setters for movement flags
    public void setUp(boolean up) { this.up = up; }
    public void setDown(boolean down) { this.down = down; }
    public void setLeft(boolean left) { this.left = left; }
    public void setRight(boolean right) { this.right = right; }
    public void setTocbien(boolean tocbien) { this.tocbien = tocbien; }
    public void setSpeed(int speed) { this.speed = speed; }

    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public String getDirection() { return direction; }
    public boolean isMoving() { return up || down || left || right; }
    public boolean wasMoving() { return wasMoving; }
}
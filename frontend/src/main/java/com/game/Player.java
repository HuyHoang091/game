package com.game;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.util.ArrayList;
import java.awt.geom.AffineTransform;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import com.game.*;
import com.game.data.GameData;
import com.game.model.*;
import java.util.List;

public class Player {
    int x, y;
    int speed = 2;

    int skillX, skillY; // Tọa độ của vùng sát thương kỹ năng

    public int maxHealth = 10000;
    public int health = 10000;
    public int maxmana = 5000;
    public int mana = 5000;

    BufferedImage collisionImage;

    BufferedImage[] upFrames, downFrames, leftFrames, rightFrames, skillFrames;
    BufferedImage currentImage;
    int frameIndex = 0;
    int frameCount = 8;
    int frameDelay = 10, frameTick = 0;

    final int tileSize = 64;

    private ArrayList<SkillData> skillList = new ArrayList<>();

    boolean up, down, left, right;
    BufferedImage[] skillEffectFrames; // Các khung hình chuyển động khi dùng kỹ năng
    int skillEffectFrameIndex = 0; // Chỉ số khung hình hiện tại
    int skillEffectFrameDelay = 5; // Độ trễ giữa các khung hình
    int skillEffectFrameTick = 0; // Bộ đếm thời gian cho khung hình
    boolean isUsingSkill = false; // Trạng thái sử dụng kỹ năng
    int skillEffectDuration = 0; // Thời gian tồn tại của hiệu ứng kỹ năng
    BufferedImage skillEffectImage; // Hình ảnh hiệu ứng kỹ năng

    ArrayList<Enemy> enemies = new ArrayList<>();

    GameWindow gameWindow;

    Long characterId;

    public Player(int x, int y,
                  BufferedImage[] up, BufferedImage[] down,
                  BufferedImage[] left, BufferedImage[] right,
                  BufferedImage[] skillEffectFrames,
                  BufferedImage collisionImage, Long characterId) {
        this.x = x;
        this.y = y;
        this.upFrames = up;
        this.downFrames = down;
        this.leftFrames = left;
        this.rightFrames = right;
        this.skillFrames = skillFrames;
        this.skillEffectFrames = skillEffectFrames; 
        this.currentImage = down[0];
        this.collisionImage = collisionImage;
        this.characterId = characterId;            

        System.out.println("Collision image size: " + collisionImage.getWidth() + "x" + collisionImage.getHeight());
    }

    public void update() {
        int newX = x;
        int newY = y;
        boolean moving = false;
    
        if (up) {
            newY -= speed;
            animate(upFrames);
            moving = true;
        }
        if (down) {
            newY += speed;
            animate(downFrames);
            moving = true;
        }
        if (left) {
            newX -= speed;
            animate(leftFrames);
            moving = true;
        }
        if (right) {
            newX += speed;
            animate(rightFrames);
            moving = true;
        }
    
        if (!moving) {
            frameIndex = 0;
            frameTick = 0;
        }

        newX = Math.max(0, Math.min(newX, collisionImage.getWidth() * 64 - currentImage.getWidth()));
        newY = Math.max(0, Math.min(newY, collisionImage.getHeight() * 64 - currentImage.getHeight()));

        // Giảm thời gian tồn tại của hiệu ứng kỹ năng
        if (isUsingSkill) {
            if (skillEffectFrames != null && skillEffectFrames.length > 0) {
                animate(skillEffectFrames); // Chạy hoạt ảnh kỹ năng
            }

            skillEffectDuration--;
            if (skillEffectDuration <= 0) {
                isUsingSkill = false;
            } else {
                // Cập nhật khung hình hiệu ứng kỹ năng
                skillEffectFrameTick++;
                if (skillEffectFrameTick >= skillEffectFrameDelay) {
                    skillEffectFrameTick = 0;
                    skillEffectFrameIndex = (skillEffectFrameIndex + 1) % frameCount;
                }
            }
        }

        if (!isBlocked(newX, y)) {
            x = newX;
        }
        if (!isBlocked(x, newY)) {
            y = newY;
        }

        // Kiểm tra và nhặt đồ
        if (GameData.droppedItems != null) {
            for (DroppedItem item : new ArrayList<>(GameData.droppedItems)) {
                if (!item.isPicked() && item.isNear(this)) {
                    pickUpItem(item);
                }
            }
        }
    }

    private void pickUpItem(DroppedItem item) {
        if (item == null || GameData.getItemById(item.getItemId()) == null) {
            System.err.println("Invalid item pickup attempt");
            return;
        }

        try {
            item.setPicked(true);

            GameInventory newInventory = new GameInventory();
            newInventory.setCharacterId(characterId);
            newInventory.setItemId(item.getItemId());
            newInventory.setItemInstanceId(item.getItemInstanceId()); // Set the instance ID
            newInventory.setQuantity(1);
            newInventory.setEquipped(false);

            List<GameInventory> currentInventory = (GameData.inventory != null) ? 
                new ArrayList<>(GameData.inventory) : new ArrayList<>();
            
            currentInventory.add(newInventory);
            GameData.inventory = currentInventory;

            GameData.droppedItems.remove(item);
                
            // Print item stats when picked up
            GameItemInstance instance = GameData.itemInstance.stream()
                .filter(i -> i.getId().equals(item.getItemInstanceId()))
                .findFirst()
                .orElse(null);
                
            if (instance != null) {
                System.out.println("Picked up item with stats: " +
                    "ATK:" + instance.getAtk() +
                    " DEF:" + instance.getDef() +
                    " HP:" + instance.getHp() +
                    " MP:" + instance.getMp() +
                    " Crit Rate:" + String.format("%.1f%%", instance.getCritRate() * 100) +
                    " Crit DMG:" + String.format("%.1f%%", instance.getCritDmg() * 100));
            }
        } catch (Exception e) {
            System.err.println("Error adding item to inventory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void animate(BufferedImage[] frames) {
        frameTick++;
        if (frameTick >= frameDelay) {
            frameTick = 0;
            frameIndex = (frameIndex + 1) % frameCount;
        }
        currentImage = frames[frameIndex];
    }

    public void draw(Graphics g, int camX, int camY) {
        Graphics2D g2d = (Graphics2D) g;
        if (isUsingSkill && skillEffectFrames != null) {
            // Vẽ hoạt ảnh khi dung kỹ năng
            BufferedImage effectFrame = skillEffectFrames[skillEffectFrameIndex];
            if (left) {
                // Lật ảnh theo trục ngang khi hướng trái
                AffineTransform transform = new AffineTransform();
                transform.translate(x - camX + effectFrame.getWidth(), y - camY); // Dịch chuyển ảnh
                transform.scale(-1, 1); // Lật ảnh theo trục X
                g2d.drawImage(effectFrame, transform, null);
            } else {
                // Vẽ bình thường khi hướng phải
                g2d.drawImage(effectFrame, x - camX, y - camY, null);
            }
        } else {
            // Vẽ hình ảnh hiện tại của Player
            g.drawImage(currentImage, x - camX, y - camY, null);
        }
    }    

    public void setDirection(int keyCode, boolean pressed) {
        switch (keyCode) {
            case KeyEvent.VK_W -> up = pressed;
            case KeyEvent.VK_S -> down = pressed;
            case KeyEvent.VK_A -> left = pressed;
            case KeyEvent.VK_D -> right = pressed;
            case KeyEvent.VK_SHIFT -> speed = pressed ? 3 : 2; // Tăng tốc độ khi nhấn Shift
            case KeyEvent.VK_1 -> {
                gameWindow.getInstance().showSettings("Game");  
            }
            case KeyEvent.VK_2 -> {
                gameWindow.getInstance().showInventory("Game");
            }
        }
    }

    public SkillEffect castSkill(int skillIndex, ArrayList<Enemy> enemies, int offset) {
        if (skillIndex < 0 || skillIndex >= skillList.size()) return null;
    
        SkillData skill = skillList.get(skillIndex);
        if (mana < skill.manaCost || skill.frames == null) return null;
    
        useMana(skill.manaCost);
        isUsingSkill = true;
        skillEffectDuration = 30;
    
        String direction = "right";

        BufferedImage frame = skill.frames[0];
        int skillWidth = frame.getWidth();
        int skillHeight = frame.getHeight();

        int centerX = x + currentImage.getWidth() / 2;
        int centerY = y + currentImage.getHeight() / 2;

        int damageBoxX = centerX + offset;
        int damageBoxY = centerY - skillHeight / 2;

        if (right) {
            direction = "right";
            damageBoxX = centerX + offset;
            damageBoxY = centerY - skillHeight / 2;
        } else if (left) {
            direction = "left";
            damageBoxX = centerX - skillWidth - offset;
            damageBoxY = centerY - skillHeight / 2;
        } else if (up) {
            direction = "up";
            damageBoxX = centerX - skillWidth / 2;
            damageBoxY = centerY - skillHeight - offset;
        } else if (down) {
            direction = "down";
            damageBoxX = centerX - skillWidth / 2;
            damageBoxY = centerY + offset;
        }


        Rectangle skillBox = new Rectangle(damageBoxX, damageBoxY, skillWidth, skillHeight);

        switch (direction) {
            case "right" -> skillBox = new Rectangle(centerX + 20, centerY - skillHeight / 2, skillWidth * 2 / 3, skillHeight);
            case "left"  -> skillBox = new Rectangle(centerX - skillWidth * 2 / 3 - 20, centerY - skillHeight / 2, skillWidth * 2 / 3, skillHeight);
            case "up"    -> skillBox = new Rectangle(centerX - skillWidth / 2, centerY - skillHeight * 2 / 3 - 20, skillWidth, skillHeight * 2 / 3);
            case "down"  -> skillBox = new Rectangle(centerX - skillWidth / 2, centerY + 20, skillWidth, skillHeight * 2 / 3);
            default      -> skillBox = new Rectangle(centerX + 20, centerY - skillHeight / 2, skillWidth, skillHeight); // fallback
        }
    
        for (Enemy enemy : enemies) {
            Rectangle enemyBox = new Rectangle(enemy.x, enemy.y, enemy.width, enemy.height);
            if (skillBox.intersects(enemyBox)) {
                enemy.takeDamage(skill.damage);
            }
        }
                
    
        return new SkillEffect(damageBoxX, damageBoxY, 30, skill.damage, skill.frames, direction, skillBox, skill.debugColor);
    }

    public boolean isBlocked(int x, int y) {
        int width = currentImage.getWidth();
        int height = currentImage.getHeight();

        int footHeight = height / 3;
    
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
    
    public void addSkill(SkillData skill) {
        skillList.add(skill);
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health < 0) health = 0;
    }

    public void useMana(int amount) {
        mana -= amount;
        if (mana < 0) mana = 0;
    }

    public void setHp(int hp) {
        health = hp;
    }

    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }    

    public BufferedImage getCurrentImage() {
        return currentImage;
    }
}
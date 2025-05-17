package com.game;

import java.awt.Graphics;
import java.awt.Color;
import java.util.Random;
import com.game.DroppedItem;
import com.game.data.GameData;
import com.game.model.*;
import java.util.ArrayList;
import com.game.DamageEffect;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Enemy {
    int x, y;
    Long health;
    Long maxHealth;
    int speed = 1;
    int width, height;
    int attackCooldown = 3; // Thời gian giữa các đợt tấn công
    Long attackDamage = 5L;
    Long monsterId;
    int def = 100; // Base defense value

    private BufferedImage[] upFrames, downFrames, leftFrames, rightFrames, attackFrames;
    private BufferedImage currentImage;
    private int frameIndex = 0;
    private int normalFrameCount = 6;  // For movement animations
    private int attackFrameCount = 11; // For attack animation
    private int frameDelay = 3, frameTick = 0;
    private String direction = "down";
    private boolean isAttacking = false;
    private boolean isAttackAnimationComplete = false;
    private boolean hasDealDamage = false; // Thêm biến để kiểm tra đã gây damage chưa
    private int attackAnimationDuration = 30;
    private int attackAnimationTick = 0;
    private Rectangle attackArea;

    private ArrayList<DamageEffect> damageEffects = new ArrayList<>();

    public Enemy(int x, int y, int width, int height, Long health, Long monsterId,
                BufferedImage[] up, BufferedImage[] down, 
                BufferedImage[] left, BufferedImage[] right,
                BufferedImage[] attack) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.health = health;
        this.monsterId = monsterId;
        this.maxHealth = health; // Lưu lại máu tối đa

        this.upFrames = up;
        this.downFrames = down;
        this.leftFrames = left;
        this.rightFrames = right;
        this.attackFrames = attack;
        this.currentImage = down[0];

        // Set defense based on monster level from GameData
        GameMonster monster = GameData.monster.stream()
            .filter(m -> m.getId().equals(monsterId))
            .findFirst()
            .orElse(null);
            
        if (monster != null) {
            // Scale defense with monster level
            this.def = 100 + (monster.getLevel() * 10);
        }
    }

    private void animate(BufferedImage[] frames) {
        frameTick++;
        if (frameTick >= frameDelay) {
            frameTick = 0;
            int maxFrames = (frames == attackFrames) ? attackFrameCount : normalFrameCount;
            
            if (frames == attackFrames) {
                if (frameIndex < attackFrameCount - 1) {
                    frameIndex++;
                } else {
                    isAttackAnimationComplete = true;
                    frameIndex = 0;
                }
            } else {
                frameIndex = (frameIndex + 1) % maxFrames;
            }
        }
        currentImage = frames[frameIndex];
    }

    public void update(Player player) {
        int newX = x;
        int newY = y;
        // AI thông minh hơn: Di chuyển ngẫu nhiên khi không tấn công
        if (attackCooldown > 0) {
            attackCooldown--; // Đếm ngược thời gian hồi chiêu tấn công
        }

        // Tính tọa độ tâm của Enemy và Player
        int enemyCenterX = x;
        int enemyCenterY = y;
        int playerCenterX = player.getX() + player.getWidth()/2;
        int playerCenterY = player.getY() + player.getHeight()/2;

        // Quái sẽ có hành động dựa trên khoảng cách với người chơi
        int distanceX = Math.abs(playerCenterX - enemyCenterX);
        int distanceY = Math.abs(playerCenterY - enemyCenterY);

        newX = Math.max(0, Math.min(newX, player.collisionImage.getWidth() * 64 - width));
        newY = Math.max(0, Math.min(newY, player.collisionImage.getHeight() * 64 - height));

        if (!player.isBlocked(newX, y, 20, 50)) x = newX;
        if (!player.isBlocked(x, newY, 20, 50)) y = newY;

        if (distanceX < 150 && distanceY < 150) {
            // Xác định hướng di chuyển dựa vào tâm
            if (Math.abs(distanceX) > Math.abs(distanceY)) {
                direction = playerCenterX > enemyCenterX ? "right" : "left";
            } else {
                direction = playerCenterY > enemyCenterY ? "down" : "up";
            }

            // Nếu đủ gần để tấn công
            if (distanceX < 30 && distanceY < 30 && attackCooldown == 0) {
                isAttacking = true;
                hasDealDamage = false;
                attackAnimationTick = attackAnimationDuration;
                attackCooldown = 3;
                
                // Xác định vùng gây sát thương từ tâm sprite
                int attackRange = 140;
                int centerX = x; // Tâm sprite
                int centerY = y; // Điều chỉnh theo chiều cao thực của sprite
                switch (direction) {
                    case "right" -> attackArea = new Rectangle(centerX, centerY - height/2, 
                                                            attackRange, height);
                    case "left" -> attackArea = new Rectangle(centerX - attackRange, centerY - height/2, 
                                                            attackRange, height);
                    case "down" -> attackArea = new Rectangle(centerX - width/2, centerY, 
                                                            width, attackRange);
                    case "up" -> attackArea = new Rectangle(centerX - width/2, centerY - attackRange, 
                                                        width, attackRange);
                }
            }
        }
        // Xử lý animation tấn công
        if (isAttacking) {
            animate(attackFrames);
            attackAnimationTick--;

            // Kiểm tra va chạm và gây sát thương khi animation kết thúc
            if (!hasDealDamage && isAttackAnimationComplete) {
                Rectangle playerBox = new Rectangle(player.getX(), player.getY(), 
                                                 player.getWidth(), player.getHeight());
                if (attackArea != null && attackArea.intersects(playerBox)) {
                    player.takeDamage(attackDamage);
                    hasDealDamage = true;
                }
            }
            
            // Kết thúc animation tấn công
            if (isAttackAnimationComplete) {
                isAttacking = false;
                isAttackAnimationComplete = false;
                frameIndex = 0;
                attackArea = null;
            }
        } else if (distanceX < 150 && distanceY < 150) {
            moveTowardPlayerSmartly(player);
        }

        // Cập nhật hiệu ứng damage
        for (int i = damageEffects.size() - 1; i >= 0; i--) {
            DamageEffect effect = damageEffects.get(i);
            effect.update();
            if (effect.isExpired()) {
                damageEffects.remove(i);
            }
        }
    }

    private void moveTowardPlayerSmartly(Player player) {
        int offset = 2;

        int playerX = player.getX() + player.getWidth() / 2;
        int playerY = player.getY() + player.getHeight() / 2;

        int dx = playerX - x;
        int dy = playerY - y;

        boolean moved = false;

        // Ưu tiên trục có khoảng cách lớn hơn
        if (Math.abs(dx) > Math.abs(dy)) {
            // Ưu tiên di chuyển theo X
            if (dx > 0 && !player.isBlocked(x + speed, y, 20, 50)) {
                x += speed;
                direction = "right";
                animate(rightFrames);
                moved = true;
            } else if (dx < 0 && !player.isBlocked(x - speed, y, 20, 50)) {
                x -= speed;
                direction = "left";
                animate(leftFrames);
                moved = true;
            }
            // Nếu bị chặn, thử đi dọc để "men theo tường"
            else if (dy > 0 && !player.isBlocked(x, y + offset, 20, 50)) {
                y += offset;
            } else if (dy < 0 && !player.isBlocked(x, y - offset, 20, 50)) {
                y -= offset;
            }
        } else {
            // Ưu tiên di chuyển theo Y
            if (dy > 0 && !player.isBlocked(x, y + speed, 20, 50)) {
                y += speed;
                direction = "down";
                animate(downFrames);
                moved = true;
            } else if (dy < 0 && !player.isBlocked(x, y - speed, 20, 50)) {
                y -= speed;
                direction = "up";
                animate(upFrames);
                moved = true;
            }
            // Nếu bị chặn, thử đi ngang để "men theo tường"
            else if (dx > 0 && !player.isBlocked(x + offset, y, 20, 50)) {
                x += offset;
            } else if (dx < 0 && !player.isBlocked(x - offset, y, 20, 50)) {
                x -= offset;
            }
        }

        // Nếu vẫn không di chuyển được
        if (!moved) {
            // moveSmartly(player); // fallback
        }
    }


    public void draw(Graphics g, int camX, int camY) {
        Graphics2D g2d = (Graphics2D) g;

        // Vẽ sprite của quái - căn giữa theo chiều ngang, chân ở dưới
        if (currentImage != null) {
            int drawX = x - width / 2 - camX;
            int drawY = y - height / 2 - camY;

            g2d.drawImage(currentImage, drawX, drawY, width, height, null);

            // Debug: vẽ vị trí chân đứng của quái
            g2d.setColor(Color.RED);
            g2d.fillOval(x - camX - 2, y - camY - 2, 4, 4);
            // Vẽ thanh máu - căn giữa theo sprite mới
            g.setColor(Color.RED);
            int healthBarWidth = (int)(health * 0.5);
            int healthBarX = drawX + (width - healthBarWidth) / 2;
            int healthBarY = drawY - 10; // Đặt thanh máu phía trên sprite
            g.fillRect(healthBarX, healthBarY, healthBarWidth, 5);

            // Vẽ chỉ số máu - căn giữa theo sprite mới
            g.setColor(Color.WHITE);
            String healthText = health + "/" + maxHealth;
            java.awt.FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(healthText);
            int textX = drawX + (width - textWidth) / 2;
            int textY = healthBarY - 5;
            g.drawString(healthText, textX, textY);
        }

        // Vẽ hiệu ứng damage
        for (DamageEffect effect : damageEffects) {
            effect.draw(g, camX, camY);
        }

        // Debug: Vẽ vùng tấn công
        if (isAttacking && attackArea != null) {
            g.setColor(new Color(255, 0, 0, 100));
            g.fillRect(attackArea.x - camX, attackArea.y - camY, 
                      attackArea.width, attackArea.height);
        }

        g.setColor(new Color(0, 255, 0, 100));
        g.fillRect(x - camX, y - camY, 50, 70/3);

        drawCollisionBox(g2d, x - camX, y - camY);
    }

    public void drawCollisionBox(Graphics2D g, int x, int y) {
        int width = 20;
        int height = 50; //quái lớn(150px)
        // int height = 20; //quái nhỏ
        int footHeight = height / 10;

        g.setColor(new Color(255, 0, 0, 100)); // Đỏ trong suốt
        g.drawRect(x, y + height - footHeight, width, footHeight);
    }

    public void takeDamage(Long damage, boolean isCrit) {
        health -= damage;

        // Thêm hiệu ứng damage mới
        damageEffects.add(new DamageEffect(
            x + width/2, 
            y - 10,
            damage,
            isCrit
        ));

        if (health <= 0) {
            health = 0L;
            // Initialize the list only if it's null
            if (GameData.droppedItems == null) {
                GameData.droppedItems = new ArrayList<>();
            }
            // Drop item when monster dies
            drop(x, y, monsterId);
        }
    }

    public boolean isDead() {
        return health <= 0;
    }

    // Quái hồi máu (kỹ năng đặc biệt)
    public void heal() {
        if (health < maxHealth) {
            health += 5; // Hồi 5 máu
            if (health > maxHealth) {
                health = maxHealth; // Đảm bảo không vượt quá máu tối đa
            }
        }
    }

    public void drop(int x, int y, Long monsterId) {
        if (GameData.monsterDrop == null) return;

        try {
            GameMonster monster = GameData.monster.stream()
                .filter(m -> m.getId().equals(monsterId))
                .findFirst()
                .orElse(null);
                
            if (monster == null) return;

            for (GameMonsterDrop drop : GameData.monsterDrop) {
                if (drop.getMonsterId().equals(monsterId)) {
                    if (Math.random() <= drop.getDropRate()) {
                        if (GameData.getItemById(drop.getItemId()) != null) {
                            int offsetX = (int)(Math.random() * 40) - 20;
                            int offsetY = (int)(Math.random() * 40) - 20;
                            
                            // Pass monster level when creating item instance
                            GameItemInstance itemInstance = GameData.createItemInstance(
                                drop.getItemId(), 
                                monster.getLevel()
                            );
                            
                            DroppedItem droppedItem = new DroppedItem(
                                drop.getItemId(),
                                x + offsetX,
                                y + offsetY,
                                itemInstance.getId()
                            );
                            GameData.droppedItems.add(droppedItem);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing monster drop: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

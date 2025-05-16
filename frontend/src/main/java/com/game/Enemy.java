package com.game;

import java.awt.Graphics;
import java.awt.Color;
import java.util.Random;
import com.game.DroppedItem;
import com.game.data.GameData;
import com.game.model.*;
import java.util.ArrayList;
import com.game.DamageEffect;

public class Enemy {
    int x, y;
    Long health;
    Long maxHealth;
    int speed = 1;
    int width, height;
    int attackCooldown = 0; // Thời gian giữa các đợt tấn công
    Long attackDamage = 5L;
    Long monsterId;
    int def = 100; // Base defense value

    private ArrayList<DamageEffect> damageEffects = new ArrayList<>();

    public Enemy(int x, int y, int width, int height, Long health, Long monsterId) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.health = health;
        this.monsterId = monsterId;
        this.maxHealth = health; // Lưu lại máu tối đa

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

    public void update(Player player) {
        int newX = x;
        int newY = y;
        // AI thông minh hơn: Di chuyển ngẫu nhiên khi không tấn công
        if (attackCooldown > 0) {
            attackCooldown--; // Đếm ngược thời gian hồi chiêu tấn công
        }

        // Quái sẽ có hành động dựa trên khoảng cách với người chơi
        int distanceX = Math.abs(player.getX() - x);
        int distanceY = Math.abs(player.getY() - y);

        newX = Math.max(0, Math.min(newX, player.collisionImage.getWidth() * 64 - width));
        newY = Math.max(0, Math.min(newY, player.collisionImage.getHeight() * 64 - height));

        if (!player.isBlocked(newX, y)) x = newX;
        if (!player.isBlocked(x, newY)) y = newY;

        if (distanceX < 150 && distanceY < 150) {
            // Nếu đủ gần để tấn công
            if (distanceX < 30 && distanceY < 30 && attackCooldown == 0) {
                attack(player);
                attackCooldown = 100;
            } else {
                // Đuổi theo người chơi
                if (player.getX() > x && !player.isBlocked(x + speed, y)) x += speed;
                else if (player.getX() < x && !player.isBlocked(x - speed, y)) x -= speed;
    
                if (player.getY() > y && !player.isBlocked(x, y + speed)) y += speed;
                else if (player.getY() < y && !player.isBlocked(x, y - speed)) y -= speed;
            }
        } else {
            // moveSmartly(player);
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

    // Di chuyển thông minh: quái có thể tránh tường hoặc người chơi
    private void moveSmartly(Player player) {
        Random rand = new Random();
        int dir = rand.nextInt(4); // 0: lên, 1: xuống, 2: trái, 3: phải
    
        switch (dir) {
            case 0: // lên
                if (!player.isBlocked(x, y - speed)) y -= speed;
                break;
            case 1: // xuống
                if (!player.isBlocked(x, y + speed)) y += speed;
                break;
            case 2: // trái
                if (!player.isBlocked(x - speed, y)) x -= speed;
                break;
            case 3: // phải
                if (!player.isBlocked(x + speed, y)) x += speed;
                break;
        }
    }
    
    public void attack(Player player) {
        // Kiểm tra va chạm giữa quái vật và người chơi
        int distanceX = Math.abs(player.getX() - x);
        int distanceY = Math.abs(player.getY() - y);

        if (distanceX < 30 && distanceY < 30) { // Nếu khoảng cách nhỏ hơn kích thước quái vật
            player.takeDamage(attackDamage); // Gây sát thương cho người chơi
        }
    }

    public void draw(Graphics g, int camX, int camY) {
        g.setColor(Color.GREEN);
        g.fillRect(x - camX, y - camY, 30, 30);

        // Vẽ thanh máu
        g.setColor(Color.RED);
        g.fillRect(x - camX, y - camY - 10, (int) (health * 0.5), 5); // Thanh máu phụ thuộc vào tỉ lệ máu hiện tại

        // Vẽ chỉ số máu
        g.setColor(Color.WHITE);
        g.drawString(health + "/" + maxHealth, x - camX, y - camY - 15);

        // Vẽ hiệu ứng damage
        for (DamageEffect effect : damageEffects) {
            effect.draw(g, camX, camY);
        }
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

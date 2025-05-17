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
import com.game.ui.SkillTreeDialog;
import javax.swing.*;

public class Player extends JComponent {
    int x, y;
    int speed = 2;

    int skillX, skillY; // Tọa độ của vùng sát thương kỹ năng

    public int basemaxHealth = 500;
    public Long maxHealth;
    public Long health;
    public int basemaxmana = 5000;
    public Long maxmana;
    public Long mana;
    public int baseatk = 20;
    public Long atk;
    public int basedef = 2;
    public Long def;
    public double basecritRate = 0.0001;
    public double critRate;
    public double basecritDmg = 1.2;
    public double critDmg;

    public BufferedImage collisionImage;

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

        ChisoGoc(); // Khởi tạo chỉ số gốc
        ChiSoTB(); // Cập nhật chỉ số dựa trên trang bị
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

        if (!isBlocked(newX, y, currentImage.getWidth(), currentImage.getHeight())) {
            x = newX;
        }
        if (!isBlocked(x, newY , currentImage.getWidth(), currentImage.getHeight())) {
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

        // Vẽ hitbox người chơi để debug
        // g.setColor(new Color(0, 255, 0, 100));
        // g.fillRect(x - camX, y - camY, 50, 70);

        g.setColor(new Color(0, 255, 0, 100));
        g.fillRect(x - camX, y - camY, currentImage.getWidth(), currentImage.getHeight()/3);

        // Vẽ vùng sát thương kỹ năng
        if (isUsingSkill && skillEffectDuration > 0) {
            g.setColor(Color.RED);
            g.fillRect(skillX - camX, skillY - camY, skillEffectFrames[0].getWidth(), skillEffectFrames[0].getHeight());
        }

        drawCollisionBox(g2d, x - camX, y - camY); // Vẽ hitbox
    }    

    public void setDirection(int keyCode, boolean pressed) {
        switch (keyCode) {
            case KeyEvent.VK_W -> up = pressed;
            case KeyEvent.VK_S -> down = pressed;
            case KeyEvent.VK_A -> left = pressed;
            case KeyEvent.VK_D -> right = pressed;
            case KeyEvent.VK_SHIFT -> speed = pressed ? 3 : 2; // Tăng tốc độ khi nhấn Shift
            case KeyEvent.VK_N -> {
                gameWindow.getInstance().showSettings("Game");  
            }
            case KeyEvent.VK_B -> {
                gameWindow.getInstance().showInventory("Game");
            }
            case KeyEvent.VK_K -> {
                // Mở cửa sổ kỹ năng khi nhấn K
                SkillTreeDialog dialog = new SkillTreeDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    characterId
                );
                dialog.setVisible(true);
            }
        }
    }

    public SkillEffect castSkill(int skillIndex, ArrayList<Enemy> enemies, int offset) {
        if (skillIndex < 0 || skillIndex >= skillList.size()) return null;
        if (enemies.isEmpty()) return null;

        SkillData skill = skillList.get(skillIndex);
        if (mana < skill.manaCost || skill.frames == null) return null;

        useMana(skill.manaCost);
        isUsingSkill = true;
        skillEffectDuration = 30;

        // Tìm enemy gần nhất
        Enemy nearestEnemy = null;
        double minDistance = Double.MAX_VALUE;
        int playerCenterX = x + currentImage.getWidth() / 2;
        int playerCenterY = y + currentImage.getHeight() / 2;

        for (Enemy enemy : new ArrayList<>(enemies)) {
            int enemyCenterX = enemy.x + enemy.width / 2;
            int enemyCenterY = enemy.y + enemy.height / 2;
            double distance = Math.sqrt(
                Math.pow(enemyCenterX - playerCenterX, 2) + 
                Math.pow(enemyCenterY - playerCenterY, 2)
            );
            if (distance < minDistance) {
                minDistance = distance;
                nearestEnemy = enemy;
            }
        }

        if (nearestEnemy == null) return null;

        // Tính góc giữa player và enemy
        int enemyCenterX = nearestEnemy.x + nearestEnemy.width / 2;
        int enemyCenterY = nearestEnemy.y + nearestEnemy.height / 2;
        double angle = Math.atan2(enemyCenterY - playerCenterY, enemyCenterX - playerCenterX);

        // Chuyển góc thành hướng
        String direction;
        if (angle >= -Math.PI/4 && angle < Math.PI/4) direction = "right";
        else if (angle >= Math.PI/4 && angle < 3*Math.PI/4) direction = "down";
        else if (angle >= -3*Math.PI/4 && angle < -Math.PI/4) direction = "up";
        else direction = "left";

        // Tạo vùng sát thương theo hướng quái
        BufferedImage frame = skill.frames[0];
        int skillWidth = frame.getWidth();
        int skillHeight = frame.getHeight();
        
        // Đặt vùng sát thương gần player hơn
        int centerX = x + currentImage.getWidth() / 2;
        int centerY = y + currentImage.getHeight() / 2;

        int skillX = centerX + offset;
        int skillY = centerY - skillHeight / 2;

        switch (direction) {
            case "right" -> {
                skillX = centerX + offset;
                skillY = centerY - skillHeight / 2;
            }
            case "left" -> {
                skillX = centerX - skillWidth - offset;
                skillY = centerY - skillHeight / 2;
            }
            case "up" -> {
                skillX = centerX - skillWidth / 2;
                skillY = centerY - skillHeight - offset;
            }
            case "down" -> {
                skillX = centerX - skillWidth / 2;
                skillY = centerY + offset;
            }
            default -> {
                skillX = centerX + offset;
                skillY = centerY - skillHeight / 2;
            }
        }

        Rectangle skillBox = new Rectangle(skillX, skillY, skillWidth, skillHeight);

        switch (direction) {
            case "right" -> skillBox = new Rectangle(centerX + 20, centerY - skillHeight / 2, skillWidth * 2 / 3, skillHeight);
            case "left"  -> skillBox = new Rectangle(centerX - skillWidth * 2 / 3 - 20, centerY - skillHeight / 2, skillWidth * 2 / 3, skillHeight);
            case "up"    -> skillBox = new Rectangle(centerX - skillWidth / 2, centerY - skillHeight * 2 / 3 - 20, skillWidth, skillHeight * 2 / 3);
            case "down"  -> skillBox = new Rectangle(centerX - skillWidth / 2, centerY + 20, skillWidth, skillHeight * 2 / 3);
            default      -> skillBox = new Rectangle(centerX + 20, centerY - skillHeight / 2, skillWidth, skillHeight); // fallback
        }

        // Xử lý sát thương cho tất cả quái trong vùng sát thương
        for (Enemy enemy : new ArrayList<>(enemies)) {
            Rectangle enemyBox = new Rectangle(enemy.x, enemy.y, enemy.width, enemy.height);
            if (skillBox.intersects(enemyBox)) {
                // Tính DEF_Reduction
                double defReduction = Math.min(0.5, enemy.def / (enemy.def + 10000.0));
                
                // Tính xem có crit không
                boolean isCrit = Math.random() < critRate;
                
                // Tính sát thương:
                // - Nếu là đánh thường (skillIndex == 0)
                // - Nếu là skill (skillIndex > 0)
                Long damage;
                if (skillIndex == 0) {
                    if (isCrit) {
                        damage = (long) (atk * critDmg * (1 - defReduction));
                    } else {
                        damage = (long) (atk * (1 - defReduction));
                    }
                } else {
                    if (isCrit) {
                        damage = (long) (atk * skill.damage * critDmg * (1 - defReduction));
                    } else {
                        damage = (long) (atk * skill.damage * (1 - defReduction));
                    }
                }

                // Gửi thêm thông tin crit
                enemy.takeDamage(damage, isCrit);

                // Áp dụng sát thương và in thông tin
                // enemy.takeDamage(damage);
                
                // In thông tin combat để debug
                System.out.printf("Hit: %s | ATK: %d | DEF_Reduction: %.2f | Crit: %b | Damage: %d%n",
                    skillIndex == 0 ? "Normal" : "Skill",
                    atk,
                    defReduction,
                    isCrit,
                    damage
                );
            }
        }
                
    
        return new SkillEffect(skillX, skillY, 30, skill.frames, direction, skillBox, skill.debugColor);
    }

    public boolean isBlocked(int x, int y, int width, int height) {
        // int width = currentImage.getWidth();
        // int height = currentImage.getHeight();

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

    public void drawCollisionBox(Graphics2D g, int x, int y) {
        int width = currentImage.getWidth();
        int height = currentImage.getHeight();
        int footHeight = height / 10;

        g.setColor(new Color(255, 0, 0, 100)); // Đỏ trong suốt
        g.drawRect(x, y + height - footHeight, width, footHeight);
    }
    
    public void addSkill(SkillData skill) {
        skillList.add(skill);
    }

    public void takeDamage(Long damage) {
        health -= damage;
        if (health < 0) health = 0L;
    }

    public void ChisoGoc() {
        GameCharacter character = GameData.character.stream()
            .filter(c -> c.getId().equals(characterId))
            .findFirst()
            .orElse(null);

        if (character == null) {
            System.out.println("Character not found: " + characterId);
            return;
        }

        int level = character.getLevel();
        double levelMultiplier = Math.pow(1.1, level); // tăng 10% mỗi cấp
        maxHealth = (long)(basemaxHealth * levelMultiplier);
        maxmana = (long)(basemaxmana * levelMultiplier);
        health = maxHealth;
        mana = maxmana;
        atk = (long)(baseatk * levelMultiplier);
        def = (long)(basedef * levelMultiplier);
        // Giới hạn Crit Rate và Crit Dmg
        critRate = Math.min(1.0, basecritRate * levelMultiplier); 
        critDmg = Math.min(12.0, basecritDmg + 0.1 * level); 
    }

    public void ChiSoTB() {
        for (GameInventory item : GameData.inventory) {
            if (item.isEquipped()) {
                GameItemInstance instance = GameData.itemInstance.stream()
                    .filter(i -> i.getId().equals(item.getItemInstanceId()))
                    .findFirst()
                    .orElse(null);
                if (instance != null) {
                    maxHealth += instance.getHp();
                    maxmana += instance.getMp();
                    health += instance.getHp();
                    mana += instance.getMp();
                    atk += instance.getAtk();
                    def += instance.getDef();
                    critRate += instance.getCritRate();
                    critDmg += instance.getCritDmg();
                }
            }
        }
    }

    public void useMana(int amount) {
        mana -= amount;
        if (mana < 0) mana = 0L;
    }

    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }    

    @Override
    public int getWidth() {
        return currentImage.getWidth();
    }

    @Override
    public int getHeight() {
        return currentImage.getHeight();
    }

    public BufferedImage getCurrentImage() {
        return currentImage;
    }
}
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
import com.game.stats.*;

public class Player extends JComponent {
    int x, y;
    int speed = 3;
    int skillX, skillY; // Tọa độ của vùng sát thương kỹ năng

    // region Chỉ số Player
    // public int basemaxHealth = 500;
    // public Long maxHealth;
    // public Long health;
    // public int basemaxmana = 5000;
    // public Long maxmana;
    // public Long mana;
    // public int baseatk = 20;
    // public Long atk;
    // public int basedef = 2;
    // public Long def;
    // public double basecritRate = 0.0001;
    // public double critRate;
    // public double basecritDmg = 1.2;
    // public double critDmg;
    // private int currentExp = 0;
    // private int expBase = 100;
    // endrehion

    public BufferedImage collisionImage;

    // region Animation
    BufferedImage[] upFrames, downFrames, leftFrames, rightFrames;
    BufferedImage currentImage;
    BufferedImage[] skillEffectFramesL;
    BufferedImage[] skillEffectFramesR;
    BufferedImage skillEffectImage; // Hình ảnh hiệu ứng kỹ năng
    BufferedImage[] idleFrames;
    int frameIndex = 0;
    int skillEffectFrameIndex = 0; // Chỉ số khung hình hiện tại
    int frameCount = 8;
    int skillFrameCount = 9;
    int idleFrameCount = 6;
    int frameDelay = 10, frameTick = 0;
    int skillEffectFrameDelay = 5; // Độ trễ giữa các khung hình
    int skillEffectFrameTick = 0; // Bộ đếm thời gian cho khung hình
    int skillEffectDuration = 0; // Thời gian tồn tại của hiệu ứng kỹ năng
    boolean isUsingSkill = false; // Trạng thái sử dụng kỹ năng
    private boolean isIdle = false;
    private ArrayList<SkillData> skillList = new ArrayList<>();
    // endregion

    // region Di chuyển
    boolean up, down, left, right, tocbien;
    private boolean wasMoving = false;
    private String direction = "right";
    private int dodgeCooldown = 250;
    // endregion
    
    // region Set Đồ
    private int boXuyenGiap = 0, boChiMang = 0, boMauTrau = 0, boHutMau = 0;
    private Double khangST = 0.0, RateHoiMau = 0.25;
    private boolean HoiMau = false, CMHoiMau = false, BeMau = false;
    private int TGHoiMau = 60;
    // endregion

    // region Khác
    ArrayList<Enemy> enemies = new ArrayList<>();
    GameWindow gameWindow;
    public PlayerStats stats;
    Long characterId;
    final int tileSize = 64;
    // endregion

    public Player(int x, int y,
                  BufferedImage[] up, BufferedImage[] down,
                  BufferedImage[] left, BufferedImage[] right,
                  BufferedImage[] skillEffectFramesL,
                  BufferedImage[] skillEffectFramesR,
                  BufferedImage[] idle,
                  BufferedImage collisionImage, Long characterId) {
        this.x = x;
        this.y = y;
        this.upFrames = up;
        this.downFrames = down;
        this.leftFrames = left;
        this.rightFrames = right;
        this.skillEffectFramesL = skillEffectFramesL; 
        this.skillEffectFramesR = skillEffectFramesR; 
        this.currentImage = down[0];
        this.collisionImage = collisionImage;
        this.characterId = characterId;     
        this.idleFrames = idle;   
        this.stats = new PlayerStats(characterId);    

        // ChisoGoc();
        // ChiSoTB();
        // SetDo();

        if (boMauTrau == 6) {
            stats.setMaxHealth(stats.getMaxHealth()*2);
            stats.setHealth(stats.getMaxHealth());
            khangST = 0.99;
            HoiMau = true;
        } else if (boMauTrau >= 3) {
            stats.setMaxHealth((long)(stats.getMaxHealth()*1.5));
            stats.setHealth(stats.getMaxHealth());
            khangST = 0.3;
            HoiMau = false;
        } else if (boMauTrau >= 1) {
            stats.setMaxHealth((long)(stats.getMaxHealth()*1.2));
            stats.setHealth(stats.getMaxHealth());
            khangST = 0.1;
            HoiMau = false;
        } else {
            HoiMau = false;
        }

        if (boChiMang == 6) {
            stats.setCritRate(stats.getCritRate() + 0.25);
            stats.setCritDmg(stats.getCritDmg() + 0.5);
            if (stats.getCritRate() > 1.0) {
                stats.setCritDmg(stats.getCritDmg() + (stats.getCritRate() - 1.0)/2);
                stats.setCritRate(1.0);
            }
            CMHoiMau = true;
        } else if (boChiMang >= 3) {
            stats.setCritRate(stats.getCritRate() + 0.15);
            stats.setCritDmg(stats.getCritDmg() + 0.3);
            CMHoiMau = false;
        } else if (boChiMang >= 1) {
            stats.setCritRate(stats.getCritRate() + 0.05);
            stats.setCritDmg(stats.getCritDmg() + 0.1);
            CMHoiMau = false;
        } else {
            CMHoiMau = false;
        }
    }

    public void update() {
        int newX = x;
        int newY = y;
        boolean moving = false;

        if(dodgeCooldown > 0) {
            dodgeCooldown --;
        }
    
        if (up) {
            newY -= speed;
            animate(upFrames);
            moving = true;
            if (tocbien && dodgeCooldown <= 0) {
                newY -= 10;
            }
        }
        if (down) {
            newY += speed;
            animate(downFrames);
            moving = true;
            if (tocbien && dodgeCooldown <= 0) {
                newY += 10;
            }
        }
        if (left) {
            newX -= speed;
            animate(leftFrames);
            moving = true;
            if (tocbien && dodgeCooldown <= 0) {
                newX -= 10;
            }
        }
        if (right) {
            newX += speed;
            animate(rightFrames);
            moving = true;
            if (tocbien && dodgeCooldown <= 0) {
                newX += 10;
            }
        }
        if (tocbien && dodgeCooldown <= 0) {
            newX += 10;
        }
    
        if (!moving) {
            if (wasMoving) {
                frameIndex = 0;
                frameTick = 0;
            }
        } else {
            isIdle = false;
        }

        newX = Math.max(0, Math.min(newX, collisionImage.getWidth() * 64 - currentImage.getWidth()));
        newY = Math.max(0, Math.min(newY, collisionImage.getHeight() * 64 - currentImage.getHeight()));

        // Giảm thời gian tồn tại của hiệu ứng kỹ năng
        if (isUsingSkill) {
            if (skillEffectFramesR != null && skillEffectFramesR.length > 0) {
                if (direction.equals("left")){
                    animate(skillEffectFramesL);
                } else if (direction.equals("right")){
                    animate(skillEffectFramesR);
                } else if (direction.equals("up")){
                    animate(skillEffectFramesR);
                } else if (direction.equals("down")){
                    animate(skillEffectFramesL);
                }
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

        // Kiểm tra đụng tường
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

        // Buff Set hồi máu
        if (boMauTrau == 6 && HoiMau) {
            TGHoiMau --;
            if (TGHoiMau <= 0) {
                stats.setHealth(stats.getHealth() + (long)(stats.getMaxHealth()*0.01));
                TGHoiMau = 60;
            }
        }

        // Buff Set hút máu (1 lần / trận)
        if (boHutMau == 6 && BeMau) {
            if (stats.getHealth() <= 0) {
                stats.setHealth(stats.getMaxHealth());
                BeMau = false;
            }
        }

        // Kiểm tra chạy hoạt ảnh đứng yên
        if (!moving && !isUsingSkill) {
            isIdle = true;
            animate(idleFrames);
        } else {
            isIdle = false;
        }

        wasMoving = moving;
    }

    // region nhặt đồ
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
    // endregion

    // region Chạy chuyển động
    private void animate(BufferedImage[] frames) {
        frameTick++;
        if (frameTick >= frameDelay) {
            frameTick = 0;
            if (frames == null || frames.length == 0) {
                return;
            }
            int maxFrames;
            if (frames == idleFrames) {
                maxFrames = Math.min(idleFrameCount, frames.length);
            } else if (frames == skillEffectFramesL || frames == skillEffectFramesR) {
                maxFrames = Math.min(skillFrameCount, frames.length);
            } else {
                maxFrames = Math.min(frameCount, frames.length);
            }
            
            frameIndex = (frameIndex + 1) % maxFrames;

            // Safety check before accessing frame
            if (frameIndex >= 0 && frameIndex < frames.length) {
                currentImage = frames[frameIndex];
            }
        }
    }
    // endregion

    public void draw(Graphics g, int camX, int camY) {
        Graphics2D g2d = (Graphics2D) g;
        g.drawImage(currentImage, x - camX, y - camY, null);

        // Vẽ hitbox người chơi để debug
        // g.setColor(new Color(0, 255, 0, 100));
        // g.fillRect(x - camX, y - camY, 50, 70);

        g.setColor(new Color(0, 255, 0, 100));
        g.fillRect(x - camX, y - camY, currentImage.getWidth(), currentImage.getHeight()/3);

        // Vẽ vùng sát thương kỹ năng
        if (isUsingSkill && skillEffectDuration > 0) {
            g.setColor(Color.RED);
            g.fillRect(skillX - camX, skillY - camY, skillEffectFramesR[0].getWidth(), skillEffectFramesR[0].getHeight());
        }

        drawCollisionBox(g2d, x - camX, y - camY); // Vẽ hitbox
    }    

    // Input
    public void setDirection(int keyCode, boolean pressed) {
        switch (keyCode) {
            case KeyEvent.VK_W -> up = pressed;
            case KeyEvent.VK_S -> down = pressed;
            case KeyEvent.VK_A -> left = pressed;
            case KeyEvent.VK_D -> right = pressed;
            case KeyEvent.VK_SHIFT -> speed = pressed ? 4 : 3; // Tăng tốc độ khi nhấn Shift
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
            case KeyEvent.VK_G -> tocbien = pressed;
        }
    }
    // endregion

    // region Chạy hiệu ứng đánh thường, Skill, gây dame
    public SkillEffect castSkill(int skillIndex, ArrayList<Enemy> enemies, int offset) {
        if (skillIndex < 0 || skillIndex >= skillList.size()) return null;
        if (enemies.isEmpty()) return null;

        SkillData skill = skillList.get(skillIndex);
        if (stats.getMana() < skill.manaCost || skill.frames == null) return null;

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
                double defReduction = Math.min(0.5, enemy.def / (enemy.def + 10000.0));

                if (boXuyenGiap == 6) {
                    defReduction -= 1.1;
                    stats.setCritRate(Math.min(0.5, stats.getCritRate()));
                } else if (boXuyenGiap >= 3) {
                    defReduction -= 0.3;
                } else if (boXuyenGiap >= 1) {
                    defReduction -= 0.1;
                }
                
                boolean isCrit = Math.random() < stats.getCritRate();
                
                Long damage;
                if (skillIndex == 0) {
                    if (isCrit) {
                        damage = (long) (stats.getAtk() * stats.getCritDmg() * (1 - defReduction));
                    } else {
                        damage = (long) (stats.getAtk() * (1 - defReduction));
                    }
                } else {
                    if (isCrit) {
                        damage = (long) (stats.getAtk() * skill.damage * stats.getCritDmg() * (1 - defReduction));
                    } else {
                        damage = (long) (stats.getAtk() * skill.damage * (1 - defReduction));
                    }
                }

                if (CMHoiMau && isCrit) {
                    boolean hoimau = Math.random() < RateHoiMau;
                    if (hoimau) {
                        stats.setHealth((long)Math.min(stats.getMaxHealth()*0.15, damage));
                    }
                }

                if (boHutMau == 6) {
                    if (skillIndex == 0) {
                        stats.setHealth((long)Math.min(damage*0.1,stats.getMaxHealth()*0.05));
                    } else {
                        stats.setHealth((long)Math.min(damage*0.2,stats.getMaxHealth()*0.3));
                    }
                } else if (boHutMau >= 3) {
                    if (skillIndex == 0) {
                        stats.setHealth((long)Math.min(damage*0.05,stats.getMaxHealth()*0.05));
                    } else {
                        stats.setHealth((long)Math.min(damage*0.1,stats.getMaxHealth()*0.3));
                    }
                } else if (boHutMau >= 1) {
                    if (skillIndex == 0) {
                        stats.setHealth((long)Math.min(damage*0.02,stats.getMaxHealth()*0.05));
                    } else {
                        stats.setHealth((long)Math.min(damage*0.05,stats.getMaxHealth()*0.3));
                    }
                }

                // Gửi thêm thông tin crit
                enemy.takeDamage(damage, isCrit);
                
                // In thông tin combat để debug
                System.out.printf("Hit: %s | ATK: %d | DEF_Reduction: %.2f | Crit: %b | Damage: %d%n",
                    skillIndex == 0 ? "Normal" : "Skill",
                    stats.getAtk(),
                    defReduction,
                    isCrit,
                    damage
                );
            }
        }
                
    
        return new SkillEffect(skillX, skillY, 30, skill.frames, direction, skillBox, skill.debugColor);
    }
    // endregion

    // region Kiểm tra chạm tường
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
    // endregion

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
        stats.takeDamage(damage, khangST);
    }

    private void SetDo() {
        boChiMang = 0; boHutMau = 0; boMauTrau = 0; boXuyenGiap = 0;
        for (GameInventory inventory : GameData.inventory) {
            if (inventory.getCharacterId().equals(characterId)) {
                if (inventory.isEquipped()){
                    for (GameItem item : GameData.item){
                        if (item.getId().equals(inventory.getItemId())){
                            for (GameThuocTinh thuocTinh : GameData.thuoctinh){
                                if(item.getThuoctinhId().equals(thuocTinh.getId())){
                                    if (thuocTinh.getName().equals("bộ xuyên giáp")){
                                        boXuyenGiap += 1;
                                    } else if (thuocTinh.getName().equals("bộ máu trâu")){
                                        boMauTrau += 1;
                                    } else if (thuocTinh.getName().equals("bộ chí mạng")){
                                        boChiMang += 1;
                                    } else if (thuocTinh.getName().equals("bộ hút máu")){
                                        boHutMau += 1;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    // endregion

    // region Exp người chơi
    // Calculate exp needed for next level
    private int calculateExpNeeded(int level) {
        return (int)(stats.getExpBase() * Math.pow(level, 1.5));
    }

    // Add method to gain exp when killing monsters
    public void gainExp(Long monsterId) {
        GameMonster monster = GameData.monster.stream()
            .filter(m -> m.getId().equals(monsterId))
            .findFirst()
            .orElse(null);

        if (monster == null) return;

        // Calculate monster exp reward based on level
        int monsterLevel = monster.getLevel();
        int baseExpReward = monster.getExpReward();
        int scaledExpReward = (int)(baseExpReward * Math.pow(monsterLevel, 1.2));
        
        stats.setCurrentExp(stats.getCurrentExp() + scaledExpReward);

        System.out.println("Gained " + scaledExpReward + " exp!");
        
        // Check for level up
        checkLevelUp();
    }

    // Modified level up check
    private void checkLevelUp() {
        GameCharacter character = GameData.character.stream()
            .filter(c -> c.getId().equals(characterId))
            .findFirst()
            .orElse(null);

        if (character == null) return;

        int currentLevel = character.getLevel();
        int expNeeded = calculateExpNeeded(currentLevel);

        while (stats.getCurrentExp() >= expNeeded) {
            // Level up!
            currentLevel++;
            character.setLevel(currentLevel);
            
            // Update exp values
            stats.setCurrentExp(stats.getCurrentExp() - expNeeded);
            expNeeded = calculateExpNeeded(currentLevel);

            // Recalculate stats with new level
            stats.ChiSoGocGL();
            stats.ChiSoTB();
            
            System.out.println("Level Up! Now level " + currentLevel);
            System.out.println("Next level requires " + expNeeded + " exp");
        }

        // Update character in GameData
        for (int i = 0; i < GameData.character.size(); i++) {
            if (GameData.character.get(i).getId().equals(characterId)) {
                GameData.character.set(i, character);
                break;
            }
        }
    }
    // endregion

    public void useMana(int amount) {
        stats.useMana(amount);
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
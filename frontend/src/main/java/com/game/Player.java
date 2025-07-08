package com.game;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.util.ArrayList;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import com.game.audio.SoundEffectPlayer;
import com.game.core.KeyBindingConfig;
import com.game.data.GameData;
import com.game.model.*;
import java.util.List;

import com.game.ui.GamePanel;
import com.game.ui.SkillTreeDialog;
import javax.swing.*;
import com.game.stats.*;
import com.game.movement.*;

public class Player extends JComponent {
    int skillX, skillY; // Tọa độ của vùng sát thương kỹ năng

    public BufferedImage collisionImage;

    // region Animation
    BufferedImage[] upFrames, downFrames, leftFrames, rightFrames;
    BufferedImage currentImage;
    BufferedImage[] skillEffectFramesL;
    BufferedImage[] skillEffectFramesR;
    BufferedImage skillEffectImage; // Hình ảnh hiệu ứng kỹ năng
    BufferedImage[] idleFrames;
    BufferedImage[] deathFrame;
    int frameIndex = 0;
    int skillEffectFrameIndex = 0; // Chỉ số khung hình hiện tại
    int frameCount = 8;
    int skillFrameCount = 9;
    int idleFrameCount = 6;
    int deathFrameCount = 12;
    int frameDelay = 10, frameTick = 0;
    int skillEffectFrameDelay = 5; // Độ trễ giữa các khung hình
    int skillEffectFrameTick = 0; // Bộ đếm thời gian cho khung hình
    int skillEffectDuration = 0; // Thời gian tồn tại của hiệu ứng kỹ năng
    boolean isUsingSkill = false; // Trạng thái sử dụng kỹ năng
    boolean isDying = false, isReadyToDie = false, isDieAnimationComplete = false;
    private ArrayList<SkillData> skillList = new ArrayList<>();
    // endregion

    // region Di chuyển
    private String direction = "right";
    // endregion
    
    // region Set Đồ
    private int boXuyenGiap = 0, boChiMang = 0, boMauTrau = 0, boHutMau = 0;
    private Double khangST = 0.0, RateHoiMau = 0.25;
    private boolean HoiMau = false, CMHoiMau = false, BeMau = false;
    private int TGHoiMau = 60;
    // endregion

    // region Khác
    ArrayList<Enemy> enemies = new ArrayList<>();
    public PlayerStats stats;
    public PlayerMovement movement;
    Long characterId;
    public boolean isDie = false;
    // final int tileSize = 64;
    // endregion

    public Player(int x, int y,
                  BufferedImage[] up, BufferedImage[] down,
                  BufferedImage[] left, BufferedImage[] right,
                  BufferedImage[] skillEffectFramesL,
                  BufferedImage[] skillEffectFramesR,
                  BufferedImage[] idle,
                  BufferedImage[] die,
                  BufferedImage collisionImage, Long characterId) {
        // this.x = x;
        // this.y = y;
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
        this.deathFrame = die;
        this.stats = new PlayerStats(characterId);    
        this.movement = new PlayerMovement(x, y, collisionImage);

        // ChisoGoc();
        // ChiSoTB();
        SetDo();

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
        if (stats.getHealth() <= 0L && !isDying) {
            stats.setHealth(1L); // Keep alive temporarily for animation
            isUsingSkill = false;
            isDying = true;
            isReadyToDie = false; // Reset flag
        }

        // Handle death animation
        if (isDying) {
            animate(deathFrame);
            // If animation complete and dying, mark ready to die
            if (isDieAnimationComplete) {
                isDying = false;
                isReadyToDie = true;
            }
            return; // Skip all other updates while dying
        }

        // Actually die on next frame
        if (isReadyToDie) {
            stats.setHealth(0L);
            isDie = true;
            isReadyToDie = false;
            return;
        }

        if (isDie) return;

        movement.update();
        movement.move(currentImage.getWidth(), currentImage.getHeight());
        
        if (isUsingSkill) {
            if (direction.equals("left")){
                animate(skillEffectFramesL);
            } else if (direction.equals("right")){
                animate(skillEffectFramesR);
            } else if (direction.equals("up")){
                animate(skillEffectFramesR);
            } else if (direction.equals("down")){
                animate(skillEffectFramesL);
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

            return;
        }

        // Update animation based on movement
        if (movement.isMoving()) {
            if (movement.getDirection().equals("right")) {
                animate(rightFrames);
            } else if (movement.getDirection().equals("left")) {
                animate(leftFrames);
            } else if (movement.getDirection().equals("up")) {
                animate(upFrames);
            } else if (movement.getDirection().equals("down")) {
                animate(downFrames);
            }
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
        if (!movement.isMoving() && !isUsingSkill) {
            animate(idleFrames);
        }

        // wasMoving = moving;
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

            Long maxId = 0L;
            if (GameData.inventory != null && !GameData.inventory.isEmpty()) {
                for (GameInventory inv : GameData.inventory) {
                    if (inv.getId() != null && inv.getId() > maxId) {
                        maxId = inv.getId();
                    }
                }
            }
            newInventory.setId(maxId + 1);

            List<GameInventory> currentInventory = (GameData.inventory != null) ? 
                new ArrayList<>(GameData.inventory) : new ArrayList<>();
            
            currentInventory.add(newInventory);
            GameData.inventory = currentInventory;

            GameData.droppedItems.remove(item);
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
            } else if (frames == deathFrame) {
                maxFrames = Math.min(deathFrameCount, frames.length);
            } else {
                maxFrames = Math.min(frameCount, frames.length);
            }

            if (frames == deathFrame) {
                if (frameIndex < deathFrameCount - 1) {
                    frameIndex++;
                } else {
                    isDieAnimationComplete = true;
                }
            } else {
                frameIndex = (frameIndex + 1) % maxFrames;
            }

            // Safety check before accessing frame
            if (frameIndex >= 0 && frameIndex < frames.length) {
                currentImage = frames[frameIndex];
            }
        }
    }
    // endregion

    public void draw(Graphics g, int camX, int camY) {
        Graphics2D g2d = (Graphics2D) g;
        g.drawImage(currentImage, movement.getX() - camX, movement.getY() - camY, null);

        // Vẽ hitbox người chơi để debug
        // g.setColor(new Color(0, 255, 0, 100));
        // g.fillRect(x - camX, y - camY, 50, 70);

        // g.setColor(new Color(0, 255, 0, 100));
        // g.fillRect(movement.getX() - camX, movement.getY() - camY, currentImage.getWidth(), currentImage.getHeight()/3);

        // Vẽ vùng sát thương kỹ năng
        if (isUsingSkill && skillEffectDuration > 0) {
            g.setColor(Color.RED);
            g.fillRect(skillX - camX, skillY - camY, skillEffectFramesR[0].getWidth(), skillEffectFramesR[0].getHeight());
        }

        drawCollisionBox(g2d, movement.getX() - camX, movement.getY() - camY); // Vẽ hitbox
    }    

    // Input
    public void setDirection(int keyCode, boolean pressed) {
        if (keyCode == KeyBindingConfig.getKey("Up")) {
            movement.setUp(pressed);
        }
        if (keyCode == KeyBindingConfig.getKey("Down")) {
            movement.setDown(pressed);
        }
        if (keyCode == KeyBindingConfig.getKey("Left")) {
            movement.setLeft(pressed);
        }
        if (keyCode == KeyBindingConfig.getKey("Right")) {
            movement.setRight(pressed);
        }
        if (keyCode == KeyBindingConfig.getKey("Speed")) {
            movement.setSpeed(pressed ? 4 : 3);
        }
        if (keyCode == KeyBindingConfig.getKey("Open Setting")) {
            GamePanel.getInstance().setPaused(true);
            GameWindow.getInstance().showSettings("Game");  
        }
        if (keyCode == KeyBindingConfig.getKey("Open Inventory")) {
            GamePanel.getInstance().setPaused(true);
            GameWindow.getInstance().showInventory("Game");  
        }
        if (keyCode == KeyBindingConfig.getKey("Open Skill Tree")) {
            GamePanel.getInstance().setPaused(true);
            SkillTreeDialog dialog = new SkillTreeDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                characterId
            );
            dialog.setVisible(true); 
        }
        if (keyCode == KeyBindingConfig.getKey("Dodge")) {
            movement.setTocbien(pressed);
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
        int playerCenterX = movement.getX() + currentImage.getWidth() / 2;
        int playerCenterY = movement.getY() + currentImage.getHeight() / 2;

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

        SoundEffectPlayer soundEffectPlayer = new SoundEffectPlayer();
        soundEffectPlayer.playSound("assets/player_attack.wav");

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
        int centerX = movement.getX() + currentImage.getWidth() / 2;
        int centerY = movement.getY() + currentImage.getHeight() / 2;

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

                if (damage > 99999999) {
                    damage = 99999999L; // Giới hạn sát thương tối đa
                }

                // Gửi thêm thông tin crit
                enemy.takeDamage(damage, isCrit);
            }
        }
                
    
        return new SkillEffect(skillX, skillY, 30, skill.frames, direction, skillBox, skill.debugColor);
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
                                if(item.getThuoctinh().equals(thuocTinh.getId())){
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
        
        GameData.character.get(0).setExp(GameData.character.get(0).getExp() + scaledExpReward);

        gainGold(monsterId);

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

        while (GameData.character.get(0).getExp()  >= expNeeded) {
            // Level up!
            currentLevel++;
            character.setLevel(currentLevel);
            
            // Update exp values
            GameData.character.get(0).setExp(GameData.character.get(0).getExp() - expNeeded);
            expNeeded = calculateExpNeeded(currentLevel);

            // Recalculate stats with new level
            stats.ChiSoGocGL();
            stats.ChiSoTB();
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

    public void gainGold(Long monsterId) {
        GameMonster monster = GameData.monster.stream()
            .filter(m -> m.getId().equals(monsterId))
            .findFirst()
            .orElse(null);

        if (monster == null) return;

        int baseGold = 10;
        int monsterLevel = monster.getLevel();
        int goldReward = (int) (baseGold * Math.pow(monsterLevel, 1.2)); // tăng dần theo cấp

        GameCharacter character = GameData.character.stream()
            .filter(c -> c.getId().equals(characterId))
            .findFirst()
            .orElse(null);

        if (character != null) {
            character.setGold(character.getGold() + goldReward);
        }
    }

    public void useMana(int amount) {
        stats.useMana(amount);
    }

    public boolean isDead() {
        return stats.getHealth() <= 0;
    }

    public int getX() {
        return movement.getX();
    }
    
    public int getY() {
        return movement.getY();
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
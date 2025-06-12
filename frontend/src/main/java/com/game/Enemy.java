package com.game;

import java.awt.Graphics;
import java.awt.Color;
import com.game.*;
import com.game.data.GameData;
import com.game.model.*;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.*;
import com.game.ui.GamePanel;

public class Enemy {
    int x, y;
    public Long basemaxHealth;
    public Long maxHealth;
    public Long health;
    public int baseatk = 5;
    public Long atk;
    public int basedef = 2;
    public Long def;
    int speed = 2;
    int width, height;
    int attackCooldown = 60; // Thời gian giữa các đợt tấn công
    Long monsterId;
    private BufferedImage[] deathFrames;  // Add this to constructor parameters
    private boolean isDying = false;
    private boolean isReadyToDie = false;
    private int deathFrameCount = 23; 
    private boolean isDieAnimationComplete = false;

    private BufferedImage[] upFrames, downFrames, leftFrames, rightFrames, attackFramesR, attackFramesL;
    private BufferedImage currentImage;
    private int frameIndex = 0;
    private int normalFrameCount = 6;  // For movement animations
    private int attackFrameCount = 11; // For attack animation
    private int buffFrameCount = 10;
    private int skillFrameCount = 16;
    private int frameDelay = 3, frameTick = 0;
    private String direction = "down";
    private boolean isAttacking = false;
    private boolean isAttackAnimationComplete = false;
    private boolean hasDealDamage = false; // Thêm biến để kiểm tra đã gây damage chưa
    private int attackAnimationDuration = 30;
    private Rectangle attackArea;

    private ArrayList<DamageEffect> damageEffects = new ArrayList<>();
    private List<Enemy> pendingEnemies = new ArrayList<>();
    private Random random = new Random();

    private BufferedImage[] idleFrames;
    private boolean isIdle = false;
    private int idleFrameCount = 9;

    private BufferedImage[] buffSkill;
    private BufferedImage[] targetSkill;
    private BufferedImage[] explosion;

    //Buff
    private boolean isUsingBuffSkill = false;
    private int buffCooldown = 300;
    private int buffDuration = 0;
    private boolean buffActive = false;
    private boolean isBuffAnimationComplete = false;

    private BufferedImage[] bufferEffectFrames;
    private int buffEffectFrameIndex = 0;
    private int buffEffectFrameDelay = 5;
    private int buffEffectTick = 0;
    //

    //Skill
    private boolean isUsingTargetSkill = false;
    private boolean isTargetSkillComplete = false;
    private int targetSkillCooldown = 600; // 10 seconds
    private boolean isShowingExplosion = false;
    private int explosionFrameIndex = 0;
    private int explosionFrameCount = 8;
    private int explosionX, explosionY;
    private boolean hasDealtExplosionDamage = false;
    //

    private int explosionDamageDelay = 24; // 0.4 seconds at 60fps
    private int explosionDamageTimer = 0;

    private int level;
    private String name, type;
    private boolean SkillTH = false;

    GamePanel gamePanel;
    private boolean trieuhoi;

    public Enemy(int x, int y, int width, int height, Long health, Long monsterId,
                BufferedImage[] up, BufferedImage[] down, 
                BufferedImage[] left, BufferedImage[] right,
                BufferedImage[] attackL,
                BufferedImage[] attackR, 
                BufferedImage[] buffSkill,
                BufferedImage[] buffEffect,
                BufferedImage[] targetSkill,
                BufferedImage[] explosion,
                BufferedImage[] idle,
                BufferedImage[] die,
                boolean trieuhoi) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.basemaxHealth = health;
        this.monsterId = monsterId;
        this.maxHealth = health; // Lưu lại máu tối đa

        this.upFrames = up;
        this.downFrames = down;
        this.leftFrames = left;
        this.rightFrames = right;
        this.attackFramesL = attackL;
        this.attackFramesR = attackR;
        this.currentImage = down[0];

        this.buffSkill = buffSkill;
        this.targetSkill = targetSkill;
        this.explosion = explosion;
        this.bufferEffectFrames = buffEffect;

        this.idleFrames = idle;

        this.deathFrames = die;

        this.trieuhoi = trieuhoi;

        for(GameMonster monster : GameData.monster) {
            if(monster.getId().equals(monsterId)){
                this.level = GameWindow.getInstance().getLevel();
                this.name = monster.getName();
                this.type = monster.getBehavior();
            }
        }
        ChisoGoc();
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

        if (distanceX < 150 && distanceY < 150) {
            // Xác định hướng di chuyển dựa vào tâm
            if (Math.abs(distanceX) > Math.abs(distanceY)) {
                direction = playerCenterX > enemyCenterX ? "right" : "left";
            } else {
                direction = playerCenterY > enemyCenterY ? "down" : "up";
            }

            // Nếu đủ gần để tấn công
            if (distanceX < 100 && distanceY < 100 && attackCooldown == 0) {
                isAttacking = true;
                hasDealDamage = false;
                attackCooldown = 60;
                
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
        if (isAttacking && !isDying && !isUsingTargetSkill) {
            // animate(attackFrames);
            if(direction.equals("right")){
                animate(attackFramesR);
            } else if (direction.equals("left")){
                animate(attackFramesL);
            } else if (direction.equals("up")) {
                animate(attackFramesR);
            } else if (direction.equals("down")) {
                animate(attackFramesR);
            }

            // Kiểm tra va chạm và gây sát thương khi animation kết thúc
            if (!hasDealDamage && isAttackAnimationComplete) {
                Rectangle playerBox = new Rectangle(player.getX(), player.getY(), 
                                                 player.getWidth(), player.getHeight());
                if (attackArea != null && attackArea.intersects(playerBox)) {
                    Long damage = calculateLimitedDamage(player, "đánh thường");
                    player.takeDamage(damage);
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
        } else if (isUsingBuffSkill && !isDying && !isUsingTargetSkill) {
            animate(buffSkill);
            
            if (isBuffAnimationComplete) {
                if(level < 50) {
                    buffDuration = 3100;
                    buffCooldown = 3000;
                    heal();
                } else if (level < 90) {
                    buffDuration = 9000;
                    buffCooldown = 12000;
                    heal();
                    if (type.equals("cận chiến")) {
                        speed = 3;
                    } else if (type.equals("tầm xa")) {
                        speed = 2;
                    }
                } else if (level <= 100) {
                    buffDuration = 9000;
                    buffCooldown = 12000;
                    atk *= 2;
                    heal();
                    if (type.equals("cận chiến")) {
                        speed = 3;
                        // skillTamXa = true;
                    } else if (type.equals("tầm xa")) {
                        speed = 2;
                        // Hutmau = true;
                    }
                }
                isUsingBuffSkill = false;
                buffActive = true;
                frameIndex = 0;
                
            }
        } else if (distanceX < 550 && distanceY < 550 && !isDying && !isUsingTargetSkill) {
            moveTowardPlayerSmartly(player);
        }

        // Kỹ năng triệu hồi
        if (type.equals("tầm xa") && level >= 50 && !SkillTH && health <= (maxHealth*30/100) && !trieuhoi) {
            animate(buffSkill);
            int sl = 0;
            if (level <= 60) sl = 1;
            else if (level <= 70) sl = 2;
            else if (level <= 80) sl = 3;
            else if (level <= 90) sl = 4;
            else sl = 5;
            for (int i = 0; i < sl; i++) {
                Enemy newEnemy = gamePanel.getInstance().createEnemy(
                    x,
                    y,
                    70, 70, 10L, monsterId, name, true
                );
                if (newEnemy != null) {
                    pendingEnemies.add(newEnemy);
                }
            }
            SkillTH = true;
        }

        // Cập nhật hiệu ứng damage
        for (int i = damageEffects.size() - 1; i >= 0; i--) {
            DamageEffect effect = damageEffects.get(i);
            effect.update();
            if (effect.isExpired()) {
                damageEffects.remove(i);
            }
        }

        // Update buff cooldown
        if (buffCooldown > 0) {
            buffCooldown--;
        }

        // Update buff duration
        if (buffActive && buffDuration > 0) {
            buffDuration--;
            if (buffDuration <= 0) {
                buffActive = false;
                atk = atk / 2; // Reset damage buff
            }
        }
        boolean buff = false;

        if (level >= 10 && level < 20){ if(health < maxHealth*20/100) buff = true;}
        else if (level < 30){ if(health < maxHealth*30/100) buff = true;}
        else if (level < 40){ if(health < maxHealth*40/100) buff = true;}
        else if (level < 50){ if(health < maxHealth*50/100) buff = true;}
        else if (level < 60){ if(health < maxHealth*60/100) buff = true;}
        else if (level < 70){ if(health < maxHealth*70/100) buff = true;}
        else if (level < 80){ if(health < maxHealth*80/100) buff = true;}
        else if (level <= 100){ if(health < maxHealth*80/100) buff = true;}

        // Check for buff skill activation
        if (!type.equals("canh gác") && buff && !isUsingBuffSkill && !isAttacking && buffCooldown <= 0) {
            isUsingBuffSkill = true;
            isBuffAnimationComplete = false;
            frameIndex = 0;
            frameTick = 0;
        }

        if (health <= 0L && !isDying) {
            health = 1L; // Keep alive temporarily for animation
            isAttacking = false; // Stop other animations
            isUsingBuffSkill = false;
            buffActive = false;
            isDying = true;
            isReadyToDie = false; // Reset flag
            frameIndex = 0; // Start death animation from beginning
        }

        // Handle death animation
        if (isDying) {
            animate(deathFrames);
            // If animation complete and dying, mark ready to die
            if (isDieAnimationComplete) {
                isDying = false;
                isReadyToDie = true;
            }
            return; // Skip all other updates while dying
        }

        // Actually die on next frame
        if (isReadyToDie) {
            health = 0L;
            isReadyToDie = false;
        }

        // Check for target skill activation
        if (type.equals("tầm xa") && distanceX < 150 && distanceY < 150 && !isUsingTargetSkill 
            && !isAttacking && !isUsingBuffSkill && targetSkillCooldown <= 0) {
            isUsingTargetSkill = true;
            isTargetSkillComplete = false;
            frameIndex = 0;
            targetSkillCooldown = 600;
        }

        // Handle target skill animation
        if (isUsingTargetSkill && !isDying) {
            animate(targetSkill);
            
            if (isTargetSkillComplete) {
                isUsingTargetSkill = false;
                isShowingExplosion = true;
                explosionFrameIndex = 0;
                // Set explosion position at player's center
                explosionX = player.getX() + player.getWidth()/2;
                explosionY = player.getY() + player.getHeight()/2;
            }
        }

        // Handle explosion animation
        if (isShowingExplosion) {
            // Increment damage timer
            explosionDamageTimer++;
            // Only check collision if haven't dealt damage yet
            if (!hasDealtExplosionDamage && explosionDamageTimer >= explosionDamageDelay) {
                Rectangle explosionBox = new Rectangle(
                    explosionX - 25,
                    explosionY - 25,
                    50,
                    50
                );
                
                Rectangle playerBox = new Rectangle(
                    player.getX(), 
                    player.getY(), 
                    player.getWidth(), 
                    player.getHeight()
                );
                
                // Deal damage only once if player is in explosion area
                if (explosionBox.intersects(playerBox)) {
                    Long damage = calculateLimitedDamage(player, "skill");
                    player.takeDamage(damage);
                    hasDealtExplosionDamage = false;
                    explosionDamageTimer = 0;
                }
            }

            frameTick++;
            if (frameTick >= frameDelay) {
                frameTick = 0;
                if (explosionFrameIndex < explosionFrameCount - 1) {
                    explosionFrameIndex++;
                } else {
                    isShowingExplosion = false;
                    explosionFrameIndex = 0;
                }
            }
        }

        // Update target skill cooldown
        if (targetSkillCooldown > 0) {
            targetSkillCooldown--;
        }

        if (!isAttacking && !isUsingBuffSkill && !isUsingTargetSkill 
            && Math.abs(x - newX) < 0.1 && Math.abs(y - newY) < 0.1) {
            isIdle = true;
            animate(idleFrames);
        } else {
            isIdle = false;
        }
    }

    public List<Enemy> getPendingEnemies() {
        return pendingEnemies;
    }

    // Add method to clear pending enemies after they're added
    public void clearPendingEnemies() {
        pendingEnemies.clear();
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

        // Draw buff active effect
        if (buffActive) {
            drawBuffEffect(g, camX, camY);
        }

        // Draw target skill animation
        if (isUsingTargetSkill && currentImage != null) {
            int drawX = x - width/2 - camX;
            int drawY = y - height/2 - camY;
            g2d.drawImage(currentImage, drawX, drawY, width, height, null);
        }

        // Draw explosion effect
        if (isShowingExplosion && explosion != null && explosionFrameIndex < explosion.length) {
            BufferedImage explosionFrame = explosion[explosionFrameIndex];
            int explosionWidth = 50;
            int explosionHeight = 50;
            g.drawImage(explosionFrame, 
                explosionX - explosionWidth/2 - camX,
                explosionY - explosionHeight/2 - camY,
                explosionWidth,
                explosionHeight,
                null);
            
            // Debug: show explosion damage area
            g.setColor(new Color(255, 165, 0, 100));
            g.fillRect(explosionX - explosionWidth/2 - camX,
                      explosionY - explosionHeight/2 - camY,
                      explosionWidth,
                      explosionHeight);
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
            Player player = gamePanel.getInstance().getPlayer();
            if (player != null) {
                player.gainExp(monsterId);
            }
            // Initialize the list only if it's null
            if (GameData.droppedItems == null) {
                GameData.droppedItems = new ArrayList<>();
            }
            // Drop item when monster dies
            drop(x, y, monsterId);
        }
    }

    public void ChisoGoc() {
        GameMonster monster = GameData.monster.stream()
            .filter(c -> c.getId().equals(monsterId))
            .findFirst()
            .orElse(null);

        if (monster == null) {
            System.out.println("Monster not found: " + monsterId);
            return;
        }

        double levelMultiplier = Math.pow(1.2, level); // tăng 10% mỗi cấp
        maxHealth = (long)(basemaxHealth * levelMultiplier);
        health = maxHealth;
        atk = (long)(baseatk * levelMultiplier);
        def = (long)(basedef * levelMultiplier);
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
                                GameWindow.getInstance().getLevel()
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

// region Quản lý Logic

    // Thuật toán di chuyển của quái
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
            if (dx > 0 && !player.movement.isBlocked(x + speed, y, 20, 50)) {
                x += speed;
                direction = "right";
                animate(rightFrames);
                moved = true;
            } else if (dx < 0 && !player.movement.isBlocked(x - speed, y, 20, 50)) {
                x -= speed;
                direction = "left";
                animate(leftFrames);
                moved = true;
            }
            // Nếu bị chặn, thử đi dọc để "men theo tường"
            else if (dy > 0 && !player.movement.isBlocked(x, y + offset, 20, 50)) {
                y += offset;
                moved = true;
            } else if (dy < 0 && !player.movement.isBlocked(x, y - offset, 20, 50)) {
                y -= offset;
                moved = true;
            }
        } else {
            // Ưu tiên di chuyển theo Y
            if (dy > 0 && !player.movement.isBlocked(x, y + speed, 20, 50)) {
                y += speed;
                direction = "down";
                animate(downFrames);
                moved = true;
            } else if (dy < 0 && !player.movement.isBlocked(x, y - speed, 20, 50)) {
                y -= speed;
                direction = "up";
                animate(upFrames);
                moved = true;
            }
            // Nếu bị chặn, thử đi ngang để "men theo tường"
            else if (dx > 0 && !player.movement.isBlocked(x + offset, y, 20, 50)) {
                x += offset;
                moved = true;
            } else if (dx < 0 && !player.movement.isBlocked(x - offset, y, 20, 50)) {
                x -= offset;
                moved = true;
            }
        }

        // Nếu vẫn không di chuyển được
        if (!moved) {
            if (dx > 0 && Math.abs(dx) > Math.abs(dy)) {
                if (!player.movement.isBlocked(x, y - speed, 20, 50)) {
                    x += speed;
                    y -= speed * 2;
                } else if (!player.movement.isBlocked(x, y + speed, 20, 50)) {
                    x += speed;
                    y += speed * 2;
                }
            } else if (dx > 0 && Math.abs(dx) <= Math.abs(dy)) {
                if (!player.movement.isBlocked(x - speed, y, 20, 50)) {
                    y -= speed;
                    x -= speed * 2;
                } else if (!player.movement.isBlocked(x + speed, y, 20, 50)) {
                    y -= speed;
                    x += speed * 2;
                }
                
            } else if (dx < 0 && Math.abs(dx) > Math.abs(dy)) {
                if (!player.movement.isBlocked(x, y - speed, 20, 50)) {
                    x -= speed;
                    y -= speed * 2; 
                } else if (!player.movement.isBlocked(x, y + speed, 20, 50)) {
                    x -= speed;
                    y += speed * 2; 
                }
            } else if (dx < 0 && Math.abs(dx) <= Math.abs(dy)) {
                if (!player.movement.isBlocked(x - speed, y, 20, 50)) {
                    y += speed;
                    x -= speed * 2;
                } else if (!player.movement.isBlocked(x + speed, y, 20, 50)) {
                    y += speed;
                    x += speed * 2;
                }
            } 
            // else {
            //     Random rand = new Random();
            //     int dir = rand.nextInt(4); // 0: lên, 1: xuống, 2: trái, 3: phải

            //     switch (dir) {
            //         case 0:
            //             if (!player.isBlocked(x, y - speed, 40, 50)) {
            //                 y -= speed;
            //                 direction = "up";
            //                 animate(upFrames);
            //             }
            //             break;
            //         case 1:
            //             if (!player.isBlocked(x, y + speed, 40, 50)) {
            //                 y += speed;
            //                 direction = "down";
            //                 animate(downFrames);
            //             }
            //             break;
            //         case 2:
            //             if (!player.isBlocked(x - speed, y, 40, 50)) {
            //                 x -= speed;
            //                 direction = "left";
            //                 animate(leftFrames);
            //             }
            //             break;
            //         case 3:
            //             if (!player.isBlocked(x + speed, y, 40, 50)) {
            //                 x += speed;
            //                 direction = "right";
            //                 animate(rightFrames);
            //             }
            //             break;
            //     }
            // }
        }
    }

    //
    public boolean isDead() {
        return health <= 0;
    }

    private Long calculateLimitedDamage(Player player, String type) {
        double damage = 1.0;
        if (buffActive && buffDuration > 0) {
            damage = 1.3;
        }
        if(type.equals("đánh thường")) {
            // Calculate max damage allowed (25-35% of player's max HP)
            double maxDamagePercent = 15 + random.nextInt(11); // Random between 25-35
            Long maxAllowedDamage = (long)(player.stats.getMaxHealth() * maxDamagePercent / 100);
            
            // Return the smaller value between raw damage and max allowed damage
            return (long)(maxAllowedDamage * damage);
        } else {
            // Calculate max damage allowed (25-35% of player's max HP)
            double maxDamagePercent = 20 + random.nextInt(11); // Random between 25-35
            Long maxAllowedDamage = (long)(player.stats.getMaxHealth() * maxDamagePercent / 100);
            
            // Return the smaller value between raw damage and max allowed damage
            return (long)(maxAllowedDamage * damage);
        }
    }

// endregion

// region Quản lý effect, debug

    // Vùng va chạm của quái
    public void drawCollisionBox(Graphics2D g, int x, int y) {
        int width = 20;
        int height = 50; //quái lớn(150px)
        // int height = 20; //quái nhỏ
        int footHeight = height / 10;

        g.setColor(new Color(255, 0, 0, 100)); // Đỏ trong suốt
        g.drawRect(x, y + height - footHeight, width, footHeight);
    }

    // Hiệu ứng: hào quang Buff
    private void drawBuffEffect(Graphics g, int camX, int camY) {
        if (buffActive && bufferEffectFrames != null && bufferEffectFrames.length > 0) {
            buffEffectTick++;
            if (buffEffectTick >= buffEffectFrameDelay) {
                buffEffectTick = 0;
                buffEffectFrameIndex = (buffEffectFrameIndex + 1) % bufferEffectFrames.length;
            }

            // Vẽ hiệu ứng xung quanh Enemy (giữa sprite hoặc rộng hơn tí)
            BufferedImage buffImg = bufferEffectFrames[buffEffectFrameIndex];
            int drawX = x - width/2 - camX - 5; // Để hiệu ứng rộng hơn nhân vật
            int drawY = y - height/2 - camY - 5;
            int drawW = width + 10;
            int drawH = height + 10;

            g.drawImage(buffImg, drawX, drawY, drawW, drawH, null);
        }
    }

    // Hoạt ảnh của quái
    private void animate(BufferedImage[] frames) {
        frameTick++;
        if (frameTick >= frameDelay) {
            frameTick = 0;
            // Safety check for null or empty frames array
            if (frames == null || frames.length == 0) {
                return;
            }
            int maxFrames;
            if (frames == idleFrames) {  // Add idle animation handling
                maxFrames = Math.min(idleFrameCount, frames.length);
            } else {
                maxFrames = Math.min(normalFrameCount, frames.length);
            }
            
            if (frames == attackFramesL || frames == attackFramesR) {
                if (frameIndex < attackFrameCount - 1) {
                    frameIndex++;
                } else {
                    isAttackAnimationComplete = true;
                    frameIndex = 0;
                }
            } else if (frames == buffSkill) {
                if (frameIndex < buffFrameCount - 1) {
                    frameIndex++;
                } else {
                    isBuffAnimationComplete = true;
                    frameIndex = 0;
                    isUsingBuffSkill = false;
                }
            } else if (frames == deathFrames) {
                if (frameIndex < deathFrameCount - 1) {
                    frameIndex++;
                } else {
                    isDieAnimationComplete = true;
                    frameIndex = 0;
                }
            } else if (frames == targetSkill) {
                if (frameIndex < skillFrameCount - 1) {
                    frameIndex++;
                } else {
                    isTargetSkillComplete = true;
                    frameIndex = 0;
                }
            } else if (frames == idleFrames) {  // Add idle animation logic
                frameIndex = (frameIndex + 1) % maxFrames;
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
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public Long getHealth() {
        return health;
    }
    public Long getMaxHealth() {
        return maxHealth;
    }
}

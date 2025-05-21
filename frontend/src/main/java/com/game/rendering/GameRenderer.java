package com.game.rendering;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import com.game.*;
import com.game.ui.GamePanel;
import com.game.data.GameData;
import java.util.ConcurrentModificationException;

public class GameRenderer {
    private GamePanel gamePanel;
    private static final double SCALE = 1.8;
    
    // Constants from GamePanel
    private static final int SKILL_BUTTON_SIZE = 70;
    private static final int NORMAL_ATTACK_SIZE = 80;
    private static final int SKILL_PADDING = 10;
    private static final int PORTAL_SIZE = 50;

    public GameRenderer(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void render(Graphics2D g2d) {
        g2d.scale(SCALE, SCALE);

        if (gamePanel.getPlayer() != null) {
            renderGameWorld(g2d);
            renderHUD(g2d);
            renderSkillButtons(g2d);
        }
    }

    private void renderGameWorld(Graphics2D g2d) {
        Player player = gamePanel.getPlayer();
        int camX = player.getX() - (int)(gamePanel.getWidth()/(2 * SCALE));
        int camY = player.getY() - (int)(gamePanel.getHeight()/(2 * SCALE));

        // Apply camera bounds
        camX = Math.max(0, Math.min(camX, gamePanel.getMapImage().getWidth() - (int)(gamePanel.getWidth()/SCALE)));
        camY = Math.max(0, Math.min(camY, gamePanel.getMapImage().getHeight() - (int)(gamePanel.getHeight()/SCALE)));

        // Draw map
        g2d.drawImage(gamePanel.getMapImage(), -camX, -camY, null);

        // Draw portal if not in boss room
        if (!gamePanel.isBossRoom()) {
            renderPortal(g2d, camX, camY);
        }

        // Draw game ending screen
        if (gamePanel.isGameEnding()) {
            renderGameEnding(g2d);
        }

        // Draw entities
        player.draw(g2d, camX, camY);
        for (Enemy enemy : gamePanel.getEnemies()) {
            enemy.draw(g2d, camX, camY);
        }

        // Draw dropped items
        try {
            if (GameData.droppedItems != null) {
                for (DroppedItem item : GameData.droppedItems) {
                    if (item != null) {
                        item.draw(g2d, camX, camY);
                    }
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("Bug tránh được tạm thời: " + e.getMessage());
        }

        // Draw skills
        g2d.scale(1/SCALE, 1/SCALE);
        for (SkillEffect skill : gamePanel.getSkills()) {
            if (skill != null) {
                skill.draw(g2d, camX, camY);
            }
        }
    }

    private void renderPortal(Graphics2D g2d, int camX, int camY) {
        g2d.setColor(new Color(255, 215, 0, 100));
        g2d.fillRect(gamePanel.getPortalX() - camX, gamePanel.getPortalY() - camY, PORTAL_SIZE, PORTAL_SIZE);

        if (gamePanel.isShowPortalPrompt()) {
            g2d.setColor(Color.WHITE);
            String prompt = "Press F to enter Boss Room";
            FontMetrics fm = g2d.getFontMetrics();
            int textX = gamePanel.getPortalX() - camX + (PORTAL_SIZE - fm.stringWidth(prompt))/2;
            int textY = gamePanel.getPortalY() - camY - 10;
            g2d.drawString(prompt, textX, textY);
        }
    }

    private void renderGameEnding(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        int secondsLeft = gamePanel.getEndGameSecondsLeft();
        String message = "Game Over! Returning in " + secondsLeft + "...";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(message, 
            (gamePanel.getWidth() - fm.stringWidth(message))/2, 
            gamePanel.getHeight()/2);
    }

    private void renderHUD(Graphics2D g2d) {
        Player player = gamePanel.getPlayer();
        
        int hudWidth = 200;
        int hudHeight = 100;
        g2d.drawImage(gamePanel.getHudImage(), 0, 0, hudWidth, hudHeight, null);

        renderHealthBar(g2d, player);
        renderManaBar(g2d, player);
    }

    private void renderHealthBar(Graphics2D g2d, Player player) {
        int maxHealthBarWidth = 110;
        int healthBarHeight = 10;
        double healthPercent = (double) player.stats.getHealth() / player.stats.getMaxHealth();
        int currentHealthWidth = (int) (healthPercent * maxHealthBarWidth);

        g2d.setColor(Color.RED);
        g2d.fillRect(50, 30, currentHealthWidth, healthBarHeight);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(50, 30, maxHealthBarWidth, healthBarHeight);
        g2d.setColor(Color.WHITE);
        g2d.drawString((long)(player.stats.getHealth()) + "HP", 55, 39);
    }

    private void renderManaBar(Graphics2D g2d, Player player) {
        int maxManaBarWidth = 110;
        int manaBarHeight = 10;
        double manaPercent = (double) player.stats.getMana() / player.stats.getMaxMana();
        int currentManaWidth = (int) (manaPercent * maxManaBarWidth);

        g2d.setColor(Color.BLUE);
        g2d.fillRect(50, 50, currentManaWidth, manaBarHeight);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(50, 50, maxManaBarWidth, manaBarHeight);
        g2d.setColor(Color.WHITE);
        g2d.drawString((long)(player.stats.getMana()) + "MP", 55, 59);
    }

    private void renderSkillButtons(Graphics2D g) {
        int screenWidth = gamePanel.getWidth();
        int screenHeight = gamePanel.getHeight();
        
        int normalX = screenWidth - NORMAL_ATTACK_SIZE - SKILL_PADDING;
        int normalY = screenHeight - NORMAL_ATTACK_SIZE - SKILL_PADDING;
        gamePanel.setNormalAttackBounds(new Rectangle(normalX, normalY, NORMAL_ATTACK_SIZE, NORMAL_ATTACK_SIZE));
        
        g.setColor(new Color(0, 0, 0, 150));
        g.fillOval(normalX, normalY, NORMAL_ATTACK_SIZE, NORMAL_ATTACK_SIZE);
        g.drawImage(gamePanel.getDanhthuong(), normalX, normalY, NORMAL_ATTACK_SIZE, NORMAL_ATTACK_SIZE, null);
        
        double radius = NORMAL_ATTACK_SIZE + SKILL_PADDING;
        for (int i = 0; i < 4; i++) {
            double angle = Math.PI / 2 + (i * Math.PI / 6);
            int skillX = normalX + NORMAL_ATTACK_SIZE/2 - SKILL_BUTTON_SIZE/2 
                        + (int)(radius * Math.cos(angle));
            int skillY = normalY + NORMAL_ATTACK_SIZE/2 - SKILL_BUTTON_SIZE/2 
                        - (int)(radius * Math.sin(angle));
            
            gamePanel.setSkillButtonBound(i, new Rectangle(skillX, skillY, SKILL_BUTTON_SIZE, SKILL_BUTTON_SIZE));
            
            g.setColor(new Color(0, 0, 0, 150));
            g.fillOval(skillX, skillY, SKILL_BUTTON_SIZE, SKILL_BUTTON_SIZE);
            
            if (gamePanel.isSkillUnlocked(i) && gamePanel.getSkillIcon(i) != null) {
                g.drawImage(gamePanel.getSkillIcon(i), skillX, skillY, SKILL_BUTTON_SIZE, SKILL_BUTTON_SIZE, null);
            }
            
            g.setColor(Color.GRAY);
            g.drawOval(skillX, skillY, SKILL_BUTTON_SIZE, SKILL_BUTTON_SIZE);
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            String slotNum = String.valueOf(i + 1);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(slotNum, 
                skillX + (SKILL_BUTTON_SIZE - fm.stringWidth(slotNum))/2,
                skillY + SKILL_BUTTON_SIZE - 5);
        }

        long currentTime = System.currentTimeMillis();
    
        // Draw normal attack cooldown
        if (currentTime - gamePanel.getLastNormalAttackTime() < (1000 / gamePanel.getAttackSpeed())) {
            double progress = 1 - ((currentTime - gamePanel.getLastNormalAttackTime()) / 
                                (1000.0 / gamePanel.getAttackSpeed()));
            drawCooldownOverlay(g, gamePanel.getNormalAttackBounds(), progress);
        }

        // Draw skill cooldowns
        for (int i = 0; i < 4; i++) {
            if (gamePanel.isSkillUnlocked(i)) {
                long timeSinceLastUse = currentTime - gamePanel.getSkillLastUsedTime(i);
                int cooldown = gamePanel.getSkillCooldown(i);
                
                if (timeSinceLastUse < cooldown) {
                    double progress = 1 - (timeSinceLastUse / (double)cooldown);
                    drawCooldownOverlay(g, gamePanel.getSkillButtonBounds()[i], progress);
                    
                    // Draw cooldown text
                    int secondsLeft = (int)Math.ceil((cooldown - timeSinceLastUse) / 1000.0);
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Arial", Font.BOLD, 16));
                    String cooldownText = String.valueOf(secondsLeft);
                    FontMetrics fm = g.getFontMetrics();
                    g.drawString(cooldownText,
                        gamePanel.getSkillButtonBounds()[i].x + 
                            (SKILL_BUTTON_SIZE - fm.stringWidth(cooldownText))/2,
                        gamePanel.getSkillButtonBounds()[i].y + SKILL_BUTTON_SIZE/2 + fm.getHeight()/2);
                }
            }
        }
    }

    private void drawCooldownOverlay(Graphics2D g, Rectangle bounds, double progress) {
        g.setColor(new Color(0, 0, 0, 150));
        Arc2D.Double arc = new Arc2D.Double(
            bounds.x, bounds.y, bounds.width, bounds.height,
            90, progress * 360, Arc2D.PIE
        );
        g.fill(arc);
    }
}
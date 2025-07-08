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

    private int camX, camY;
    
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
            renderHealthEnemy(g2d);
        }
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.setColor(Color.YELLOW);
        String fpsText = "FPS: " + GamePanel.getInstance().getGameLoop().getFps();
        FontMetrics fm = g2d.getFontMetrics();
        int x = gamePanel.getWidth() - fm.stringWidth(fpsText) - 16;
        int y = 22;
        g2d.drawString(fpsText, x, y);
    }

    private void renderGameWorld(Graphics2D g2d) {
        Player player = gamePanel.getPlayer();
        camX = player.getX() - (int)(gamePanel.getWidth()/(2 * SCALE));
        camY = player.getY() - (int)(gamePanel.getHeight()/(2 * SCALE));

        // Apply camera bounds
        camX = Math.max(0, Math.min(camX, gamePanel.getMapImage().getWidth() - (int)(gamePanel.getWidth()/SCALE)));
        camY = Math.max(0, Math.min(camY, gamePanel.getMapImage().getHeight() - (int)(gamePanel.getHeight()/SCALE)));

        // Draw map
        BufferedImage mapImg = gamePanel.getMapImage();
        int viewW = (int)(gamePanel.getWidth()/SCALE);
        int viewH = (int)(gamePanel.getHeight()/SCALE);
        BufferedImage subMap = mapImg.getSubimage(camX, camY, viewW, viewH);
        g2d.drawImage(subMap, 0, 0, viewW, viewH, null);

        // Draw portal if not in boss room
        if (!gamePanel.isBossRoom()) {
            renderPortal(g2d, camX, camY);
        }

        // Draw entities
        player.draw(g2d, camX, camY);
        for (Enemy enemy : gamePanel.getEnemies()) {
            if (enemy.isInView(camX, camY, viewW, viewH)) {
                enemy.draw(g2d, camX, camY);
            }
        }

        // Draw dropped items
        try {
            if (GameData.droppedItems != null) {
                for (DroppedItem item : GameData.droppedItems) {
                    if (item != null && item.isInView(camX, camY, viewW, viewH)) {
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

        renderVisionLimit(g2d, camX, camY, 550f);

        // Draw game ending screen
        if (gamePanel.isGameEnding()) {
            if (gamePanel.isLoss()) {
                renderVisionLimit(g2d, camX, camY, 300f);
            }
            renderGameEnding(g2d);
        }
    }

    private void renderPortal(Graphics2D g2d, int camX, int camY) {
        Font oldFont = g2d.getFont();
        Font font = new Font("Arial", Font.BOLD, 20);
        g2d.setFont(font);

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
        g2d.setFont(oldFont);
    }

    private void renderGameEnding(Graphics2D g2d) {
        Font oldFont = g2d.getFont();
        Font font = new Font("Arial", Font.BOLD, 48);
        g2d.setFont(font);

        int secondsLeft = gamePanel.getEndGameSecondsLeft();

        String line1 = "Your Win!";
        if (gamePanel.isLoss()) {
            line1 = "Game Over!";
        }
        String line2 = "Returning in " + secondsLeft + "...";

        FontMetrics fm = g2d.getFontMetrics();
        int panelWidth = gamePanel.getWidth();
        int panelHeight = gamePanel.getHeight();

        int lineHeight = fm.getHeight();

        int textX1 = (panelWidth - fm.stringWidth(line1)) / 2;
        int textX2 = (panelWidth - fm.stringWidth(line2)) / 2;

        int startY = (panelHeight - lineHeight * 2) / 2;

        // Vẽ viền chữ để dễ đọc
        g2d.setColor(Color.BLACK);
        g2d.drawString(line1, textX1 + 2, startY + 2);
        g2d.drawString(line2, textX2 + 2, startY + lineHeight + 2);

        g2d.setColor(Color.WHITE);
        g2d.drawString(line1, textX1, startY);
        g2d.drawString(line2, textX2, startY + lineHeight);

        g2d.setFont(oldFont);
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

    private void renderVisionLimit(Graphics2D g2d, int camX, int camY, float radius) {
        Player player = gamePanel.getPlayer();

        // Tính vị trí trung tâm tầm nhìn (vị trí nhân vật trong viewport)
        int centerX = (int)((player.getX() - camX)*SCALE);
        int centerY = (int)((player.getY() - camY)*SCALE);

        // Bán kính tầm nhìn
        // float radius = 550f;

        // Tạo hiệu ứng Radial Gradient từ sáng ở giữa đến tối dần
        RadialGradientPaint light = new RadialGradientPaint(
            new Point(centerX, centerY), radius,
            new float[]{0f, 1f},
            new Color[]{new Color(0f, 0f, 0f, 0f), new Color(0f, 0f, 0f, 0.85f)}
        );

        Composite originalComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.SrcOver);
        Paint originalPaint = g2d.getPaint();

        g2d.setPaint(light);
        g2d.fillRect(0, 0, gamePanel.getWidth(), gamePanel.getHeight());

        // Khôi phục lại trạng thái ban đầu
        g2d.setPaint(originalPaint);
        g2d.setComposite(originalComposite);
    }

    private void renderHealthEnemy(Graphics2D g) {
        if (gamePanel.getNearestEnemy() != null) {
            int barWidth = 200;
            int barHeight = 20;

            Long health = gamePanel.getNearestEnemy().getHealth();
            Long maxHealth = gamePanel.getNearestEnemy().getMaxHealth();
            int filledWidth = (int) ((double) health / maxHealth * barWidth);

            int screenX = (gamePanel.getWidth() - barWidth) / 2;
            int screenY = 30; // top position

            // Vẽ viền
            g.setColor(Color.DARK_GRAY);
            g.fillRect(screenX, screenY, barWidth, barHeight);

            // Vẽ phần máu
            g.setColor(Color.RED);
            g.fillRect(screenX, screenY, filledWidth, barHeight);

            // Vẽ chữ máu
            g.setColor(Color.WHITE);
            String hpText = health + " / " + maxHealth;
            FontMetrics fm = g.getFontMetrics();
            int textX = screenX + (barWidth - fm.stringWidth(hpText)) / 2;
            int textY = screenY + ((barHeight - fm.getHeight()) / 2) + fm.getAscent();
            g.drawString(hpText, textX, textY);
        }
    }

    public int getCamX() {
        return camX;
    }
    public int getCamY() {
        return camY;
    }
}
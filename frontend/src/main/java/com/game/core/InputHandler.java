package com.game.core;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Point;
import com.game.SkillEffect;
import com.game.ui.GamePanel;
import com.game.model.GameCharacterSkill;
import com.game.data.GameData;
import com.game.rendering.*;

public class InputHandler extends MouseAdapter implements KeyListener {
    private GamePanel gamePanel;
    
    public InputHandler(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Point click = e.getPoint();

        int camX = gamePanel.getRenderer().getCamX();
        int camY = gamePanel.getRenderer().getCamY();
        double scale = 1.8;

        int mapX = (int) (click.x / scale) + camX;
        int mapY = (int) (click.y / scale) + camY;

        System.out.println("Map click at: (" + mapX + ", " + mapY + ")");

        if (gamePanel.getNormalAttackBounds().contains(click)) {
            handleNormalAttack();
        }

        for (int i = 0; i < 4; i++) {
            final int slot = i;
            if (gamePanel.getSkillButtonBounds()[i].contains(click) && 
                gamePanel.getSkillUnlocked()[i]) {
                handleSkillUse(slot);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (GamePanel.getInstance().isPaused()) {
            return; // Không xử lý phím/mouse khi đang pause
        }
        int key = e.getKeyCode();
        if (key == KeyBindingConfig.getKey("Attack")) {
            handleNormalAttack();
        }
        if (key == KeyBindingConfig.getKey("Skill1")) {
            handleSkillUse(0);
        }
        if (key == KeyBindingConfig.getKey("Skill2")) {
            handleSkillUse(1);
        }
        if (key == KeyBindingConfig.getKey("Skill3")) {
            handleSkillUse(2);
        }
        if (key == KeyBindingConfig.getKey("Skill4")) {
            handleSkillUse(3);
        }
        if (key == KeyBindingConfig.getKey("Interact")) {
            if (gamePanel.isNearPortal() && !gamePanel.isBossRoom()) {
                gamePanel.loadBossRoom();
            }
        }
        gamePanel.getPlayer().setDirection(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (GamePanel.getInstance().isPaused()) {
            return; // Không xử lý phím/mouse khi đang pause
        }
        gamePanel.getPlayer().setDirection(e.getKeyCode(), false);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    private void handleNormalAttack() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - gamePanel.getLastNormalAttackTime() >= (1000 / gamePanel.getAttackSpeed())) {
            SkillEffect skill = gamePanel.getPlayer().castSkill(0, gamePanel.getEnemies(), -50);
            if (skill != null) {
                gamePanel.addSkill(skill);
                gamePanel.updateNormalAttackCooldown(System.currentTimeMillis());
            }
        }
    }

    private void handleSkillUse(int slot) {
        if (!gamePanel.getSkillUnlocked()[slot]) return;

        GameCharacterSkill charSkill = GameData.characterSkills.stream()
            .filter(cs -> cs.getCharacterId().equals(gamePanel.getPlayerId()) && 
                        cs.getSlot() == slot + 1)
            .findFirst()
            .orElse(null);

        if (charSkill != null) {
            var skillData = gamePanel.getSkillDataMap().get(charSkill.getSkillId());
            if (skillData != null) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - gamePanel.getSkillLastUsedTime(slot) >= 
                    gamePanel.getSkillCooldown(slot)) {
                    SkillEffect effect = gamePanel.getPlayer().castSkill(slot + 1, 
                                                                      gamePanel.getEnemies(), -50);
                    if (effect != null) {
                        gamePanel.addSkill(effect);
                        gamePanel.updateSkillLastUsedTime(slot, currentTime);
                    }
                }
            }
        }
    }
}
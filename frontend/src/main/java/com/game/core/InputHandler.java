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

public class InputHandler extends MouseAdapter implements KeyListener {
    private GamePanel gamePanel;
    
    public InputHandler(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Point click = e.getPoint();

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
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SPACE:
                handleNormalAttack();
                break;
            case KeyEvent.VK_Q:
                handleSkillUse(0);
                break;
            case KeyEvent.VK_E:
                handleSkillUse(1);
                break;
            case KeyEvent.VK_R:
                handleSkillUse(2);
                break;
            case KeyEvent.VK_T:
                handleSkillUse(3);
                break;
            case KeyEvent.VK_F:
                if (gamePanel.isNearPortal()) {
                    gamePanel.loadBossRoom();
                }
                break;
            default:
                gamePanel.getPlayer().setDirection(e.getKeyCode(), true);
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
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
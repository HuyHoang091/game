package com.game.state;

import com.game.*;
import com.game.ui.GamePanel;
import javax.swing.SwingUtilities;
import java.awt.Window;
import java.util.ArrayList;

public class BossRoomState {
    private GamePanel gamePanel;
    private boolean isBossRoom = false;
    private boolean isBossInitialized = false;
    private boolean isLoadingMap = false;
    private boolean gameEnding = false;
    private long gameEndTime = 0;
    private static final int END_GAME_DELAY = 3000; // 3 seconds

    public BossRoomState(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void loadBossRoom() {
        isLoadingMap = true;

        // Load boss map data
        MapData bossMapData = new MapData("assets/image.png", "assets/vatlieu.png");
        
        // Add boss enemy data
        // MapData.EnemyData bossData = new MapData.EnemyData(300, 250, 100, 100, 1000L, 1L, "NightBorne");
        // bossData.isBoss = true;
        // bossMapData.enemies = new ArrayList<>();
        // bossMapData.enemies.add(bossData);
        
        // Set boss room flag
        isBossRoom = true;
        isBossInitialized = false;
        
        // Load the new map
        gamePanel.loadResources(bossMapData);

        isLoadingMap = false;
    }

    public void update() {
        if (gameEnding) {
            if (System.currentTimeMillis() - gameEndTime >= END_GAME_DELAY) {
                gamePanel.getGameLoop().stopGameThread();
                SwingUtilities.invokeLater(() -> {
                    Window window = SwingUtilities.getWindowAncestor(gamePanel);
                    if (window != null) {
                        window.dispose();
                    }
                });
            }
            return;
        }

        if (!isLoadingMap) {
            if (isBossRoom && !isBossInitialized && !gamePanel.getEnemies().isEmpty()) {
                isBossInitialized = true;
            }

            if (isBossRoom && isBossInitialized && !isLoadingMap && 
                gamePanel.getEnemies().isEmpty()) {
                gameEnding = true;
                gameEndTime = System.currentTimeMillis();
            }
        }
    }

    // Getters
    public boolean isBossRoom() { return isBossRoom; }
    public boolean isGameEnding() { return gameEnding; }
    public int getEndGameSecondsLeft() {
        return (int)Math.ceil((END_GAME_DELAY - 
            (System.currentTimeMillis() - gameEndTime)) / 1000.0);
    }
}
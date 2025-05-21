package com.game.core;

import com.game.ui.GamePanel;

public class GameLoop implements Runnable {
    private boolean isRunning;
    private Thread gameThread;
    private GamePanel gamePanel;
    
    public GameLoop(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void startGameThread() {
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void stopGameThread() {
        isRunning = false;
        // if (gameThread != null) {
        //     try {
        //         gameThread.join();
        //         skills.clear();
        //         enemies.clear();
        //         collisionObjects.clear();
        //         if (GameData.droppedItems != null) {
        //             GameData.droppedItems.clear();
        //         }
        //         mapImage = null;
        //         hudImage = null;
        //         up = down = left = right = null;
        //     } catch (InterruptedException e) {
        //         e.printStackTrace();
        //     }
        //     gameThread = null;
        // }
    }

    @Override
    public void run() {
        while (isRunning) {
            gamePanel.update();
            gamePanel.repaint();
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
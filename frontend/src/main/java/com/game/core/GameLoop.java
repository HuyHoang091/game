package com.game.core;

import com.game.ui.GamePanel;
import com.game.audio.*;

public class GameLoop implements Runnable {
    private boolean isRunning;
    private Thread gameThread;
    private GamePanel gamePanel;
    private MusicPlayer musicPlayer;
    
    public GameLoop(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.musicPlayer = new MusicPlayer();
    }

    public void startGameThread() {
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();

        // 🔊 Phát nhạc nền khi bắt đầu game
        musicPlayer.playBackgroundMusic("assets/music.wav");
    }

    public void stopGameThread() {
        isRunning = false;
        if (gameThread != null) {
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            gameThread = null;
        }

        musicPlayer.stop();
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

    public MusicPlayer getMusicPlayer() {
        return musicPlayer;
    }
}
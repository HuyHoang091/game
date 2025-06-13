package com.game.core;

import com.game.ui.GamePanel;
import com.game.audio.*;

public class GameLoop implements Runnable {
    private boolean isRunning;
    private Thread gameThread;
    private GamePanel gamePanel;
    private MusicPlayer musicPlayer;

    private int fps = 0;
    private int frames = 0;
    private long lastFpsTime = System.currentTimeMillis();
    
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

            frames++;
            long now = System.currentTimeMillis();
            if (now - lastFpsTime >= 1000) {
                fps = frames;
                frames = 0;
                lastFpsTime = now;
            }
            
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

    public int getFps() { return fps; }
}
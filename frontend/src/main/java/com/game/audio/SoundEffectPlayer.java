package com.game.audio;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundEffectPlayer {

    public void playSound(String resourcePath) {
        try {
            URL url = getClass().getClassLoader().getResource(resourcePath);
            if (url == null) {
                System.err.println("Không tìm thấy hiệu ứng âm thanh: " + resourcePath);
                return;
            }

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInput);
            clip.start(); // Phát 1 lần, không lặp
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
package com.game.audio;

import java.net.URL;
import javax.sound.sampled.*;
import java.io.IOException;
import com.game.ui.*;

public class MusicPlayer {
    private Clip clip;
    private FloatControl volumeControl;
    private SettingsPanel settingsPanel;

    public void playBackgroundMusic(String resourcePath) {
        try {
            URL url = getClass().getClassLoader().getResource(resourcePath);
            if (url == null) {
                System.err.println("Không tìm thấy file nhạc: " + resourcePath);
                return;
            }

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(audioInput);

            // Lấy volume control TRƯỚC khi sử dụng
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

                // Áp dụng âm lượng từ SettingsPanel nếu cần
                float max = volumeControl.getMaximum(); // Thường là 6.0f
                float minAudible = -20.0f; // Dưới mức này là tắt
                float gain = minAudible + (max - minAudible) * settingsPanel.getInstance().getVolume();
                volumeControl.setValue(gain);
            } else {
                System.err.println("Clip không hỗ trợ MASTER_GAIN");
            }

            clip.loop(Clip.LOOP_CONTINUOUSLY); // Lặp vô hạn
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }

    public void setVolume(float volume) {
        if (volumeControl != null) {
            float max = volumeControl.getMaximum(); // Thường là 6.0f
            float minAudible = -20.0f; // Dưới mức này là tắt
            float gain = minAudible + (max - minAudible) * volume;
            volumeControl.setValue(gain);
        }
    }
}

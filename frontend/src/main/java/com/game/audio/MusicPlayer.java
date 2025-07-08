package com.game.audio;

import java.net.URL;
import javax.sound.sampled.*;
import com.game.ui.*;

public class MusicPlayer {
    private volatile boolean playing = false;
    private Thread musicThread;
    private SourceDataLine line;
    private FloatControl volumeControl;

    public void playBackgroundMusic(String resourcePath) {
        stop();
        playing = true;
        musicThread = new Thread(() -> {
            while (playing) {
                try {
                    URL url = getClass().getClassLoader().getResource(resourcePath);
                    if (url == null) {
                        System.err.println("Không tìm thấy file nhạc: " + resourcePath);
                        return;
                    }
                    AudioInputStream audioInput = AudioSystem.getAudioInputStream(url);
                    AudioFormat format = audioInput.getFormat();
                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                    line = (SourceDataLine) AudioSystem.getLine(info);
                    line.open(format);
                    if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        volumeControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                        float min = volumeControl.getMinimum();
                        float max = volumeControl.getMaximum();
                        float volume = SettingsPanel.getInstance().getVolume(); // 0.0f - 1.0f
                        float gain;
                        if (volume == 0f) {
                            gain = min;
                        } else {
                            gain = (float) (Math.log10(volume) * 20.0);
                            if (gain < min) gain = min;
                            if (gain > max) gain = max;
                        }
                        volumeControl.setValue(gain);
                    } else {
                        volumeControl = null;
                    }
                    line.start();

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while (playing && (bytesRead = audioInput.read(buffer, 0, buffer.length)) != -1) {
                        line.write(buffer, 0, bytesRead);
                    }

                    line.drain();
                    line.stop();
                    line.close();
                    audioInput.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
        musicThread.setDaemon(true);
        musicThread.start();
    }

    public void stop() {
        playing = false;
        if (musicThread != null && musicThread.isAlive()) {
            try {
                musicThread.join(100); // Đợi thread kết thúc
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (line != null && line.isOpen()) {
            line.stop();
            line.close();
        }
    }

    public void setVolume(float volume) {
        // volume: 0.0f (min) -> 1.0f (max)
        if (volumeControl != null) {
            float min = volumeControl.getMinimum();
            float max = volumeControl.getMaximum();
            float gain = min + (max - min) * volume;
            volumeControl.setValue(gain);
        }
    }
}
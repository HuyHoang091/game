package com.game.ui;

import javax.swing.*;
import java.awt.*;
import com.game.*;

public class SettingsPanel extends JPanel {
    private GameWindow gameWindow;
    private JSlider volumeSlider;
    private JComboBox<String> resolutionCombo;
    private JCheckBox fullscreenCheck;
    // GamePanel gamePanel;

    public SettingsPanel(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // Tạo panel cho các controls
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new GridLayout(4, 2, 10, 10));
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Âm lượng
        controlsPanel.add(new JLabel("Âm lượng:"));
        volumeSlider = new JSlider(0, 100, 50);
        controlsPanel.add(volumeSlider);
        
        // Độ phân giải
        controlsPanel.add(new JLabel("Độ phân giải:"));
        String[] resolutions = {"1920x1080", "1280x720", "800x600"};
        resolutionCombo = new JComboBox<>(resolutions);
        controlsPanel.add(resolutionCombo);
        
        // Fullscreen
        controlsPanel.add(new JLabel("Toàn màn hình:"));
        fullscreenCheck = new JCheckBox();
        controlsPanel.add(fullscreenCheck);
        
        // Nút Back
        JButton backButton = new JButton("Quay lại");
        backButton.addActionListener(e -> {
            // gamePanel.player.setHp(100);
            gameWindow.goBack();
        });

        JButton backButton1 = new JButton("Menu chính");
        backButton1.addActionListener(e -> gameWindow.BackToMenu());
        
        // Thêm các components vào panel
        add(Box.createVerticalGlue());
        add(controlsPanel);
        add(Box.createVerticalStrut(20));
        add(backButton);
        add(backButton1);
        add(Box.createVerticalGlue());
    }
}
package com.game.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.Timer;

import javax.swing.*;

import com.game.data.GameData;
import com.game.model.GameCharacter;
import com.game.resource.ResourceManager;

public class CharacterGalleryPanel extends JPanel {
    private Long currentPlayerId; // ID của người chơi
    private Map<Long, GameCharacter> ownedCharacters = new HashMap<>();
    private Timer animationTimer;
    private int frameIndex = 0;
    int frameDelay = 10, frameTick = 0;
    BufferedImage currentImage;

    // Define all available character classes that should be displayed
    private static final String[] ALL_CLASS_NAMES = {
        "LangKhach", "Samurai", "Tanker", "Assassin", "Vampire"
    };

    private static final Map<String, GameCharacter> defaultClassTemplates = new HashMap<>();

    static {
        defaultClassTemplates.put("LangKhach", new GameCharacter(-1L, GameData.user.getId(), "Ẩn danh", 0, 1, 0, 0, "LangKhach"));
        defaultClassTemplates.put("Samurai", new GameCharacter(-1L, GameData.user.getId(), "Ẩn danh", 0, 1, 0, 0, "Samurai"));
        defaultClassTemplates.put("Tanker", new GameCharacter(-1L, GameData.user.getId(), "Ẩn danh", 0, 1, 0, 0, "Tanker"));
        defaultClassTemplates.put("Assassin", new GameCharacter(-1L, GameData.user.getId(), "Ẩn danh", 0, 1, 0, 0, "Assassin"));
        defaultClassTemplates.put("Vampire", new GameCharacter(-1L, GameData.user.getId(), "Ẩn danh", 0, 1, 0, 0, "Vampire"));
    }

    public CharacterGalleryPanel(Long currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
        loadOwnedCharacters();
        setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
        contentPanel.setBackground(Color.DARK_GRAY);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 30, 20));

        for (String className : ALL_CLASS_NAMES) {
            GameCharacter characterToShow = null;

            // Nếu người chơi đã sở hữu class này
            for (GameCharacter c : GameData.character) {
                if (c.getClassName().equalsIgnoreCase(className) && c.getUserId().equals(currentPlayerId)) {
                    characterToShow = c;
                    break;
                }
            }

            // Nếu chưa sở hữu, lấy nhân vật mẫu
            if (characterToShow == null) {
                characterToShow = defaultClassTemplates.get(className);
            }

            if (characterToShow != null) {
                JPanel charPanel = createCharacterCard(characterToShow);
                contentPanel.add(charPanel);
                contentPanel.add(Box.createRigidArea(new Dimension(20, 0)));
            }
        }


        JScrollPane scrollPane = new JScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(null);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

    }

    private void loadOwnedCharacters() {
        for (GameCharacter c : GameData.character) {
            if (c.getUserId().equals(currentPlayerId)) {
                ownedCharacters.put(c.getId(), c);
            }
        }
    }

    private JPanel createCharacterCard(GameCharacter character) {
        boolean isOwned = character.getId() > 0;

        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(250, 300));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(isOwned ? new Color(40, 40, 40) : new Color(40, 40, 40, 120));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY), // border bên ngoài
            BorderFactory.createEmptyBorder(8, 25, 20, 25) // padding bên trong
        ));

        JLabel classLabel = new JLabel(character.getClassName(), SwingConstants.CENTER);
        classLabel.setForeground(Color.WHITE);
        classLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        classLabel.setFont(new Font("Arial", Font.BOLD, 14));
        classLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));

        JLabel animationLabel = new JLabel(); // sẽ set ảnh động
        animationLabel.setPreferredSize(new Dimension(128, 128));
        animationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        BufferedImage[] frames = ResourceManager.getPlayerAnimation("assets/Run/LangKhach", "idle", 6);
        if (frames != null && frames.length > 0) {
            BufferedImage originalImage = frames[0];

            int width = 80;
            int height = 100;

            // Image scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            // ImageIcon resizedIcon = new ImageIcon(scaledImage);
            // animationLabel.setIcon(resizedIcon);
            
            // Cập nhật lại size cho JLabel nếu cần
            startAnimationTimer(frames, animationLabel, width, height);
        }

        JLabel nameLabel = new JLabel(isOwned ? character.getName() : "???", SwingConstants.CENTER);
        nameLabel.setForeground(Color.CYAN);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));

        JLabel levelLabel = new JLabel(isOwned ? "Level " + character.getLevel() : "", SwingConstants.CENTER);
        levelLabel.setForeground(Color.YELLOW);
        levelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton selectButton = new JButton("Chọn");
        selectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        selectButton.setEnabled(isOwned);
        selectButton.addActionListener(e -> {
            if (isOwned) {
                GameData.character = new ArrayList<>();
                GameData.character.add(character);
                JOptionPane.showMessageDialog(this, "Đã chọn nhân vật: " + character.getName());
                System.out.println("Đã chọn nhân vật: " + GameData.character);
            } else {
                JOptionPane.showMessageDialog(this, "Bạn chưa sở hữu nhân vật này.");
            }
        });

        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(classLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(animationLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(nameLabel);
        panel.add(levelLabel);
        panel.add(Box.createVerticalGlue());
        panel.add(selectButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        return panel;
    }

    private void startAnimationTimer(BufferedImage[] frames, JLabel label, int width, int height) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new java.util.TimerTask() {
            int localFrameIndex = 0;

            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    if (frames.length == 0) return;

                    BufferedImage frame = frames[localFrameIndex];
                    Image scaledImage = frame.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    ImageIcon resizedIcon = new ImageIcon(scaledImage);
                    label.setIcon(resizedIcon);
                    label.setPreferredSize(new Dimension(width, height));

                    localFrameIndex = (localFrameIndex + 1) % Math.min(frames.length, 6);
                });
            }
        }, 30, 100); // 100ms per frame ≈ 10 FPS
    }
}
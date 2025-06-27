package com.game.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.List;
import java.util.Timer;

import javax.swing.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.AccessFrame;
import com.game.GameWindow;
import com.game.data.GameData;
import com.game.resource.ResourceManager;
import com.game.model.*;

public class CharacterGalleryPanel extends JPanel {
    private Long currentPlayerId; // ID của người chơi
    private Map<Long, GameCharacter> ownedCharacters = new HashMap<>();
    int frameDelay = 10, frameTick = 0;
    BufferedImage currentImage;

    // Define all available character classes that should be displayed
    private static final String[] ALL_CLASS_NAMES = {
            "LangKhach", "Samurai", "Tanker", "Assassin", "Vampire"
    };

    private static final Map<String, GameCharacter> defaultClassTemplates = new HashMap<>();

    static {
        defaultClassTemplates.put("LangKhach",
                new GameCharacter(-1L, GameData.user.getId(), "Ẩn danh", 0, 1, 0, 0, "LangKhach"));
        defaultClassTemplates.put("Samurai",
                new GameCharacter(-1L, GameData.user.getId(), "Ẩn danh", 0, 1, 0, 0, "Samurai"));
        defaultClassTemplates.put("Tanker",
                new GameCharacter(-1L, GameData.user.getId(), "Ẩn danh", 0, 1, 0, 0, "Tanker"));
        defaultClassTemplates.put("Assassin",
                new GameCharacter(-1L, GameData.user.getId(), "Ẩn danh", 0, 1, 0, 0, "Assassin"));
        defaultClassTemplates.put("Vampire",
                new GameCharacter(-1L, GameData.user.getId(), "Ẩn danh", 0, 1, 0, 0, "Vampire"));
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

        JScrollPane scrollPane = new JScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
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

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Nền panel
                Color bg = isOwned ? new Color(40, 40, 40) : new Color(40, 40, 40, 120);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

                super.paintComponent(g2);
                g2.dispose();
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        panel.setPreferredSize(new Dimension(250, 300));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 25, 20, 25));

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
            int width = 80;
            int height = 100;
            
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

        JButton selectButton = new JButton(isOwned ? "Chọn" : "Tạo");
        selectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        selectButton.addActionListener(e -> {
            if (isOwned) {
                GameData.character = new ArrayList<>();
                GameData.character.add(character);
                JOptionPane.showMessageDialog(this, "Đã chọn nhân vật: " + character.getName());
                try {
                    // Tải dữ liệu nhân vật từ API
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu nhân vật: " + ex.getMessage());
                    return;
                }

                // ✅ Mở giao diện chính
                AccessFrame.getInstance().dispose();
                GameWindow gameWindow = new GameWindow();
                gameWindow.setVisible(true);
            } else {
                String name = JOptionPane.showInputDialog(this, "Nhập tên cho nhân vật mới:");
                if (name != null && !name.trim().isEmpty()) {
                    try {
                        // Chuẩn bị dữ liệu gửi lên API
                        Map<String, Object> req = new HashMap<>();
                        req.put("userId", currentPlayerId);
                        req.put("name", name.trim());
                        req.put("level", 1);
                        req.put("skillPoint", 1);
                        req.put("gold", 1000);
                        req.put("exp", 0);
                        req.put("className", character.getClassName());

                        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(req);

                        java.net.URL url = new java.net.URL("http://localhost:8080/api/characters/");
                        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setRequestProperty("Authorization", "Bearer " + GameData.token);
                        conn.setDoOutput(true);

                        try (java.io.OutputStream os = conn.getOutputStream()) {
                            os.write(json.getBytes("UTF-8"));
                        }

                        int code = conn.getResponseCode();
                        if (code == 200 || code == 201) {
                            JOptionPane.showMessageDialog(this, "Tạo nhân vật thành công!");
                            // Có thể reload lại danh sách nhân vật ở đây nếu muốn
                            GameData.character = new ArrayList<>();
                            HttpClient client = HttpClient.newHttpClient();
                            ObjectMapper mapper = new ObjectMapper();
                            String characterUrl = "http://localhost:8080/api/characters/" + currentPlayerId;
                            HttpRequest request1 = HttpRequest.newBuilder()
                            .uri(URI.create(characterUrl))
                            .header("Authorization", "Bearer " + GameData.token)
                            .GET()
                            .build();
                            HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
                            if (response1.body() != null && !response1.body().trim().isEmpty()
                                    && !response1.body().trim().equalsIgnoreCase("null")) {
                                List<GameCharacter> characters = Arrays
                                        .asList(mapper.readValue(response1.body(), GameCharacter[].class));
                                GameData.character = characters;
                            } else {
                                GameData.character = new ArrayList<>();
                            }

                            AccessFrame.getInstance().showCharacter(currentPlayerId);
                        } else {
                            JOptionPane.showMessageDialog(this, "Tạo nhân vật thất bại! Mã lỗi: " + code);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Lỗi khi tạo nhân vật: " + ex.getMessage());
                    }
                }
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

    private void loadData() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();
        
        Long characterId = GameData.character.get(0).getId();

        // Lấy character skills
        String csUrl = "http://localhost:8080/api/character_skills/" + characterId;
        HttpRequest request3 = HttpRequest.newBuilder()
        .uri(URI.create(csUrl))
        .header("Authorization", "Bearer " + GameData.token)
        .GET()
        .build();
        HttpResponse<String> response3 = client.send(request3, HttpResponse.BodyHandlers.ofString());
        if (response3.body() != null && !response3.body().trim().isEmpty()
                && !response3.body().trim().equalsIgnoreCase("null")) {
            List<GameCharacterSkill> apiCharacterSkill = Arrays.asList(mapper.readValue(response3.body(), GameCharacterSkill[].class));
            GameData.loadCharacterSkills(apiCharacterSkill);
        } else {
            GameData.characterSkills = new ArrayList<>();
        }

        // Lấy inventory
        String invUrl = "http://localhost:8080/api/inventory/" + characterId;
        HttpRequest request4 = HttpRequest.newBuilder()
        .uri(URI.create(invUrl))
        .header("Authorization", "Bearer " + GameData.token)
        .GET()
        .build();
        HttpResponse<String> response4 = client.send(request4, HttpResponse.BodyHandlers.ofString());
        if (response4.body() != null && !response4.body().trim().isEmpty()
                && !response4.body().trim().equalsIgnoreCase("null")) {
            GameData.inventory = Arrays.asList(mapper.readValue(response4.body(), GameInventory[].class));
        } else {
            GameData.inventory = new ArrayList<>();
        }

        // Lấy item
        String itUrl = "http://localhost:8080/api/item/";
        HttpRequest request5 = HttpRequest.newBuilder()
        .uri(URI.create(itUrl))
        .header("Authorization", "Bearer " + GameData.token)
        .GET()
        .build();
        HttpResponse<String> response5 = client.send(request5, HttpResponse.BodyHandlers.ofString());
        if (response5.body() != null && !response5.body().trim().isEmpty()
                && !response5.body().trim().equalsIgnoreCase("null")) {
            GameData.item = Arrays.asList(mapper.readValue(response5.body(), GameItem[].class));
        } else {
            GameData.item = new ArrayList<>();
        }

        // Lấy item instance
        String itiUrl = "http://localhost:8080/api/iteminstance/";
        HttpRequest request6 = HttpRequest.newBuilder()
        .uri(URI.create(itiUrl))
        .header("Authorization", "Bearer " + GameData.token)
        .GET()
        .build();
        HttpResponse<String> response6 = client.send(request6, HttpResponse.BodyHandlers.ofString());
        if (response6.body() != null && !response6.body().trim().isEmpty()
                && !response6.body().trim().equalsIgnoreCase("null")) {
            List<GameItemInstance> apiInstances = Arrays.asList(mapper.readValue(response6.body(), GameItemInstance[].class));
            GameData.loadItemInstances(apiInstances);
        } else {
            GameData.itemInstance = new ArrayList<>();
        }

        // Lấy map
        String mapUrl = "http://localhost:8080/api/map/";
        HttpRequest request7 = HttpRequest.newBuilder()
        .uri(URI.create(mapUrl))
        .header("Authorization", "Bearer " + GameData.token)
        .GET()
        .build();
        HttpResponse<String> response7 = client.send(request7, HttpResponse.BodyHandlers.ofString());
        GameData.map = Arrays.asList(mapper.readValue(response7.body(), GameMap[].class));

        // Lấy monster
        String msUrl = "http://localhost:8080/api/monster/";
        HttpRequest request8 = HttpRequest.newBuilder()
        .uri(URI.create(msUrl))
        .header("Authorization", "Bearer " + GameData.token)
        .GET()
        .build();
        HttpResponse<String> response8 = client.send(request8, HttpResponse.BodyHandlers.ofString());
        GameData.monster = Arrays.asList(mapper.readValue(response8.body(), GameMonster[].class));

        // Lấy monster drop
        String msdrUrl = "http://localhost:8080/api/monsterdrop/";
        HttpRequest request9 = HttpRequest.newBuilder()
        .uri(URI.create(msdrUrl))
        .header("Authorization", "Bearer " + GameData.token)
        .GET()
        .build();
        HttpResponse<String> response9 = client.send(request9, HttpResponse.BodyHandlers.ofString());
        GameData.monsterDrop = Arrays.asList(mapper.readValue(response9.body(), GameMonsterDrop[].class));

        // Lấy skills
        String skUrl = "http://localhost:8080/api/skill/";
        HttpRequest request10 = HttpRequest.newBuilder()
        .uri(URI.create(skUrl))
        .header("Authorization", "Bearer " + GameData.token)
        .GET()
        .build();
        HttpResponse<String> response10 = client.send(request10, HttpResponse.BodyHandlers.ofString());
        GameData.skills = Arrays.asList(mapper.readValue(response10.body(), GameSkill[].class));

        // Lấy skill update requirements
        String skuUrl = "http://localhost:8080/api/skillupdate/";
        HttpRequest request11 = HttpRequest.newBuilder()
        .uri(URI.create(skuUrl))
        .header("Authorization", "Bearer " + GameData.token)
        .GET()
        .build();
        HttpResponse<String> response11 = client.send(request11, HttpResponse.BodyHandlers.ofString());
        GameData.skillUpdate = Arrays.asList(mapper.readValue(response11.body(), GameSkillUpdateRequirements[].class));

        // Lấy thuoctinh
        String ttUrl = "http://localhost:8080/api/thuoctinh/";
        HttpRequest request12 = HttpRequest.newBuilder()
        .uri(URI.create(ttUrl))
        .header("Authorization", "Bearer " + GameData.token)
        .GET()
        .build();
        HttpResponse<String> response12 = client.send(request12, HttpResponse.BodyHandlers.ofString());
        GameData.thuoctinh = Arrays.asList(mapper.readValue(response12.body(), GameThuocTinh[].class));
    }

    private void startAnimationTimer(BufferedImage[] frames, JLabel label, int width, int height) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new java.util.TimerTask() {
            int localFrameIndex = 0;

            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    if (frames.length == 0)
                        return;

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
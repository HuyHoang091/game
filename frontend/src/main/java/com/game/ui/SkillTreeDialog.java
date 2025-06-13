package com.game.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import com.game.data.GameData;
import com.game.model.*;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;
import org.w3c.dom.events.MouseEvent;

import java.util.ArrayList;
import javax.swing.border.EmptyBorder;

public class SkillTreeDialog extends JDialog {
    private static final int DIALOG_WIDTH = 800;
    private static final int DIALOG_HEIGHT = 600;
    private static final int SKILL_ICON_SIZE = 64;
    private static final Color UNLEARNED_COLOR = new Color(50, 50, 50, 200);
    private static final Color LEARNED_COLOR = new Color(0, 0, 0, 150);
    
    private JPanel mainPanel;
    private JLabel pointLabel;
    private JLabel goldLabel;
    private Long characterId;
    private Map<Long, GameCharacterSkill> characterSkills = new HashMap<>();
    private Map<Long, BufferedImage> skillIcons = new HashMap<>();

    public SkillTreeDialog(JFrame parent, Long characterId) {
        super(parent, "Cây Kỹ Năng", true);
        this.characterId = characterId;
        initializeDialog();
        loadCharacterSkills();
        createSkillTree();
    }

    private void initializeDialog() {
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setLayout(null);

        // Tạo thanh tiêu đề
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(40, 40, 40));
        titlePanel.setBounds(0, 0, DIALOG_WIDTH, 30);
        
        JLabel titleLabel = new JLabel("Kỹ Năng");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        titlePanel.add(titleLabel, BorderLayout.WEST);

        // Thêm panel điểm kỹ năng và vàng ở góc phải title bar
        GameCharacter character = GameData.getCharacterById(characterId);
        if (character != null) {
            JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
            statsPanel.setOpaque(false);
            
            pointLabel = new JLabel("Điểm kỹ năng: " + character.getSkillPoint());
            goldLabel = new JLabel("Vàng: " + character.getGold());
            
            pointLabel.setForeground(Color.CYAN);
            goldLabel.setForeground(Color.YELLOW);
            
            pointLabel.setFont(new Font("Arial", Font.BOLD, 12));
            goldLabel.setFont(new Font("Arial", Font.BOLD, 12));

            goldLabel.setBorder(new EmptyBorder(0, 0, 0, 20));
            
            statsPanel.add(pointLabel);
            statsPanel.add(goldLabel);
            titlePanel.add(statsPanel, BorderLayout.EAST);
        }

        // Panel chính chứa các skill
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(0, 4, 15, 15)); // 4 cột, spacing 15px
        mainPanel.setBackground(new Color(30, 30, 30));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ScrollPane cho main panel
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.setBounds(0, 30, DIALOG_WIDTH, DIALOG_HEIGHT - 30);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Nút đóng
        JButton closeButton = new JButton("×");
        closeButton.setBounds(DIALOG_WIDTH - 30, 0, 30, 30);
        styleCloseButton(closeButton);
        closeButton.addActionListener(e -> {
            GamePanel.getInstance().setPaused(false);
            setVisible(false);
            dispose();
        });

        add(titlePanel);
        add(scrollPane);
        add(closeButton);
    }

    private void loadCharacterSkills() {
        // Load character skills vào map để dễ truy cập
        if (GameData.characterSkills != null) {
            for (GameCharacterSkill cs : GameData.characterSkills) {
                if (cs.getCharacterId().equals(characterId)) {
                    characterSkills.put(cs.getSkillId(), cs);
                }
            }
        }
    }

    private void createSkillTree() {
        for (GameCharacter character : GameData.character) {
            if (character.getId().equals(characterId)) {
                // Lọc skill theo class của character
                GameData.skills.stream()
                    .filter(skill -> skill.getClassName().equals(character.getClassName()))
                    .forEach(skill -> {
                        JPanel skillPanel = createSkillPanel(skill);
                        mainPanel.add(skillPanel);
                    });
            }
        }
    }

    private JPanel createSkillPanel(GameSkill skill) {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(180, 280)); // Kích thước cố định cho mỗi skill
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(characterSkills.containsKey(skill.getId()) ? 
                          LEARNED_COLOR : UNLEARNED_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 100)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Icon section
        JPanel iconPanel = new JPanel();
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(180, SKILL_ICON_SIZE + 20));
        iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.Y_AXIS));
        
        try {
            BufferedImage icon = ImageIO.read(getClass().getClassLoader().getResource(skill.getIcon()));
            JLabel iconLabel = new JLabel(new ImageIcon(icon.getScaledInstance(
                SKILL_ICON_SIZE, SKILL_ICON_SIZE, Image.SCALE_SMOOTH)));
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            iconPanel.add(iconLabel);
            iconPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        } catch (IOException e) {
            System.err.println("Không thể tải icon cho skill: " + skill.getName());
        }
        
        // Info section
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        
        addLabel(skill.getName(), infoPanel, new Font("Arial", Font.BOLD, 14), Color.WHITE);
        addLabel("Sát thương: " + (int)(skill.getDamage() * 100) + "%", infoPanel, 
                 new Font("Arial", Font.PLAIN, 12), Color.ORANGE);
        
        // Mô tả skill với word wrap
        JTextArea descArea = new JTextArea("Mô tả: " + skill.getMota());
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);
        descArea.setOpaque(false);
        descArea.setEditable(false);
        descArea.setForeground(Color.LIGHT_GRAY);
        descArea.setFont(new Font("Arial", Font.PLAIN, 12));
        descArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        descArea.setMaximumSize(new Dimension(160, 60));
        infoPanel.add(descArea);
        
        // Button section
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setMaximumSize(new Dimension(160, 100));
        
        // Add all sections
        panel.add(iconPanel);
        panel.add(infoPanel);
        panel.add(Box.createVerticalGlue());
        panel.add(buttonPanel);
        
        // Add buttons based on skill state
        addSkillButtons(skill, buttonPanel);

        return panel;
    }

    private void addLabel(String text, JPanel panel, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
    }

    private void styleCloseButton(JButton button) {
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(50, 50, 50));
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(180, 0, 0));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(50, 50, 50));
            }
        });
    }

    private void addSkillButtons(GameSkill skill, JPanel buttonPanel) {
        GameCharacterSkill characterSkill = characterSkills.get(skill.getId());
        GameCharacter character = GameData.getCharacterById(characterId);

        if (characterSkill != null) {
            // Đã học skill
            addLabel("Cấp độ: " + characterSkill.getLevel(), buttonPanel, 
                     new Font("Arial", Font.PLAIN, 12), Color.WHITE);

            // Nút trang bị
            JButton equipButton = new JButton(characterSkill.getSlot() > 0 ? "Đổi slot" : "Trang bị");
            equipButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            equipButton.addActionListener(e -> showSlotSelectionDialog(characterSkill));
            buttonPanel.add(equipButton);
            buttonPanel.add(Box.createVerticalStrut(10));

            // Kiểm tra điều kiện tăng cấp
            GameSkillUpdateRequirements requirements = GameData.skillUpdate.stream()
                .filter(req -> req.getSkillId().equals(skill.getId()) && 
                             req.getLevel() == characterSkill.getLevel() + 1)
                .findFirst()
                .orElse(null);

            if (requirements != null && character != null) {
                // Hiển thị yêu cầu tăng cấp
                addLabel(String.format(
                    "<html>Yêu cầu: Cấp %d, %d điểm, %d vàng</html>",
                    requirements.getLevelRequired(),
                    requirements.getPointRequired(),
                    requirements.getGoldRequired()
                ), buttonPanel, new Font("Arial", Font.PLAIN, 12), Color.GRAY);

                // Kiểm tra đủ điều kiện
                boolean canUpgrade = character.getLevel() >= requirements.getLevelRequired() &&
                                   character.getSkillPoint() >= requirements.getPointRequired() &&
                                   character.getGold() >= requirements.getGoldRequired();

                if (canUpgrade) {
                    JButton upgradeButton = new JButton("Tăng cấp");
                    upgradeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                    upgradeButton.addActionListener(e -> upgradeSkill(characterSkill, requirements));
                    buttonPanel.add(upgradeButton);
                }
            }
        } else {
            // Chưa học skill
            addLabel("Yêu cầu cấp: " + skill.getLevelRequired(), buttonPanel, 
                     new Font("Arial", Font.PLAIN, 12), Color.GRAY);

            if (character != null && character.getLevel() >= skill.getLevelRequired()) {
                JButton learnButton = new JButton("Học kỹ năng");
                learnButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                learnButton.addActionListener(e -> learnSkill(skill));
                buttonPanel.add(learnButton);
            }
        }
    }

    private void learnSkill(GameSkill skill) {
        try {
            // Check if GameData.characterSkills is null or immutable
            if (GameData.characterSkills == null) {
                GameData.characterSkills = new ArrayList<>();
            } else if (!(GameData.characterSkills instanceof ArrayList)) {
                // Convert to ArrayList if it's an immutable List
                GameData.characterSkills = new ArrayList<>(GameData.characterSkills);
            }

            // Tìm ID lớn nhất trong GameCharacterSkill để tạo ID mới
            Long newId = GameData.characterSkills.stream()
                .mapToLong(GameCharacterSkill::getId)
                .max()
                .orElse(0) + 1;

            // Tạo GameCharacterSkill mới
            GameCharacterSkill newCharacterSkill = new GameCharacterSkill();
            newCharacterSkill.setId(newId);
            newCharacterSkill.setCharacterId(characterId);
            newCharacterSkill.setSkillId(skill.getId());
            newCharacterSkill.setLevel(1);
            newCharacterSkill.setSlot(0); // Chưa gán vào ô nào

            // Thêm vào GameData
            GameData.characterSkills.add(newCharacterSkill);
            
            // Thêm vào map local để hiển thị
            characterSkills.put(skill.getId(), newCharacterSkill);

            // Thông báo thành công
            JOptionPane.showMessageDialog(this, 
                "Đã học kỹ năng: " + skill.getName(), 
                "Thành công", 
                JOptionPane.INFORMATION_MESSAGE);

            // Cập nhật giao diện
            mainPanel.removeAll();
            loadCharacterSkills();   // cập nhật lại map
            createSkillTree();       // vẽ lại các skill panel
            mainPanel.revalidate();
            mainPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Lỗi khi học kỹ năng: " + e.getMessage(), 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStatsDisplay() {
        GameCharacter character = GameData.getCharacterById(characterId);
        if (character != null) {
            pointLabel.setText("Điểm kỹ năng: " + character.getSkillPoint());
            goldLabel.setText("Vàng: " + character.getGold());
        }
    }

    private void upgradeSkill(GameCharacterSkill characterSkill, GameSkillUpdateRequirements requirements) {
        try {
            GameCharacter character = GameData.getCharacterById(characterId);
            if (character != null) {
                if (character.getSkillPoint() >= requirements.getPointRequired() &&
                    character.getGold() >= requirements.getGoldRequired()) {
                    
                    // Cập nhật skillPoint và gold của character
                    character.setSkillPoint(character.getSkillPoint() - requirements.getPointRequired());
                    character.setGold(character.getGold() - requirements.getGoldRequired());
                    
                    // Tăng cấp kỹ năng
                    characterSkill.setLevel(characterSkill.getLevel() + 1);
                    
                    // Thông báo thành công
                    JOptionPane.showMessageDialog(this, 
                        "Nâng cấp kỹ năng thành công!\n" +
                        "Điểm kỹ năng còn lại: " + character.getSkillPoint() + "\n" +
                        "Vàng còn lại: " + character.getGold(),
                        "Thành công", 
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    mainPanel.removeAll();
                    loadCharacterSkills();   // cập nhật lại map
                    createSkillTree();       // vẽ lại các skill panel
                    updateStatsDisplay();
                    mainPanel.revalidate();
                    mainPanel.repaint();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Không đủ điểm kỹ năng hoặc vàng để nâng cấp!", 
                        "Không đủ điều kiện", 
                        JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Lỗi khi nâng cấp kỹ năng: " + e.getMessage(), 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSlotSelectionDialog(GameCharacterSkill characterSkill) {
        JDialog slotDialog = new JDialog(this, "Chọn slot", true);
        slotDialog.setLayout(new GridLayout(2, 2, 10, 10));
        slotDialog.setSize(200, 200);
        slotDialog.setLocationRelativeTo(this);

        // Tạo 4 nút cho 4 slot
        for (int i = 1; i <= 4; i++) {
            final int slot = i;
            JButton slotButton = new JButton("Slot " + i);
            
            // Kiểm tra xem slot có skill nào không
            GameCharacterSkill occupyingSkill = GameData.characterSkills.stream()
                .filter(cs -> cs.getCharacterId().equals(characterId) && 
                             cs.getSlot() == slot && 
                             !cs.getId().equals(characterSkill.getId()))
                .findFirst()
                .orElse(null);
            
            if (occupyingSkill != null) {
                // Hiển thị tên skill đang chiếm slot
                GameSkill skill = GameData.skills.stream()
                    .filter(s -> s.getId().equals(occupyingSkill.getSkillId()))
                    .findFirst()
                    .orElse(null);
                
                if (skill != null) {
                    slotButton.setText("Slot " + i + " (" + skill.getName() + ")");
                }
            }
            
            slotButton.addActionListener(e -> {
                // Gỡ skill khỏi slot cũ nếu có
                if (characterSkill.getSlot() > 0) {
                    characterSkill.setSlot(0);
                }
                
                // Gỡ skill đang chiếm slot mới (nếu có)
                if (occupyingSkill != null) {
                    occupyingSkill.setSlot(0);
                }
                
                // Cập nhật slot mới cho skill hiện tại
                characterSkill.setSlot(slot);

                // Cập nhật giao diện game
                GamePanel gamePanel = GamePanel.getInstance();
                if (gamePanel != null) {
                    gamePanel.loadSkillIcons();
                    gamePanel.loadSkills();
                }
                
                slotDialog.dispose();
                JOptionPane.showMessageDialog(this, 
                    "Đã gán kỹ năng vào slot " + slot, 
                    "Thành công", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Cập nhật lại giao diện skill tree
                mainPanel.removeAll();
                loadCharacterSkills();
                createSkillTree();
                mainPanel.revalidate();
                mainPanel.repaint();
            });
            
            slotDialog.add(slotButton);
        }

        slotDialog.setVisible(true);
    }
}
package com.game.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

// Import thêm thư viện Icon nếu có
// import com.game.resources.GameIcons; // Ví dụ: Lớp chứa các biểu tượng game

import com.game.GameWindow;
import com.game.core.GameLoop;
import com.game.core.KeyBindingConfig;

public class SettingsPanel extends JPanel {
    private GameWindow gameWindow;
    private JSlider volumeSlider;
    private JComboBox<String> resolutionCombo;
    private JCheckBox fullscreenCheck;
    private float volume;
    private static SettingsPanel instance;

    // Các màu sắc và font được định nghĩa để dễ dàng quản lý
    // Đã điều chỉnh để tối hơn và có các điểm nhấn màu sắc rõ rệt
    private static final Color BG_VERY_DARK = new Color(15, 15, 20); // Nền cực tối
    private static final Color PANEL_BG_DEEP = new Color(25, 25, 30); // Nền panel sâu hơn
    private static final Color ACCENT_GOLD = new Color(255, 190, 0); // Vàng gold rực rỡ hơn
    private static final Color ACCENT_TEAL = new Color(0, 160, 140); // Xanh teal đậm và rõ nét
    private static final Color TEXT_PRIMARY = new Color(220, 220, 220); // Chữ chính sáng hơn một chút
    private static final Color TEXT_SECONDARY = new Color(150, 150, 150); // Chữ phụ
    private static final Color BORDER_COLOR_LIGHT = new Color(60, 60, 70); // Màu viền nhẹ
    private static final Color HOVER_COLOR_ACCENT = new Color(0, 200, 180); // Màu hover phát sáng hơn

    // Font chữ
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 42); // Giả sử dùng Arial cho đơn giản
    private static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 20);
    private static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 17);
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 18);

    public SettingsPanel(GameWindow gameWindow) {
        instance = this;
        this.gameWindow = gameWindow;

        // --- Cài đặt cơ bản cho SettingsPanel ---
        setBackground(BG_VERY_DARK);
        setLayout(new BorderLayout());

        // --- Tạo một panel nội dung để đặt tất cả các controls vào đó ---
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                // Tạo gradient từ trên xuống dưới
                GradientPaint gp = new GradientPaint(0, 0, PANEL_BG_DEEP, 0, getHeight(), new Color(30, 30, 35));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(40, 60, 40, 60)); // Đệm lớn hơn

        // --- Tiêu đề trang cài đặt ---
        JLabel titleLabel = new JLabel("CÀI ĐẶT");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(new Color(180, 200, 220));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(50)); // Khoảng cách lớn hơn

        // --- Panel cho các cài đặt hiển thị/âm thanh (Controls Panel) ---
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new GridLayout(0, 2, 25, 25)); // Khoảng cách lớn hơn
        controlsPanel.setBackground(PANEL_BG_DEEP); // Đặt nền cho panel con
        controlsPanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(BORDER_COLOR_LIGHT, 2),
                "Cài đặt chung",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                HEADER_FONT,
                TEXT_SECONDARY
        ));

        // Âm lượng
        JLabel volumeLabel = new JLabel("Âm lượng:", getScaledIcon("/assets/bossroom.png", 24, 24), JLabel.LEFT);
        volumeLabel.setFont(LABEL_FONT);
        volumeLabel.setForeground(TEXT_PRIMARY);
        volumeLabel.setIconTextGap(10);
        volumeLabel.setVerticalAlignment(SwingConstants.CENTER);
        volumeLabel.setHorizontalAlignment(SwingConstants.LEFT);
        controlsPanel.add(volumeLabel);

        volumeSlider = new JSlider(0, 100, 70);
        volumeSlider.setBackground(PANEL_BG_DEEP); // Nền slider
        volumeSlider.setForeground(ACCENT_GOLD); // Màu thanh trượt
        volumeSlider.setPaintTicks(true);
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setToolTipText("Điều chỉnh âm lượng tổng thể");
        volumeSlider.addChangeListener(e -> {
            volume = volumeSlider.getValue() / 100f;
            if (GameWindow.getInstance() != null && GameWindow.getInstance() != null) {
                 GameLoop gameLoop = GamePanel.getInstance().getGameLoop();
                 if (gameLoop != null && gameLoop.getMusicPlayer() != null) {
                    gameLoop.getMusicPlayer().setVolume(volume);
                 }
            }
        });
        controlsPanel.add(volumeSlider);

        // Độ phân giải
        JLabel resolutionLabel = new JLabel("Độ phân giải:", getScaledIcon("/assets/bossroom.png", 24, 24), JLabel.LEFT);
        resolutionLabel.setFont(LABEL_FONT);
        resolutionLabel.setForeground(TEXT_PRIMARY);
        resolutionLabel.setIconTextGap(10);
        resolutionLabel.setVerticalAlignment(SwingConstants.CENTER);
        resolutionLabel.setHorizontalAlignment(SwingConstants.LEFT);
        controlsPanel.add(resolutionLabel);

        String[] resolutions = {"1920x820", "1200x700", "800x600"};
        resolutionCombo = new JComboBox<>(resolutions);
        resolutionCombo.setSelectedItem("1200x700");
        resolutionCombo.setBackground(new Color(45, 45, 55)); // Nền ComboBox
        resolutionCombo.setForeground(TEXT_PRIMARY);
        resolutionCombo.setFont(LABEL_FONT);
        resolutionCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBackground(isSelected ? new Color(70, 70, 80) : new Color(45, 45, 55)); // Màu khi chọn
                label.setForeground(TEXT_PRIMARY);
                label.setBorder(new EmptyBorder(5, 10, 5, 10)); // Padding cho item trong combobox
                return label;
            }
        });
        resolutionCombo.addActionListener(e -> {
            String selected = (String) resolutionCombo.getSelectedItem();
            if (selected != null) {
                String[] parts = selected.split("x");
                int width = Integer.parseInt(parts[0]);
                int height = Integer.parseInt(parts[1]);
                GameWindow gw = GameWindow.getInstance();
                if (gw != null && !gw.isFullScreen()) {
                    gw.setSize(width, height);
                    gw.setLocationRelativeTo(null);
                }
            }
        });
        controlsPanel.add(resolutionCombo);

        // Toàn màn hình
        JLabel fullscreenLabel = new JLabel("Toàn màn hình:", getScaledIcon("/assets/bossroom.png", 24, 24), JLabel.LEFT);
        fullscreenLabel.setFont(LABEL_FONT);
        fullscreenLabel.setForeground(TEXT_PRIMARY);
        fullscreenLabel.setIconTextGap(10);
        fullscreenLabel.setVerticalAlignment(SwingConstants.CENTER);
        fullscreenLabel.setHorizontalAlignment(SwingConstants.LEFT);
        controlsPanel.add(fullscreenLabel);

        fullscreenCheck = new JCheckBox();
        fullscreenCheck.setBackground(PANEL_BG_DEEP);
        fullscreenCheck.setForeground(ACCENT_GOLD); // Màu dấu tích
        fullscreenCheck.setFocusPainted(false);
        fullscreenCheck.addActionListener(e -> {
            GameWindow gw = GameWindow.getInstance();
            if (gw != null) {
                boolean wantFullscreen = fullscreenCheck.isSelected();
                if (wantFullscreen != gw.isFullScreen()) {
                    gw.toggleFullScreen();
                    resolutionCombo.setEnabled(!wantFullscreen);
                }
            }
        });
        controlsPanel.add(fullscreenCheck);
        contentPanel.add(controlsPanel);
        contentPanel.add(Box.createVerticalStrut(40));

        // --- Panel cho cấu hình phím điều khiển (Key Bindings Panel) ---
        JPanel keyBindingsPanel = new JPanel(new GridLayout(0, 2, 20, 15)); // Khoảng cách lớn hơn
        keyBindingsPanel.setBackground(PANEL_BG_DEEP); // Đặt nền cho panel con
        keyBindingsPanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(BORDER_COLOR_LIGHT, 2),
                "Cấu hình phím điều khiển",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                HEADER_FONT,
                TEXT_SECONDARY
        ));

        // Thêm các nút cấu hình phím
        for (String action : KeyBindingConfig.getAllBindings().keySet()) {
            JLabel label = new JLabel(action + ":");
            label.setFont(LABEL_FONT);
            label.setForeground(TEXT_PRIMARY);
            keyBindingsPanel.add(label);

            JButton button = new CustomJButton(KeyEvent.getKeyText(KeyBindingConfig.getKey(action))); // Sử dụng CustomJButton
            styleButton(button);
            button.setPreferredSize(new Dimension(150, 40)); // Kích thước nút lớn hơn
            button.addActionListener(e -> {
                button.setText("Nhấn phím...");
                KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
                    public boolean dispatchKeyEvent(KeyEvent ke) {
                        if (ke.getID() == KeyEvent.KEY_PRESSED) {
                            int newKey = ke.getKeyCode();

                            for (Map.Entry<String, Integer> entry : KeyBindingConfig.getAllBindings().entrySet()) {
                                if (entry.getValue() == newKey && !entry.getKey().equals(action)) {
                                    JOptionPane.showMessageDialog(SettingsPanel.this,
                                            "Phím này đã được gán cho hành động: " + entry.getKey(),
                                            "Trùng phím", JOptionPane.WARNING_MESSAGE);
                                    button.setText(KeyEvent.getKeyText(KeyBindingConfig.getKey(action)));
                                    KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
                                    return true;
                                }
                            }

                            KeyBindingConfig.setKey(action, newKey);
                            button.setText(KeyEvent.getKeyText(newKey));
                            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
                            return true;
                        }
                        return false;
                    }
                });
            });
            keyBindingsPanel.add(button);
        }
        contentPanel.add(keyBindingsPanel);
        contentPanel.add(Box.createVerticalStrut(40));

        // --- Nút Save Key Bindings ---
        JButton saveButton = new CustomJButton("LƯU CẤU HÌNH PHÍM"); // Sử dụng CustomJButton
        styleButton(saveButton);
        saveButton.setPreferredSize(new Dimension(250, 40)); 
        saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveButton.addActionListener(e -> {
            KeyBindingConfig.saveBindings();
            JOptionPane.showMessageDialog(this, "Đã lưu cấu hình phím thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });
        contentPanel.add(saveButton);
        contentPanel.add(Box.createVerticalStrut(30));

        // --- Nút điều hướng ---
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(PANEL_BG_DEEP);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0)); // Khoảng cách lớn hơn

        JButton backButton = new CustomJButton("QUAY LẠI"); // Sử dụng CustomJButton
        styleButton(backButton);
        backButton.addActionListener(e -> {
            gameWindow.goBack();
        });
        buttonPanel.add(backButton);

        JButton backToMenuButton = new CustomJButton("MENU CHÍNH"); // Sử dụng CustomJButton
        styleButton(backToMenuButton);
        backToMenuButton.addActionListener(e -> gameWindow.BackToMenu());
        buttonPanel.add(backToMenuButton);

        contentPanel.add(buttonPanel);
        contentPanel.add(Box.createVerticalGlue());

        // --- Bọc contentPanel vào JScrollPane ---
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(20); // Tăng tốc độ cuộn

        // Tùy chỉnh thanh cuộn
        scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());

        add(scrollPane, BorderLayout.CENTER);
    }

    public void registerEscAction() {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "closeSettings");
        getActionMap().put("closeSettings", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameWindow.goBack();
            }
        });
    }

    public void removeEscAction() {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke("ESCAPE"));
        getActionMap().remove("closeSettings");
    }

    // --- Phương thức để áp dụng style chung cho các nút ---
    private void styleButton(JButton button) {
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(new Color(0, 50, 80)); // Đặt màu nền ban đầu

        // Hiệu ứng hover và press
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(HOVER_COLOR_ACCENT); // Màu sáng hơn khi hover
            }
            @Override
            public void mouseExited(MouseEvent evt) {
                button.setBackground(new Color(0, 50, 80)); // Trở lại màu ban đầu
            }
            // Để có hiệu ứng press đẹp hơn, CustomJButton sẽ xử lý phần border
        });
    }

    // Phương thức giả lập đường dẫn icon
    private ImageIcon getScaledIcon(String path, int width, int height) {
        ImageIcon originalIcon = new ImageIcon(getClass().getResource(path));
        Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    public float getVolume() {
        return volume;
    }

    public static SettingsPanel getInstance() {
        return instance;
    }

    // --- Lớp JButton tùy chỉnh để có các góc bo tròn ---
    // Đặt lớp này vào file riêng (ví dụ: CustomJButton.java) để code gọn hơn
    private static class CustomJButton extends JButton {
        private static final int ARC_RADIUS = 15; // Bán kính bo góc
        private Color currentColor; // Để lưu giữ màu nền hiện tại

        public CustomJButton(String text) {
            super(text);
            setContentAreaFilled(false); // Quan trọng: Tắt fill mặc định để tự vẽ
            setFocusPainted(false); // Không vẽ viền focus mặc định
        }

        @Override
        public void setBackground(Color bg) {
            super.setBackground(bg);
            this.currentColor = bg; // Cập nhật màu nền hiện tại
            repaint(); // Vẽ lại khi màu nền thay đổi
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Vẽ nền bo góc
            g2.setColor(currentColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC_RADIUS, ARC_RADIUS);

            super.paintComponent(g2); // Vẽ chữ và icon
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Vẽ border bo góc
            if (getModel().isPressed()) {
                g2.setColor(new Color(0, 90, 80)); // Màu border khi bấm
                g2.setStroke(new BasicStroke(3)); // Độ dày border khi bấm
            } else {
                g2.setColor(new Color(0, 140, 120)); // Màu border bình thường
                g2.setStroke(new BasicStroke(2)); // Độ dày border bình thường
            }
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC_RADIUS, ARC_RADIUS);
            g2.dispose();
        }
    }
}
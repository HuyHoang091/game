package com.game.ui;

import javax.swing.*;
import java.awt.*;
import com.game.model.*;
import com.game.data.GameData;
import java.awt.event.KeyEvent;
import com.game.*;

public class StatsDialog extends JDialog {
    // Các thông số giao diện
    private static final int DIALOG_WIDTH = 300;
    private static final int DIALOG_HEIGHT = 250;
    private static final int CLOSE_BUTTON_SIZE = 20;
    
    // Các thành phần UI
    private JPanel mainPanel;
    private JPanel contentPanel;
    private JButton equipButton;
    private GameInventory currentInventory;
    GamePanel gamePanel;
    private InventoryPanel inventoryPanel;
    
    public StatsDialog(JFrame parent, InventoryPanel iPanel, GameItemInstance instance, GameItem item, String type) {
        super(parent, "Item Stats", true);
        initializeDialog(parent);
        setupMainPanel();
        setupCloseButton();
        setupContentPanel();
        this.inventoryPanel = iPanel;
        
        // Hiển thị thông tin item
        displayItemInfo(item, instance);
        
        // Thiết lập nút trang bị
        setupEquipButton(instance, item, type);
        
        finalizeDialog();
    }
    
    // Khởi tạo cửa sổ dialog
    private void initializeDialog(JFrame parent) {
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setLocationRelativeTo(parent);
        setUndecorated(true);
    }
    
    // Thiết lập panel chính với nền tối
    private void setupMainPanel() {
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                drawBackground((Graphics2D) g);
            }
        };
        mainPanel.setLayout(null);
    }
    
    // Vẽ nền và viền
    private void drawBackground(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 230));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(new Color(100, 100, 100));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(1, 1, getWidth()-2, getHeight()-2);
    }
    
    // Thêm nút đóng X
    private void setupCloseButton() {
        JButton closeButton = createCloseButton();
        setupCloseButtonStyle(closeButton);
        mainPanel.add(closeButton);
    }
    
    // Tạo nút đóng
    private JButton createCloseButton() {
        JButton button = new JButton("×");
        button.setBounds(DIALOG_WIDTH - CLOSE_BUTTON_SIZE - 5, 5, CLOSE_BUTTON_SIZE, CLOSE_BUTTON_SIZE);
        button.addActionListener(e -> dispose());
        return button;
    }
    
    // Thiết lập style cho nút đóng
    private void setupCloseButtonStyle(JButton button) {
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(50, 50, 50));
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
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
    
    // Thiết lập panel nội dung
    private void setupContentPanel() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        contentPanel.setOpaque(false);
        contentPanel.setBounds(0, 0, DIALOG_WIDTH, DIALOG_HEIGHT);
        mainPanel.add(contentPanel);
    }
    
    // Hiển thị thông tin item
    private void displayItemInfo(GameItem item, GameItemInstance instance) {
        addItemName(item.getName());
        addItemStats(instance);
    }
    
    // Thêm tên item
    private void addItemName(String name) {
        JLabel nameLabel = new JLabel(name);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(nameLabel);
        contentPanel.add(Box.createVerticalStrut(10));
    }
    
    // Thêm các chỉ số
    private void addItemStats(GameItemInstance instance) {
        addStat("ATK", instance.getAtk());
        addStat("DEF", instance.getDef());
        addStat("HP", instance.getHp());
        addStat("MP", instance.getMp());
        addStat("Crit Rate", String.format("%.1f%%", instance.getCritRate() * 100));
        addStat("Crit DMG", String.format("%.1f%%", instance.getCritDmg() * 100));
    }
    
    // Thiết lập nút trang bị
    private void setupEquipButton(GameItemInstance instance, GameItem item, String type) {
        findCurrentInventory(instance);
        equipButton = createEquipButton();
        setupEquipButtonListener(item, type);
        
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(equipButton);
        contentPanel.add(Box.createVerticalStrut(10));
    }
    
    // Tìm inventory hiện tại
    private void findCurrentInventory(GameItemInstance instance) {
        currentInventory = GameData.inventory.stream()
            .filter(inv -> inv.getItemInstanceId().equals(instance.getId()))
            .findFirst()
            .orElse(null);
    }
    
    // Hoàn thiện dialog
    private void finalizeDialog() {
        add(mainPanel);
        setupEscapeKey();
    }
    
    // Thiết lập phím ESC
    private void setupEscapeKey() {
        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }
    
    // Thêm một dòng chỉ số
    private void addStat(String label, Object value) {
        JPanel statPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        statPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(label + ":");
        nameLabel.setForeground(Color.LIGHT_GRAY);
        statPanel.add(nameLabel);
        
        JLabel valueLabel = new JLabel(value.toString());
        valueLabel.setForeground(Color.WHITE);
        statPanel.add(valueLabel);
        
        contentPanel.add(statPanel);
        contentPanel.add(Box.createVerticalStrut(5));
    }
    
    // Tạo nút trang bị
    private JButton createEquipButton() {
        JButton button = new JButton(currentInventory != null && currentInventory.isEquipped() ? 
            "Gỡ trang bị" : "Trang bị");
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(150, 30));
        return button;
    }
    
    // Thiết lập sự kiện cho nút trang bị
    private void setupEquipButtonListener(GameItem item, String type) {
        equipButton.addActionListener(e -> toggleEquipStatus(item, type));
    }
    
    // Xử lý trang bị/gỡ trang bị
    private void toggleEquipStatus(GameItem item, String type) {
        if (currentInventory == null) return;
        
        if (!currentInventory.isEquipped()) {
            // Gỡ trang bị cùng loại
            unequipSameType(type);
            // Trang bị item mới
            equipItem();
        } else {
            // Gỡ trang bị hiện tại
            unequipItem();
        }
        // Tính lại chỉ số
        Player player = gamePanel.getInstance().getPlayer();
        if (player != null) {
            player.stats.ChiSoGocGL();    // Tính lại chỉ số gốc
            player.stats.ChiSoTB();     // Tính lại chỉ số từ trang bị
        }
        inventoryPanel.updateInventory();
    }
    
    // Gỡ trang bị cùng loại
    private void unequipSameType(String type) {
        GameData.inventory.stream()
            .filter(inv -> {
                GameItem compareItem = GameData.getItemById(inv.getItemId());
                return compareItem != null && 
                       compareItem.getType().equals(type) && 
                       inv.isEquipped();
            })
            .forEach(inv -> inv.setEquipped(false));
    }
    
    // Trang bị item
    private void equipItem() {
        currentInventory.setEquipped(true);
        equipButton.setText("Gỡ trang bị");
        System.out.println("Đã trang bị item với ID: " + currentInventory.getItemInstanceId());
    }
    
    // Gỡ trang bị
    private void unequipItem() {
        currentInventory.setEquipped(false);
        equipButton.setText("Trang bị");
        System.out.println("Đã gỡ trang bị với ID: " + currentInventory.getItemInstanceId());
    }
}
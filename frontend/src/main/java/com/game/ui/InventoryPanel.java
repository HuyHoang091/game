package com.game.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import com.game.model.*;
import com.game.data.GameData;
import java.util.List;
import java.io.IOException;
import com.game.GameWindow;
import com.game.Player;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class InventoryPanel extends JPanel implements KeyListener, MouseListener {
    private static final int INVENTORY_ROWS = 10;
    private static final int INVENTORY_COLS = 10;
    private static final int SLOT_SIZE = 50;
    private static final int INVENTORY_WIDTH = INVENTORY_COLS * SLOT_SIZE;
    private static final int INVENTORY_HEIGHT = INVENTORY_ROWS * SLOT_SIZE;
    private static final int MAX_SLOTS = 100;

    private InventorySlot[] slots;
    private Color backgroundColor = new Color(0, 0, 0, 180);
    private Color slotColor = new Color(45, 45, 45, 200);
    private Color borderColor = new Color(100, 100, 100);
    private Font itemFont = new Font("Arial", Font.BOLD, 12);

    private GameWindow gameWindow;

    private JPanel leftPanel, rightPanel, statsPanel, equipPanel;
    private JLabel idleLabel;
    private EquipmentSlot[] equipmentSlots = new EquipmentSlot[6];
    private static final String[] EQUIP_TYPES = { "Vũ khí", "Áo giáp", "Mũ giáp", "Giáp chân", "Vòng cổ", "Nhẫn" };

    private JLabel hpLabel, mpLabel, atkLabel, defLabel, critRateLabel, critDmgLabel;

    public InventoryPanel(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setLayout(new BorderLayout());
        setOpaque(false);

        // --- Left: Inventory ---
        leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Vẽ nền mờ như cũ nếu muốn
            }
        };
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(INVENTORY_WIDTH + 20, INVENTORY_HEIGHT + 60));
        leftPanel.setLayout(null);
        initializeSlots();
        add(leftPanel, BorderLayout.WEST);

        // --- Right: Thông tin và trang bị ---
        rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BorderLayout());

        // Phần trên: chỉ số nhân vật
        Font statsFont = new Font("Arial", Font.BOLD, 12);
        Color statsColor = new Color(220, 220, 220);
        hpLabel = new JLabel();
        hpLabel.setForeground(statsColor);
        hpLabel.setFont(statsFont);
        mpLabel = new JLabel();
        mpLabel.setForeground(statsColor);
        mpLabel.setFont(statsFont);
        atkLabel = new JLabel();
        atkLabel.setForeground(statsColor);
        atkLabel.setFont(statsFont);
        defLabel = new JLabel();
        defLabel.setForeground(statsColor);
        defLabel.setFont(statsFont);
        critRateLabel = new JLabel();
        critRateLabel.setForeground(statsColor);
        critRateLabel.setFont(statsFont);
        critDmgLabel = new JLabel();
        critDmgLabel.setForeground(statsColor);
        critDmgLabel.setFont(statsFont);
        statsPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(350, 150));
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // Thêm các JLabel chỉ số vào đây, ví dụ:
        statsPanel.add(hpLabel);
        statsPanel.add(Box.createVerticalStrut(6));
        statsPanel.add(mpLabel);
        statsPanel.add(Box.createVerticalStrut(6));
        statsPanel.add(atkLabel);
        statsPanel.add(Box.createVerticalStrut(6));
        statsPanel.add(defLabel);
        statsPanel.add(Box.createVerticalStrut(6));
        statsPanel.add(critRateLabel);
        statsPanel.add(Box.createVerticalStrut(6));
        statsPanel.add(critDmgLabel);
        // ... các chỉ số khác ...
        rightPanel.add(statsPanel, BorderLayout.NORTH);

        // Phần dưới: khung nhân vật và trang bị
        equipPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Vẽ nền mờ nếu muốn
            }
        };
        equipPanel.setOpaque(false);
        equipPanel.setLayout(null);
        equipPanel.setPreferredSize(new Dimension(350, 350));

        // Ảnh idle nhân vật ở giữa
        idleLabel = new JLabel();
        idleLabel.setBounds(125, 80, 100, 150); // căn giữa
        // idleLabel.setIcon(new ImageIcon(...)); // set icon idle ở đây
        equipPanel.add(idleLabel);

        // 6 ô trang bị bao quanh
        int[][] slotPos = {
                { 150, 20 }, // Vũ khí (trên giữa)
                { 50, 80 }, // Áo giáp (trái trên)
                { 250, 80 }, // Quần giáp (phải trên)
                { 50, 200 }, // Giáp chân (trái dưới)
                { 250, 200 }, // Vòng cổ (phải dưới)
                { 150, 260 } // Nhẫn (dưới giữa)
        };
        for (int i = 0; i < 6; i++) {
            equipmentSlots[i] = new EquipmentSlot(EQUIP_TYPES[i]);
            equipmentSlots[i].setBounds(slotPos[i][0], slotPos[i][1], 50, 50);
            equipPanel.add(equipmentSlots[i]);
        }

        rightPanel.add(equipPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.CENTER);

        // Add listeners
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
    }

    private void initializeSlots() {
        slots = new InventorySlot[MAX_SLOTS];
        for (int i = 0; i < MAX_SLOTS; i++) {
            slots[i] = new InventorySlot();
            int row = i / INVENTORY_COLS;
            int col = i % INVENTORY_COLS;
            int x = col * SLOT_SIZE;
            int y = row * SLOT_SIZE;
            slots[i].setBounds(x, y, SLOT_SIZE, SLOT_SIZE);
        }
    }

    public void updateInventory() {
        try {
            // Clear all slots first
            for (InventorySlot slot : slots) {
                slot.setItem(null);
            }

            int dem = 0;
            if (GameData.inventory != null) {
                for (GameInventory inventory1 : GameData.inventory) {
                    if (dem < MAX_SLOTS) {
                        for (GameItem item : GameData.item) {
                            if (item.getId().equals(inventory1.getItemId())) {
                                BufferedImage itemIcon = ImageIO.read(
                                        getClass().getClassLoader().getResource(item.getIcon()));

                                slots[dem].setItem(new InventoryItem(
                                        item.getName(),
                                        item.getIcon(),
                                        inventory1.getQuantity(),
                                        inventory1.isEquipped(),
                                        inventory1.getItemInstanceId(),
                                        item.getType()));
                                dem++;
                                break;
                            }
                        }
                    }
                }
            }
            repaint();
        } catch (IOException e) {
            System.err.println("Error loading item icons: " + e.getMessage());
            e.printStackTrace();
        }

        // Update equipment slots
        updateEquipmentSlots();
        updateStatsPanel();
        repaint();
    }

    private void updateEquipmentSlots() {
        for (EquipmentSlot slot : equipmentSlots) {
            slot.setItem(null); // Clear existing item
        }

        for (GameInventory inventory1 : GameData.inventory) {
            if (!inventory1.isEquipped())
                continue; // Chỉ lấy item đang equipped

            for (GameItem item : GameData.item) {
                if (item.getId().equals(inventory1.getItemId())) {
                    // Determine equipment slot based on item type
                    int slotIndex = getEquipmentSlotIndex(item.getType());
                    if (slotIndex >= 0 && slotIndex < equipmentSlots.length) {
                        equipmentSlots[slotIndex].setItem(new InventoryItem(
                                item.getName(),
                                item.getIcon(),
                                inventory1.getQuantity(),
                                inventory1.isEquipped(),
                                inventory1.getItemInstanceId(),
                                item.getType()));
                    }
                    break;
                }
            }
        }
    }

    private int getEquipmentSlotIndex(String type) {
        switch (type) {
            case "Vũ khí":
                return 0;
            case "Áo giáp":
                return 1;
            case "Mũ giáp":
                return 2;
            case "Giáp chân":
                return 3;
            case "Vòng cổ":
                return 4;
            case "Nhẫn":
                return 5;
            default:
                return -1;
        }
    }

    private void updateStatsPanel() {
        Player player = GamePanel.getInstance().getPlayer();
        hpLabel.setText("HP: " + player.stats.getHealth());
        mpLabel.setText("MP: " + player.stats.getMana());
        atkLabel.setText("Tấn công: " + player.stats.getAtk());
        defLabel.setText("Phòng thủ: " + player.stats.getDef());
        int critRatePercent = (int) (player.stats.getCritRate() * 100);
        int critDmgPercent = (int) (player.stats.getCritDmg() * 100);

        critRateLabel.setText("Tỷ lệ chí mạng: " + critRatePercent + "%");
        critDmgLabel.setText("Sát thương chí mạng: " + critDmgPercent + "%");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw title
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Inventory", 10, 30);

        // Draw slots
        int startY = 50;
        for (InventorySlot slot : slots) {
            slot.draw(g2d, startY);
        }
    }

    private class InventorySlot {
        private Rectangle bounds;
        private InventoryItem item;

        public InventorySlot() {
            bounds = new Rectangle(SLOT_SIZE, SLOT_SIZE);
        }

        public void setBounds(int x, int y, int width, int height) {
            bounds.setBounds(x, y, width, height);
        }

        public void setItem(InventoryItem item) {
            this.item = item;
        }

        public void draw(Graphics2D g2d, int baseY) {
            // Draw slot background
            g2d.setColor(slotColor);
            g2d.fillRect(bounds.x, bounds.y + baseY, bounds.width, bounds.height);

            // Draw border
            g2d.setColor(borderColor);
            g2d.drawRect(bounds.x, bounds.y + baseY, bounds.width, bounds.height);

            // Draw item if exists
            if (item != null) {
                try {
                    ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource(item.iconPath));
                    g2d.drawImage(icon.getImage(),
                            bounds.x + 5, bounds.y + baseY + 5,
                            bounds.width - 10, bounds.height - 10, null);

                    // Draw quantity if > 1
                    if (item.quantity > 1) {
                        g2d.setColor(Color.WHITE);
                        g2d.setFont(itemFont);
                        String qty = String.valueOf(item.quantity);
                        g2d.drawString(qty,
                                bounds.x + bounds.width - 15,
                                bounds.y + baseY + bounds.height - 5);
                    }

                    // Draw equipped indicator
                    if (item.equipped) {
                        g2d.setColor(new Color(0, 255, 0, 100));
                        g2d.drawRect(bounds.x + 1, bounds.y + baseY + 1,
                                bounds.width - 2, bounds.height - 2);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading item icon: " + item.iconPath);
                }
            }
        }
    }

    private class InventoryItem {
        String name;
        String iconPath;
        int quantity;
        boolean equipped;
        Long instanceId;
        String type;

        public InventoryItem(String name, String iconPath, int quantity,
                boolean equipped, Long instanceId, String type) {
            this.name = name;
            this.iconPath = iconPath;
            this.quantity = quantity;
            this.equipped = equipped;
            this.instanceId = instanceId;
            this.type = type;
        }
    }

    // Slot trang bị
    private class EquipmentSlot extends JPanel {
        String type;
        InventoryItem item;

        public EquipmentSlot(String type) {
            this.type = type;
            setOpaque(false);
        }

        public void setItem(InventoryItem item) {
            this.item = item;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Vẽ khung slot
            g.setColor(Color.LIGHT_GRAY);
            g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            // Vẽ icon item nếu có
            if (item != null) {
                ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource(item.iconPath));
                g.drawImage(icon.getImage(), 5, 5, getWidth() - 10, getHeight() - 10, null);
            }
            // Vẽ tên type nếu rỗng
            if (item == null) {
                g.setColor(Color.GRAY);
                g.setFont(new Font("Arial", Font.PLAIN, 10));
                FontMetrics fm = g.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(type)) / 2;
                int ty = getHeight() / 2 + fm.getAscent() / 2;
                g.drawString(type, tx, ty);
            }
        }
    }

    // Add KeyListener methods
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            gameWindow.goBack();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    // Add MouseListener methods
    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY() - 50; // Adjust for title area

        // Find clicked slot
        for (int i = 0; i < slots.length; i++) {
            InventorySlot slot = slots[i];
            if (slot.item != null &&
                    x >= slot.bounds.x && x <= slot.bounds.x + SLOT_SIZE &&
                    y >= slot.bounds.y && y <= slot.bounds.y + SLOT_SIZE) {

                for (GameItemInstance instance : GameData.itemInstance) {
                    if (instance.getId().equals(slot.item.instanceId)) {
                        System.out.println("Clicked item instance ID: " + slot.item.instanceId);
                        System.out.println("Found instance: " + (instance != null));

                        if (instance != null) {
                            GameItem item = GameData.getItemById(instance.getItemId());
                            if (item != null) {
                                StatsDialog dialog = new StatsDialog(
                                        (JFrame) SwingUtilities.getWindowAncestor(this),
                                        this,
                                        instance,
                                        item,
                                        slot.item.type);
                                dialog.setVisible(true);
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    // When inventory becomes visible, request focus
    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }
}
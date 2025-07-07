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
    private final Color backgroundColor = new Color(0, 0, 0, 180);
    private final Color slotColor = new Color(45, 45, 45, 200);
    private final Color borderColor = new Color(100, 100, 100);
    private final Font itemFont = new Font("Arial", Font.BOLD, 12);

    private final GameWindow gameWindow;

    private JPanel leftPanel, rightPanel, statsPanel, equipPanel;
    private JLabel idleLabel;
    private EquipmentSlot[] equipmentSlots = new EquipmentSlot[6];
    private static final String[] EQUIP_TYPES = {"Vũ khí", "Áo giáp", "Mũ giáp", "Giáp chân", "Vòng cổ", "Nhẫn"};

    private JLabel hpLabel, mpLabel, atkLabel, defLabel, critRateLabel, critDmgLabel;

    public InventoryPanel(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setLayout(new BorderLayout());
        setOpaque(false);

        initComponents();
        setupInventoryArea();
        setupStatsPanel();
        setupEquipmentArea();
        addListeners();
    }

    private void initComponents() {
        slots = new InventorySlot[MAX_SLOTS];
        for (int i = 0; i < MAX_SLOTS; i++) {
            slots[i] = new InventorySlot();
            int row = i / INVENTORY_COLS;
            int col = i % INVENTORY_COLS;
            slots[i].setBounds(col * SLOT_SIZE, row * SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
        }

        hpLabel = createStatLabel();
        mpLabel = createStatLabel();
        atkLabel = createStatLabel();
        defLabel = createStatLabel();
        critRateLabel = createStatLabel();
        critDmgLabel = createStatLabel();
    }

    private JLabel createStatLabel() {
        JLabel label = new JLabel();
        label.setForeground(new Color(220, 220, 220));
        label.setFont(new Font("Arial", Font.BOLD, 12));
        return label;
    }

    private void setupInventoryArea() {
        leftPanel = new JPanel(null);
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(INVENTORY_WIDTH + 20, INVENTORY_HEIGHT + 60));
        add(leftPanel, BorderLayout.WEST);
    }

    private void setupStatsPanel() {
        statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        statsPanel.add(hpLabel); statsPanel.add(Box.createVerticalStrut(6));
        statsPanel.add(mpLabel); statsPanel.add(Box.createVerticalStrut(6));
        statsPanel.add(atkLabel); statsPanel.add(Box.createVerticalStrut(6));
        statsPanel.add(defLabel); statsPanel.add(Box.createVerticalStrut(6));
        statsPanel.add(critRateLabel); statsPanel.add(Box.createVerticalStrut(6));
        statsPanel.add(critDmgLabel);
    }

    private void setupEquipmentArea() {
        equipPanel = new JPanel(null);
        equipPanel.setOpaque(false);
        equipPanel.setPreferredSize(new Dimension(350, 350));

        idleLabel = new JLabel();
        idleLabel.setBounds(125, 80, 100, 150);
        equipPanel.add(idleLabel);

        int[][] pos = {
            {150, 20}, {50, 80}, {250, 80}, {50, 200}, {250, 200}, {150, 260}
        };
        for (int i = 0; i < EQUIP_TYPES.length; i++) {
            equipmentSlots[i] = new EquipmentSlot(EQUIP_TYPES[i]);
            equipmentSlots[i].setBounds(pos[i][0], pos[i][1], 50, 50);
            equipPanel.add(equipmentSlots[i]);
        }

        rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.add(statsPanel, BorderLayout.NORTH);
        rightPanel.add(equipPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.CENTER);
    }

    private void addListeners() {
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Inventory", 10, 30);

        int startY = 50;
        for (InventorySlot slot : slots) {
            slot.draw(g2d, startY);
        }
    }

    public void updateInventory() {
        try {
            for (InventorySlot slot : slots) slot.setItem(null);
            int dem = 0;
            for (GameInventory inv : GameData.inventory) {
                if (dem >= MAX_SLOTS) break;
                for (GameItem item : GameData.item) {
                    if (item.getId().equals(inv.getItemId())) {
                        slots[dem++].setItem(new InventoryItem(item.getName(), item.getIcon(), inv.getQuantity(), inv.isEquipped(), inv.getItemInstanceId(), item.getType()));
                        break;
                    }
                }
            }
            updateEquipmentSlots();
            updateStatsPanel();
            repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateEquipmentSlots() {
        for (EquipmentSlot slot : equipmentSlots) slot.setItem(null);
        for (GameInventory inv : GameData.inventory) {
            if (!inv.isEquipped()) continue;
            for (GameItem item : GameData.item) {
                if (item.getId().equals(inv.getItemId())) {
                    int idx = getEquipmentSlotIndex(item.getType());
                    if (idx >= 0) equipmentSlots[idx].setItem(new InventoryItem(item.getName(), item.getIcon(), inv.getQuantity(), true, inv.getItemInstanceId(), item.getType()));
                    break;
                }
            }
        }
    }

    private int getEquipmentSlotIndex(String type) {
        for (int i = 0; i < EQUIP_TYPES.length; i++) {
            if (EQUIP_TYPES[i].equals(type)) return i;
        }
        return -1;
    }

    private void updateStatsPanel() {
        Player player = GamePanel.getInstance().getPlayer();
        hpLabel.setText("HP: " + player.stats.getHealth());
        mpLabel.setText("MP: " + player.stats.getMana());
        atkLabel.setText("Tấn công: " + player.stats.getAtk());
        defLabel.setText("Phòng thủ: " + player.stats.getDef());
        critRateLabel.setText("Tỷ lệ chí mạng: " + (int)(player.stats.getCritRate() * 100) + "%");
        critDmgLabel.setText("Sát thương chí mạng: " + (int)(player.stats.getCritDmg() * 100) + "%");
    }

    private class InventorySlot {
        private Rectangle bounds;
        private InventoryItem item;

        public InventorySlot() {
            bounds = new Rectangle(SLOT_SIZE, SLOT_SIZE);
        }

        public void setBounds(int x, int y, int w, int h) {
            bounds.setBounds(x, y, w, h);
        }

        public void setItem(InventoryItem item) {
            this.item = item;
        }

        public void draw(Graphics2D g2d, int baseY) {
            g2d.setColor(slotColor);
            g2d.fillRect(bounds.x, bounds.y + baseY, bounds.width, bounds.height);
            g2d.setColor(borderColor);
            g2d.drawRect(bounds.x, bounds.y + baseY, bounds.width, bounds.height);

            if (item != null) {
                try {
                    ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource(item.iconPath));
                    g2d.drawImage(icon.getImage(), bounds.x + 5, bounds.y + baseY + 5, bounds.width - 10, bounds.height - 10, null);
                    if (item.quantity > 1) {
                        g2d.setColor(Color.WHITE);
                        g2d.setFont(itemFont);
                        g2d.drawString(String.valueOf(item.quantity), bounds.x + bounds.width - 15, bounds.y + baseY + bounds.height - 5);
                    }
                    if (item.equipped) {
                        g2d.setColor(new Color(0, 255, 0, 100));
                        g2d.drawRect(bounds.x + 1, bounds.y + baseY + 1, bounds.width - 2, bounds.height - 2);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading item icon: " + item.iconPath);
                }
            }
        }
    }

    private class InventoryItem {
        String name, iconPath, type;
        int quantity;
        boolean equipped;
        Long instanceId;

        public InventoryItem(String name, String iconPath, int quantity, boolean equipped, Long instanceId, String type) {
            this.name = name;
            this.iconPath = iconPath;
            this.quantity = quantity;
            this.equipped = equipped;
            this.instanceId = instanceId;
            this.type = type;
        }
    }

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
            g.setColor(Color.LIGHT_GRAY);
            g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            if (item != null) {
                ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource(item.iconPath));
                g.drawImage(icon.getImage(), 5, 5, getWidth() - 10, getHeight() - 10, null);
            } else {
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
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

    public InventoryPanel(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setLayout(null);
        setOpaque(false);
        initializeSlots();

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
                                    getClass().getClassLoader().getResource(item.getIcon())
                                );

                                slots[dem].setItem(new InventoryItem(
                                    item.getName(),
                                    item.getIcon(),
                                    inventory1.getQuantity(),
                                    inventory1.isEquipped(),
                                    inventory1.getItemInstanceId(),
                                    item.getType()
                                ));
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

    // Add KeyListener methods
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            gameWindow.goBack();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

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
                
                for(GameItemInstance instance : GameData.itemInstance) {
                    if (instance.getId().equals(slot.item.instanceId)) {
                        System.out.println("Clicked item instance ID: " + slot.item.instanceId);
                        System.out.println("Found instance: " + (instance != null));
                        
                        if (instance != null) {
                            GameItem item = GameData.getItemById(instance.getItemId());
                            if (item != null) {
                                StatsDialog dialog = new StatsDialog(
                                    (JFrame) SwingUtilities.getWindowAncestor(this),
                                    instance,
                                    item,
                                    slot.item.type
                                );
                                dialog.setVisible(true);
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // When inventory becomes visible, request focus
    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }
}
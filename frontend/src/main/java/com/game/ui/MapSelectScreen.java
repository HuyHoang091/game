package com.game.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.HashMap;
import java.awt.event.*;
import com.game.GameWindow;
import com.game.MapData;
import javax.swing.plaf.basic.BasicScrollBarUI;
import com.game.data.GameData;
import com.game.model.*;
import com.game.resource.*;

public class MapSelectScreen extends JPanel {
    private GameWindow gameWindow;
    private BufferedImage backgroundImage;
    private JPanel mapGrid;
    private String selectedMap = "map1";
    private HashMap<String, MapData> mapDataList;
    private Color accentColor = new Color(0, 230, 255);  // Neon Cyan
    private Color selectedColor = new Color(255, 215, 0); // Gold
    private Font titleFont;
    private Font labelFont;
    private float glowIntensity = 0.0f;
    private Timer glowTimer;

    public MapSelectScreen(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setLayout(new BorderLayout());

        // Load custom fonts
        try {
            titleFont = Font.createFont(Font.TRUETYPE_FONT, 
                new File("assets/fonts/Orbitron-Bold.ttf")).deriveFont(42f);
            labelFont = Font.createFont(Font.TRUETYPE_FONT, 
                new File("assets/fonts/Exo2-SemiBold.ttf")).deriveFont(18f);
        } catch (Exception e) {
            titleFont = new Font("Arial", Font.BOLD, 42);
            labelFont = new Font("Arial", Font.BOLD, 18);
        }
        
        try {
            backgroundImage = ImageIO.read(getClass().getClassLoader().getResource("assets/image.png"));
        } catch (Exception e) {
            System.out.println("Error loading background: " + e.getMessage());
        }

        startGlowAnimation();
        initializeComponents();
        initializeMapData();
    }

    private void initializeMapData() {
        mapDataList = new HashMap<>();
        int mapIndex = 1;

        for (GameMap map : GameData.map) {
            MapData forestMap = new MapData(
                map.getBackground(),
                map.getCollisionlayer()
            );
            for (GameMonster monster : GameData.monster) {
                boolean isEnemy = monster.getId().equals(map.getEnemyId());
                boolean isBoss = monster.getId().equals(map.getBossId());

                if ((isEnemy || isBoss)) {
                    String type = isBoss ? "Boss" : "Enemy";

                    System.out.print(type);

                    String[] cauhinhParts = monster.getCauhinh().split(",");
                    int width = Integer.parseInt(cauhinhParts[2].trim());
                    int height = Integer.parseInt(cauhinhParts[3].trim());

                    forestMap.enemies.add(new MapData.EnemyData(
                        type,
                        width,
                        height,
                        monster.getHp(),
                        monster.getId(),
                        monster.getName()
                    ));
                
                }
            }
            String mapKey = "map" + mapIndex;
            mapDataList.put(mapKey, forestMap);

            mapIndex++;
        }
    }

    private void initializeComponents() {
        JPanel titlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Title glow effect
                g2d.setFont(titleFont);
                String title = "SELECT A MAP TO PLAY!!!";
                FontMetrics fm = g2d.getFontMetrics();
                int titleX = (getWidth() - fm.stringWidth(title)) / 2;
                int titleY = fm.getHeight() + 20;

                // Draw glow
                g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), 
                    accentColor.getBlue(), (int)(100 * glowIntensity)));
                g2d.drawString(title, titleX + 2, titleY + 2);
                
                // Draw main text
                g2d.setColor(Color.WHITE);
                g2d.drawString(title, titleX, titleY);
            }
        };
        titlePanel.setPreferredSize(new Dimension(getWidth(), 100));
        titlePanel.setOpaque(false);

        // Create a scrollable map grid
        JPanel scrollContent = new JPanel(new GridBagLayout());
        scrollContent.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Map Grid Panel with cyber border
        mapGrid = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20)) {
            @Override
            public Dimension getPreferredSize() {
                int maxWidth = 900; // Chiều rộng tối đa của mapGrid
                int itemHeight = 100;  // Chiều cao của mỗi item (có thể điều chỉnh tùy theo nhu cầu)
                int spacing = 20; // Khoảng cách giữa các item
                int totalHeight = GameData.map.size() * (itemHeight + spacing);  // Tính chiều cao tổng của mapGrid
                
                // Nếu chiều cao tổng của mapGrid lớn hơn một giá trị nhất định (ví dụ 1000px), thì có thể giới hạn lại
                int preferredHeight = totalHeight > 1000 ? 1000 : totalHeight;  // Giới hạn chiều cao tối đa nếu cần
                
                return new Dimension(maxWidth, preferredHeight);
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ cyber border
                g2d.setStroke(new BasicStroke(2f));
                g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), 
                    accentColor.getBlue(), (int)(150 * glowIntensity)));
                g2d.drawRect(5, 5, getWidth() - 10, getHeight() - 10);
                
                // Vẽ corner accents
                int cornerSize = 20;
                g2d.drawLine(0, cornerSize, 0, 0);
                g2d.drawLine(0, 0, cornerSize, 0);
                g2d.drawLine(getWidth() - cornerSize, 0, getWidth(), 0);
                g2d.drawLine(getWidth(), 0, getWidth(), cornerSize);
                g2d.drawLine(0, getHeight() - cornerSize, 0, getHeight());
                g2d.drawLine(0, getHeight(), cornerSize, getHeight());
                g2d.drawLine(getWidth() - cornerSize, getHeight(), getWidth(), getHeight());
                g2d.drawLine(getWidth(), getHeight() - cornerSize, getWidth(), getHeight());
            }
        };
        mapGrid.setOpaque(false);
        mapGrid.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Create scrollpane with custom appearance
        JScrollPane scrollPane = new JScrollPane(mapGrid);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Customize scrollbar
        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        verticalBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(accentColor.getRed(), accentColor.getGreen(), 
                    accentColor.getBlue(), 150);
                this.trackColor = new Color(0, 0, 0, 100);
            }
            
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
        });

        // Tạo các nút map
        int mapIndex = 1;
        for (GameMap map : GameData.map) {
            String mapId = "map" + mapIndex;
            createMapButton(map.getName(), mapId, map.getPreview());

            mapIndex++;
        }

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setOpaque(false);
        
        buttonPanel.add(createGamingButton("PLAY", e -> startGame()));
        buttonPanel.add(createGamingButton("BACK", e -> gameWindow.showMenu()));

        add(titlePanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void createMapButton(String name, String mapId, String previewPath) {
        JPanel mapPanel = new JPanel() {
            private float hoverGlow = 0.0f;
            private Timer glowTimer;
            private boolean isSelected = false;

            {
                glowTimer = new Timer(30, e -> {
                    if (getName().equals(selectedMap)) {
                        hoverGlow = Math.min(1.0f, hoverGlow + 0.08f);
                        isSelected = true;
                    } else if (isSelected) {
                        hoverGlow = Math.max(0.0f, hoverGlow - 0.08f);
                        if (hoverGlow == 0) isSelected = false;
                    }
                    repaint();
                });
                glowTimer.start();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw background with gradient
                GradientPaint bgGradient = new GradientPaint(
                    0, 0, new Color(20, 20, 35),
                    0, getHeight(), new Color(35, 35, 50)
                );
                g2d.setPaint(bgGradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Draw cyber frame
                if (getName().equals(selectedMap)) {
                    // Selected state glow
                    float glowOpacity = 0.3f + 0.2f * (float)Math.sin(System.currentTimeMillis() / 500.0);
                    g2d.setColor(new Color(
                        selectedColor.getRed()/255f,
                        selectedColor.getGreen()/255f,
                        selectedColor.getBlue()/255f,
                        glowOpacity
                    ));
                    g2d.setStroke(new BasicStroke(4f));
                    g2d.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, 20, 20);
                }

                // Draw corner accents
                g2d.setColor(accentColor);
                g2d.setStroke(new BasicStroke(2f));
                int cornerSize = 20;
                
                // Top-left
                g2d.drawLine(0, cornerSize, 0, 0);
                g2d.drawLine(0, 0, cornerSize, 0);
                
                // Top-right
                g2d.drawLine(getWidth()-cornerSize, 0, getWidth(), 0);
                g2d.drawLine(getWidth()-1, 0, getWidth()-1, cornerSize);
                
                // Bottom-left
                g2d.drawLine(0, getHeight()-cornerSize, 0, getHeight());
                g2d.drawLine(0, getHeight()-1, cornerSize, getHeight()-1);
                
                // Bottom-right
                g2d.drawLine(getWidth()-cornerSize, getHeight()-1, getWidth(), getHeight()-1);
                g2d.drawLine(getWidth()-1, getHeight()-cornerSize, getWidth()-1, getHeight());

                super.paintComponent(g);
                g2d.dispose();
            }
        };

        mapPanel.setLayout(new BoxLayout(mapPanel, BoxLayout.Y_AXIS));
        mapPanel.setOpaque(false);
        mapPanel.setName(mapId);
        mapPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Map preview with custom frame
        try {
            BufferedImage preview = ImageIO.read(getClass().getClassLoader().getResource(previewPath));
            JLabel imageLabel = new JLabel(new ImageIcon(preview.getScaledInstance(200, 150, Image.SCALE_SMOOTH))) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Draw image frame
                    g2d.setColor(new Color(0, 0, 0, 100));
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    
                    super.paintComponent(g2d);
                    
                    // Draw frame border
                    g2d.setColor(accentColor);
                    g2d.setStroke(new BasicStroke(2f));
                    g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                    
                    g2d.dispose();
                }
            };
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            mapPanel.add(imageLabel);
            mapPanel.add(Box.createVerticalStrut(10));
        } catch (Exception e) {
            System.out.println("Error loading map preview: " + e.getMessage());
        }

        // Map name with custom styling
        JLabel nameLabel = new JLabel(name) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                // Background for name
                GradientPaint bgGradient = new GradientPaint(
                    0, 0, new Color(0, 0, 0, 180),
                    0, getHeight(), new Color(0, 0, 0, 220)
                );
                g2d.setPaint(bgGradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                // Draw text with enhanced glow
                g2d.setFont(labelFont.deriveFont(22f));
                FontMetrics fm = g2d.getFontMetrics();
                String originalText = getText();
                String displayText = originalText;
                int maxWidth = getWidth() - 20; // Padding để tránh sát viền

                while (fm.stringWidth(displayText) > maxWidth && displayText.length() > 0) {
                    displayText = displayText.substring(0, displayText.length() - 1);
                }
                if (!displayText.equals(originalText)) {
                    displayText += "...";
                }

                int x = (getWidth() - fm.stringWidth(displayText)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();

                // Outer glow
                g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 50));
                g2d.drawString(displayText, x + 2, y + 2);
                g2d.drawString(displayText, x - 2, y - 2);

                // Inner glow
                g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 120));
                g2d.drawString(displayText, x + 1, y + 1);

                // Main text
                g2d.setColor(Color.WHITE);
                g2d.drawString(displayText, x, y);
                
                // Border
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 100));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                
                g2d.dispose();
            }
        };
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); 
        nameLabel.setPreferredSize(new Dimension(nameLabel.getPreferredSize().width, 30));
        mapPanel.add(Box.createVerticalStrut(10));
        mapPanel.add(nameLabel);

        // Hover effects
        mapPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                JPanel panel = (JPanel)e.getSource();
                panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                JPanel panel = (JPanel)e.getSource();
                panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                selectedMap = mapId;
                updateSelection();
            }
        });

        mapGrid.add(mapPanel);
    }

    private void updateSelection() {
        // Update visual selection for all map panels
        for (Component comp : mapGrid.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel mapPanel = (JPanel) comp;
                if (mapPanel.getName().equals(selectedMap)) {
                    mapPanel.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
                } else {
                    mapPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                }
            }
        }
        repaint();
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setPreferredSize(new Dimension(150, 40));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
    }

    private JButton createGamingButton(String text, ActionListener action) {
        JButton button = new JButton(text) {
            private float hoverIntensity = 0.0f;
            private boolean isHover = false;
            
            {
                Timer pulseTimer = new Timer(50, e -> {
                    if (isHover) {
                        hoverIntensity = Math.min(1.0f, hoverIntensity + 0.1f);
                    } else {
                        hoverIntensity = Math.max(0.0f, hoverIntensity - 0.1f);
                    }
                    repaint();
                });
                pulseTimer.start();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Background for name
        GradientPaint bgGradient = new GradientPaint(
            0, 0, new Color(0, 0, 0, 180),
            0, getHeight(), new Color(0, 0, 0, 220)
        );
        g2d.setPaint(bgGradient);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        
        // Draw text with enhanced glow
        g2d.setFont(labelFont.deriveFont(22f));
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
        
        // Outer glow
        g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 50));
        g2d.drawString(getText(), x+2, y+2);
        g2d.drawString(getText(), x-2, y-2);
        
        // Inner glow
        g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 120));
        g2d.drawString(getText(), x+1, y+1);
        
        // Main text
        g2d.setColor(Color.WHITE);
        g2d.drawString(getText(), x, y);
        
        // Border
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 100));
        g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
        
        g2d.dispose();
            }
        };
        
        button.setPreferredSize(new Dimension(200, 50));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(action);
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                ((JButton)e.getSource()).putClientProperty("isHover", true);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                ((JButton)e.getSource()).putClientProperty("isHover", false);
            }
        });
        
        return button;
    }

    private void startGlowAnimation() {
        glowTimer = new Timer(50, e -> {
            glowIntensity = (float)(0.5f + 0.3f * Math.sin(System.currentTimeMillis() / 1000.0));
            repaint();
        });
        glowTimer.start();
    }

    private void startGame() {
        MapData selectedMapData = mapDataList.get(selectedMap);
        if (selectedMapData != null) {
            gameWindow.startGame(selectedMapData);
        } else {
            JOptionPane.showMessageDialog(this, "Bản đồ chưa được khởi tạo!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Draw background with parallax effect
        if (backgroundImage != null) {
            int offsetX = (int)(5 * Math.sin(System.currentTimeMillis() / 2000.0));
            int offsetY = (int)(5 * Math.cos(System.currentTimeMillis() / 2000.0));
            g2d.drawImage(backgroundImage, offsetX, offsetY, 
                getWidth() + Math.abs(offsetX) * 2, 
                getHeight() + Math.abs(offsetY) * 2, null);
        }
        
        // Add dark overlay
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}
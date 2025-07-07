package com.game.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import com.game.AccessFrame;
import com.game.GameDataUploader;
import com.game.GameWindow;
import com.game.core.KeyBindingConfig;
import com.game.data.GameData;
import com.game.rendering.GlobalLoadingManager;
import com.game.resource.MapPreviewManager;
import com.game.resource.ResourceManager;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class MainMenu extends JPanel {
    private BufferedImage backgroundImage;
    private JButton playButton;
    private JButton settingsButton;
    private JButton exitButton;
    private GameWindow gameWindow;
    private Timer glowTimer;
    private float glowIntensity = 0.0f;
    private Color accentColor = new Color(0, 230, 255);  // Neon Cyan
    private Color glowColor = new Color(0, 210, 255, 40);
    private Font titleFont;
    private String gameTitle = "LORD OF DUNGEONS";
    private int titleY = 100;
    private float titleGlow = 0.0f;

    public MainMenu(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setLayout(null);
        
        // Load custom font
        try {
            titleFont = Font.createFont(Font.TRUETYPE_FONT, 
                new File("assets/fonts/Orbitron-Bold.ttf")).deriveFont(48f);
        } catch (Exception e) {
            titleFont = new Font("Arial", Font.BOLD, 48);
        }
        
        // Load background
        try {
            backgroundImage = ImageIO.read(getClass().getClassLoader().getResource("assets/image.png"));
        } catch (Exception e) {
            System.out.println("Error loading menu background: " + e.getMessage());
        }

        GameDataUploader.startAutoUpload();

        initializeButtons();
        startAnimations();
    }

    private void initializeButtons() {
        Font buttonFont = new Font("Arial", Font.BOLD, 24);
        Dimension buttonSize = new Dimension(250, 60);
        
        playButton = createStyledButton("PLAY GAME", buttonFont, buttonSize);
        playButton.addActionListener(e -> {
            gameWindow.showMapSelect();
        });
        settingsButton = createStyledButton("SETTINGS", buttonFont, buttonSize);
        settingsButton.addActionListener(e -> {
            gameWindow.showSettings("Menu");
        });
        exitButton = createStyledButton("LOG OUT", buttonFont, buttonSize);
        exitButton.addActionListener(e -> {
            GameDataUploader.stopAutoUpload();
            GameWindow.getInstance().Logout();
            GameData.clear();
            MapPreviewManager.previewCache.clear();
            ResourceManager.clearAnimationCache();
            GamePanel.currentInstance = null;
            SettingsPanel.instance = null;
            System.gc();
            GlobalLoadingManager loadingManager = new GlobalLoadingManager(gameWindow);
            loadingManager.startLoading(() -> {
                
            });

            new Thread(() -> {
                try {
                    String hash = AccessFrame.hashDirectory(new File("target/classes"));
                    System.out.print(hash);

                    HttpClient client1 = HttpClient.newHttpClient();
                    HttpRequest request1 = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080/api/appcode/verify"))
                            .header("App-Hash", hash)
                            .GET()
                            .build();

                    HttpResponse<String> response1 = client1.send(request1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

                    if(response1.statusCode() == 200) {
                        AccessFrame.getInstance().frontendSecret = response1.body();
                    } else {
                        System.exit(0);
                    }
                } catch (Exception e1) {}
                
                loadingManager.setLoading(false);
                AccessFrame loginFrame = AccessFrame.getInstance();
                loginFrame.setVisible(true);
                loginFrame.showLogin();
                GameWindow.getInstance().dispose();
            }).start();
        });
        
        // Center buttons horizontally
        int centerX = 400;
        playButton.setBounds(centerX - 125, 300, 250, 60);
        settingsButton.setBounds(centerX - 125, 380, 250, 60);
        exitButton.setBounds(centerX - 125, 460, 250, 60);
        
        add(playButton);
        add(settingsButton);
        add(exitButton);
    }

    private JButton createStyledButton(String text, Font font, Dimension size) {
        JButton button = new JButton(text) {
            private float hoverIntensity = 0.0f;
            private Timer pulseTimer;

            {
                putClientProperty("isHover", false);

                pulseTimer = new Timer(50, e -> {
                    boolean isHover = Boolean.TRUE.equals(getClientProperty("isHover"));
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
                super.paintComponent(g); // ĐỪNG QUÊN DÒNG NÀY!
                
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, hoverIntensity * 0.3f));
                g2d.setColor(Color.CYAN); // glow color
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
            }
        };

        button.setFont(font);
        button.setPreferredSize(size);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setForeground(new Color(160, 160, 160));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.putClientProperty("isHover", true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.putClientProperty("isHover", false);
            }
        });

        return button;
    }

    private void startAnimations() {
        Timer titleTimer = new Timer(50, e -> {
            titleGlow = (float) (0.5f + 0.5f * Math.sin(System.currentTimeMillis() / 1000.0));
            titleY = 100 + (int)(5 * Math.sin(System.currentTimeMillis() / 1500.0));
            repaint();
        });
        titleTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
            RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw background with parallax effect
        if (backgroundImage != null) {
            int offsetX = (int)(5 * Math.sin(System.currentTimeMillis() / 2000.0));
            int offsetY = (int)(5 * Math.cos(System.currentTimeMillis() / 2000.0));
            g2d.drawImage(backgroundImage, offsetX, offsetY, 
                getWidth() + Math.abs(offsetX) * 2, 
                getHeight() + Math.abs(offsetY) * 2, null);
        }
        
        // Add overlay gradient
        GradientPaint overlay = new GradientPaint(
            0, 0, new Color(0, 0, 0, 180),
            0, getHeight(), new Color(0, 0, 0, 120)
        );
        g2d.setPaint(overlay);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw title with glow effect
        g2d.setFont(titleFont);
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (getWidth() - fm.stringWidth(gameTitle)) / 2;
        
        // Glow effect
        g2d.setColor(new Color(accentColor.getRed(), 
            accentColor.getGreen(), accentColor.getBlue(), 
            (int)(100 * titleGlow)));
        g2d.drawString(gameTitle, titleX + 2, titleY + 2);
        
        // Main title
        g2d.setColor(Color.WHITE);
        g2d.drawString(gameTitle, titleX, titleY);
        
        g2d.dispose();
    }
}
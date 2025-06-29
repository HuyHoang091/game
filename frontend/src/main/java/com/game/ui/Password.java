package com.game.ui;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.game.AccessFrame;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import javax.swing.border.Border;

public class Password extends JPanel {
    private JTextField emailField;
    private AccessFrame accessFrame;
    private Color accentColor = new Color(255, 64, 129);  // Material Pink
    private Font inputFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Color glowColor = new Color(0, 255, 255, 50); // Cyan glow
    private Timer glowTimer;
    private float glowIntensity = 0.0f;
    private BufferedImage backgroundImage;
    private JButton loginButton;

    public Password(AccessFrame accessFrame) {
        this.accessFrame = accessFrame;
        setLayout(new BorderLayout());
        // Load background image
        try {
            backgroundImage = ImageIO.read(getClass().getClassLoader().getResource("assets/image.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Main panel with gaming background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw background image
                if (backgroundImage != null) {
                    g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
                }

                // Add dark overlay
                g2d.setColor(new Color(0, 0, 0, 220));
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Add glow effect
                if (glowIntensity > 0) {
                    AlphaComposite alphaComposite = AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, glowIntensity);
                    g2d.setComposite(alphaComposite);
                    g2d.setColor(glowColor);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(100, 200, 80, 200));

        // Title Panel with gaming font
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        JLabel titleLabel = createGamingLabel("FORGOT PASSWORD", 36);
        titlePanel.add(titleLabel);

        // Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(3, 1, 15, 20));
        formPanel.setOpaque(false);
        
        // Username field with gaming style
        JPanel emailPanel = createGamingInputPanel("ENTER EMAIL", emailField = createGamingTextField());

        // Login button with gaming style
        loginButton = createGamingButton("SUBMIT");
        loginButton.addActionListener(e -> submit());

        JPanel registerPanel = new JPanel();
        registerPanel.setOpaque(false);
        registerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JLabel registerLabel = new JLabel("Bạn đã có tài khoản? ");
        registerLabel.setForeground(Color.WHITE);
        registerLabel.setFont(inputFont);

        JLabel signUpLabel = new JLabel("Đăng nhập");
        signUpLabel.setForeground(accentColor);
        signUpLabel.setFont(inputFont);
        signUpLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signUpLabel.setToolTipText("Nhấn để đăng nhập");

        // Gắn sự kiện khi nhấn "Đăng ký"
        signUpLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                accessFrame.showLogin();
            }
        });

        registerPanel.add(registerLabel);
        registerPanel.add(signUpLabel);

        mainPanel.add(registerPanel, BorderLayout.SOUTH);

        formPanel.add(emailPanel);
        formPanel.add(loginButton);

        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);

        // Start glow effect timer
        startGlowEffect();
    }

    private JLabel createGamingLabel(String text, int size) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, size));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accentColor, 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        return label;
    }

    private JPanel createGamingInputPanel(String labelText, JComponent input) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        
        JLabel label = createGamingLabel(labelText, 14);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        input.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(label);
        panel.add(Box.createVerticalStrut(10));
        panel.add(input);
        
        return panel;
    }

    private JTextField createGamingTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Consolas", Font.BOLD, 16));
        field.setForeground(Color.CYAN);
        field.setBackground(new Color(0, 0, 0, 150));
        field.setBorder(new CyberBorder(Color.CYAN, 10));
        field.setCaretColor(Color.CYAN);
        field.setPreferredSize(new Dimension(250, 40));
        return field;
    }

    private JButton createGamingButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Button gradient
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 255),
                    0, getHeight(), new Color(accentColor.getRed()/2, accentColor.getGreen()/2, accentColor.getBlue()/2, 255)
                );
                g2.setPaint(gradient);
                
                // Draw button with glow effect
                if (getModel().isRollover()) {
                    g2.setStroke(new BasicStroke(2.0f));
                    g2.setColor(new Color(0, 255, 255, 50));
                    g2.fillRoundRect(-5, -5, getWidth()+10, getHeight()+10, 15, 15);
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();

                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(200, 50));

        return button;
    }

    private void startGlowEffect() {
        glowTimer = new Timer(50, e -> {
            glowIntensity = (float) (0.3f + 0.1f * Math.sin(System.currentTimeMillis() / 1000.0));
            repaint();
        });
        glowTimer.start();
    }

    private void submit() {
        String email = emailField.getText();

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("email", email);
            String json = mapper.writeValueAsString(requestBody);

            Properties props = new Properties();
            props.load(getClass().getResourceAsStream("/app.properties"));
            String appCode = props.getProperty("app.code");

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/users/repass"))
                    .header("Content-Type", "application/json")
                    .header("App-Code", appCode)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() == 200) {
                JOptionPane.showMessageDialog(this,
                        response.body(),
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
                accessFrame.showLogin();
            } else {
                JOptionPane.showMessageDialog(this,
                        response.body(),
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Connection error",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private class CyberBorder implements Border {
        private float animationPhase = 0;
        private final Color borderColor;
        private final int arc;

        public CyberBorder(Color color, int arc) {
            this.borderColor = color;
            this.arc = arc;
            new Timer(50, e -> {
                animationPhase += 0.1f;
                if (animationPhase > 2 * Math.PI) animationPhase = 0;
            }).start();
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Glowing animation effect
            float brightness = 0.7f + 0.3f * (float) Math.sin(animationPhase);
            Color glowingColor = new Color(
                Math.min(255, (int) (borderColor.getRed() * brightness)),
                Math.min(255, (int) (borderColor.getGreen() * brightness)),
                Math.min(255, (int) (borderColor.getBlue() * brightness))
            );

            g2d.setStroke(new BasicStroke(2f));
            g2d.setColor(glowingColor);
            g2d.drawRoundRect(x + 1, y + 1, width - 3, height - 3, arc, arc); // Điều chỉnh bán kính ở đây

            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(10, 10, 10, 10); // Padding đều
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

    public JButton getLoginButton() {
        return loginButton;
    }
}

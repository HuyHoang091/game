package com.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.game.ui.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.data.*;
import com.game.rendering.GlobalLoadingManager;

public class AccessFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentPane;
    private LoginPanel loginFrame;
    private ResetPassPanel resetPassPanel;
    private Password password;
    private SignUpPanel signUpPanel;
    private String previousScreen = "Menu";
    private static AccessFrame instance;
    private CharacterGalleryPanel cPanel;

    public ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    public Path filePath = null;

    public String frontendSecret = "";
    private int currentPingMs = 999;
    private JPanel overlay;

    public AccessFrame() {
        instance = this;
        setTitle("Game Access");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (filePath != null) {
                    try {
                        Files.deleteIfExists(filePath);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                if (GameData.token != null) {
                    String username = GameData.user.getUsername();

                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, String> requestBody = new HashMap<>();
                        requestBody.put("username", username);
                        String json = mapper.writeValueAsString(requestBody);

                        HttpClient client = HttpClient.newHttpClient();
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8080/api/auth/logout"))
                                .header("Content-Type", "application/json")
                                .header("Authorization", "Bearer " + GameData.token)
                                .POST(HttpRequest.BodyPublishers.ofString(json))
                                .build();

                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                        if (response.statusCode() == 200){}
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                    scheduler.shutdownNow();
                }
                System.exit(0);
            }
        });

        cardLayout = new CardLayout();
        contentPane = new JPanel(cardLayout);
        
        // Khởi tạo các màn hình
        loginFrame = new LoginPanel(this);
        signUpPanel = new SignUpPanel(this);
        resetPassPanel = new ResetPassPanel(this);
        password = new Password(this);
        
        // Thêm các màn hình vào cardLayout
        contentPane.add(loginFrame, "Login");
        contentPane.add(signUpPanel, "SignUp");
        contentPane.add(resetPassPanel, "RessetPass");
        contentPane.add(password, "Password");
        
        setContentPane(contentPane);
        showLogin();

        GlobalLoadingManager loadingManager = new GlobalLoadingManager(this);
        loadingManager.startLoading(() -> {
            
        });

        new Thread(() -> {
            try {
                HashClient.main(null);
            } catch (Exception e) {}
            
            loadingManager.setLoading(false);
        }).start();

        overlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                int ping = currentPingMs;

                int iconX = getWidth() - 85;
                int iconY = 20;

                String emoji;
                Color pingColor;
                if (ping < 80) {
                    emoji = "●●●";
                    pingColor = Color.GREEN;
                }
                else if (ping < 150){
                    emoji = "●●○";
                    pingColor = Color.YELLOW;
                }
                else if (ping < 300){
                    emoji = "●○○";
                    pingColor = Color.ORANGE;
                }  
                else {
                    emoji = "○○○";
                    pingColor = Color.RED;
                }
                    
                g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 10));
                g.setColor(pingColor);
                g.drawString(emoji, iconX, iconY);

                g.setFont(new Font("Arial", Font.PLAIN, 11));
                g.drawString(ping + "ms", iconX + 30, iconY);
            }
        };

        overlay.setOpaque(false);
        overlay.setLayout(null);
        getLayeredPane().setLayout(null);
        overlay.setBounds(0, 0, getWidth(), getHeight());
        getLayeredPane().add(overlay, JLayeredPane.POPUP_LAYER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                overlay.setBounds(0, 0, getWidth(), getHeight());
            }
        });
    }

    public static AccessFrame getInstance() {
        if (instance == null) {
            instance = new AccessFrame();
        }
        return instance;
    }

    public void showLogin() {
        cardLayout.show(contentPane, "Login");
        if (loginFrame.getLoginButton() != null) {
            this.getRootPane().setDefaultButton(loginFrame.getLoginButton());
        }
    }

    public void showSignUp() {
        cardLayout.show(contentPane, "SignUp");
        if (signUpPanel.getLoginButton() != null) {
            this.getRootPane().setDefaultButton(signUpPanel.getLoginButton());
        }
    }

    public void showRepass() {
        resetPassPanel.setData();
        cardLayout.show(contentPane, "RessetPass");
        if (resetPassPanel.getLoginButton() != null) {
            this.getRootPane().setDefaultButton(resetPassPanel.getLoginButton());
        }
    }

    public void showForgotPass() {
        cardLayout.show(contentPane, "Password");
        if (password.getLoginButton() != null) {
            this.getRootPane().setDefaultButton(password.getLoginButton());
        }
    }

    public void showCharacter(Long id) {
        cPanel = new CharacterGalleryPanel(id);
        contentPane.add(cPanel, "Character");
        cardLayout.show(contentPane, "Character");
    }

    public void goBack() {
        cardLayout.show(contentPane, previousScreen);
    }

    public void LoginSecret() {
        if (scheduler.isShutdown() || scheduler.isTerminated()) {
            scheduler = Executors.newScheduledThreadPool(2);
        }
        scheduler.schedule(() -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, String> requestBody = new HashMap<>();
                requestBody.put("username", GameData.user.getUsername());
                String json = mapper.writeValueAsString(requestBody);

                Properties props = new Properties();
                props.load(getClass().getResourceAsStream("/app.properties"));
                String appCode = props.getProperty("app.part2");

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/appcode/part2"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + GameData.token)
                        .header("App-Code", appCode)
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

                if (response.statusCode() == 408) {
                    JOptionPane.showMessageDialog(this,
                        response.body(),
                        "Cảnh báo",
                        JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                } else {
                    System.out.print("ok 2 " + response.statusCode());
                    downloadAppCodeFragment();
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Không thể kết nối đến Server!",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }, 7, TimeUnit.SECONDS);
        
        scheduler.schedule(() -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, String> requestBody = new HashMap<>();
                requestBody.put("username", GameData.user.getUsername());
                String json = mapper.writeValueAsString(requestBody);

                Path path = Paths.get("app-code-demo.txt");
                String encodedContent = Files.readString(path, StandardCharsets.UTF_8);

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/appcode/part3"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + GameData.token)
                        .header("App-Code", encodedContent)
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

                if (response.statusCode() == 408) {
                    JOptionPane.showMessageDialog(this,
                        "Bạn đang xâm nhập trái phép! Ngắt kết nối với server!",
                        "Cảnh báo",
                        JOptionPane.ERROR_MESSAGE);
                        System.exit(0);
                } else {
                    System.out.print("ok 3 " + response.statusCode());
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Không thể kết nối đến Server!",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }, 14, TimeUnit.SECONDS);
    }

    public void downloadAppCodeFragment() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("username", GameData.user.getUsername());
            String json = mapper.writeValueAsString(requestBody);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/appcode/download"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + GameData.token)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                filePath = Paths.get("app-code-demo.txt");
                Files.copy(response.body(), filePath, StandardCopyOption.REPLACE_EXISTING);

                ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
                scheduler.schedule(() -> {
                    try {
                        Files.deleteIfExists(filePath);
                        filePath = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, 10, TimeUnit.SECONDS);
            } else {
                System.out.println("Tải mã thất bại: " + response.statusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getMs() {
        return currentPingMs;
    }

    public void setMs(int ms) {
        currentPingMs = ms;
        overlay.repaint();
        if (GameWindow.getInstance() != null) {
            GameWindow.getInstance().overlay.repaint();
        }
    }
}
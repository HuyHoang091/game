package com.game;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.MapData;
import com.game.data.GameData;
import com.game.model.AuthResponse;
import com.game.rendering.GlobalLoadingManager;
import com.game.ui.*;
import com.game.resource.*;

public class GameWindow extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentPane;
    private MainMenu mainMenu;
    private GamePanel gamePanel;
    private SettingsPanel settingsPanel;
    private MapSelectScreen mapSelectScreen;
    private InventoryPanel inventoryPanel;
    private String previousScreen = "Menu";
    private static GameWindow instance;
    private ResourceManager resourceManager;

    private boolean isFullScreen = false;
    private GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    private Rectangle windowedBounds;  // Lưu vị trí + kích thước cửa sổ trước fullscreen

    public int level = 1;

    public GameWindow() {
        instance = this;
        setTitle("Game Menu");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        GameWindow.this,
                        "Bạn có chắc muốn thoát game?",
                        "Xác nhận thoát",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (AccessFrame.getInstance().filePath != null) {
                        try {
                            Files.deleteIfExists(AccessFrame.getInstance().filePath);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    Logout();
                    System.exit(0);
                }
            }
        });
        cardLayout = new CardLayout();
        contentPane = new JPanel(cardLayout);
        
        // Khởi tạo các màn hình
        mainMenu = new MainMenu(this);
        mapSelectScreen = new MapSelectScreen(this);
        gamePanel = new GamePanel(this);
        settingsPanel = new SettingsPanel(this);
        inventoryPanel = new InventoryPanel(this);
        
        // Thêm các màn hình vào cardLayout
        contentPane.add(mainMenu, "Menu");
        contentPane.add(mapSelectScreen, "MapSelect");
        contentPane.add(gamePanel, "Game");
        contentPane.add(settingsPanel, "Settings");
        contentPane.add(inventoryPanel, "Inventory");
        
        setContentPane(contentPane);
    }

    public static GameWindow getInstance() {
        if (instance == null) {
            instance = new GameWindow();
        }
        return instance;
    }

    public void showMapSelect() {
        cardLayout.show(contentPane, "MapSelect");
    }

    public void reloadMapSelectScreen() {
        // Gỡ mapSelectScreen khỏi contentPane
        contentPane.remove(mapSelectScreen);

        // Giải phóng tài nguyên
        if (mapSelectScreen != null) {
            mapSelectScreen.dispose();
            mapSelectScreen = null;
        }

        // Gợi ý GC
        System.gc();

        // Tạo lại MapSelectScreen mới với dữ liệu mới nhất
        mapSelectScreen = new MapSelectScreen(this);
        contentPane.add(mapSelectScreen, "MapSelect");
    }

    public void startGame(MapData mapData, int level) {
        this.level = level;
        gamePanel.loadResources(mapData);
        cardLayout.show(contentPane, "Game");
        gamePanel.requestFocus();
        gamePanel.getGameLoop().startGameThread();
    }

    public void showMenu() {
        cardLayout.show(contentPane, "Menu");
    }

    public void showSettings(String fromScreen) {
        previousScreen = fromScreen;
        cardLayout.show(contentPane, "Settings");
        new javax.swing.Timer(200, e -> {
            settingsPanel.registerEscAction();
            ((javax.swing.Timer) e.getSource()).stop();
        }).start();
    }

    public void showInventory(String fromScreen) {
        previousScreen = fromScreen;
        cardLayout.show(contentPane, "Inventory");
        inventoryPanel.updateInventory();
        inventoryPanel.requestFocus();
    }

    public void goBack() {
        cardLayout.show(contentPane, previousScreen);
        if(previousScreen.equals("Game")) {
            GamePanel.getInstance().setPaused(false);
            new javax.swing.Timer(200, e -> {
                gamePanel.requestFocus();
                ((javax.swing.Timer) e.getSource()).stop();
            }).start();
            // gamePanel.startGameThread();
            settingsPanel.removeEscAction();
        }
    }

    public void BackToMenu() {
        gamePanel.getGameLoop().stopGameThread();

        // Gỡ gamePanel khỏi contentPane
        contentPane.remove(gamePanel);

        // Null để GC dễ dọn dẹp
        gamePanel.currentInstance = null;
        gamePanel = null;
        resourceManager.clearAnimationCache();
        MapPreviewManager.previewCache.clear();

        // Tạo lại nếu cần chơi lại
        gamePanel = new GamePanel(this);
        contentPane.add(gamePanel, "Game");

        cardLayout.show(contentPane, "Menu");

        reloadMapSelectScreen();

        System.gc(); // Gợi ý GC
        settingsPanel.removeEscAction();
        GameData.droppedItems.clear(); // Xóa danh sách item rơi
    }

    public void toggleFullScreen() {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        if (isFullScreen) {
            // Thoát fullscreen
            dispose();
            setUndecorated(false);
            setBounds(windowedBounds); // Trả về kích thước cũ
            setVisible(true);
            isFullScreen = false;
        } else {
            // Lưu lại kích thước cửa sổ hiện tại
            windowedBounds = getBounds();

            // Vào fullscreen borderless
            dispose();
            setUndecorated(true);
            setBounds(0, 0, screenSize.width, screenSize.height); // Full màn hình
            setVisible(true);
            isFullScreen = true;
        }
    }

    public boolean isFullScreen() {
        return isFullScreen;
    }

    public int getLevel() {
        return level;
    }

    public void Logout() {
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

            if (response.statusCode() == 200) {
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
        AccessFrame.getInstance().scheduler.shutdownNow();
        mainMenu = null;
        mapSelectScreen = null;
        gamePanel = null;
        settingsPanel = null;
        inventoryPanel = null;
        cardLayout = null;
    }
}
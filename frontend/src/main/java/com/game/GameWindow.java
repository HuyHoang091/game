package com.game;

import javax.swing.*;
import java.awt.*;
import com.game.MapData;
import com.game.data.GameData;
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
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        
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
        gamePanel = null;
        resourceManager.clearAnimationCache();

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
}
package com.game;

import javax.swing.*;
import java.awt.*;
import com.game.MapData;
import com.game.ui.*;

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

    public void startGame(MapData mapData) {
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
            gamePanel.requestFocus();
            // gamePanel.startGameThread();
        }
    }

    public void BackToMenu() {
        gamePanel.getGameLoop().stopGameThread();

        // Gỡ gamePanel khỏi contentPane
        contentPane.remove(gamePanel);

        // Null để GC dễ dọn dẹp
        gamePanel = null;

        // Tạo lại nếu cần chơi lại
        gamePanel = new GamePanel(this);
        contentPane.add(gamePanel, "Game");

        cardLayout.show(contentPane, "Menu");

        System.gc(); // Gợi ý GC
    }
}
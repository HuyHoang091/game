package com.game;

import javax.swing.*;
import java.awt.*;
import com.game.ui.*;
import com.game.data.*;

public class AccessFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentPane;
    private LoginPanel loginFrame;
    private ResetPassPanel resetPassPanel;
    private SignUpPanel signUpPanel;
    private String previousScreen = "Menu";
    private static AccessFrame instance;
    private GameData data;
    private CharacterGalleryPanel cPanel;

    public AccessFrame() {
        instance = this;
        setTitle("Game Access");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        contentPane = new JPanel(cardLayout);
        
        // Khởi tạo các màn hình
        loginFrame = new LoginPanel(this);
        signUpPanel = new SignUpPanel(this);
        resetPassPanel = new ResetPassPanel(this);
        
        // Thêm các màn hình vào cardLayout
        contentPane.add(loginFrame, "Login");
        contentPane.add(signUpPanel, "SignUp");
        contentPane.add(resetPassPanel, "RessetPass");
        
        setContentPane(contentPane);
        showLogin();
    }

    public static AccessFrame getInstance() {
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

    public void showCharacter(Long id) {
        cPanel = new CharacterGalleryPanel(id);
        contentPane.add(cPanel, "Character");
        cardLayout.show(contentPane, "Character");
    }

    public void goBack() {
        cardLayout.show(contentPane, previousScreen);
    }
}
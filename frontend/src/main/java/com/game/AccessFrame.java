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
    private Password password;
    private SignUpPanel signUpPanel;
    private String previousScreen = "Menu";
    private static AccessFrame instance;
    private GameData data;
    private CharacterGalleryPanel cPanel;

    public AccessFrame() {
        instance = this;
        setTitle("Game Access");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        AccessFrame.this,
                        "Bạn có chắc muốn thoát?",
                        "Xác nhận thoát",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (data.token != null && !data.token.isEmpty()) {
                        GameWindow.getInstance().Logout();
                    }
                    System.exit(0);
                }
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

    public void showForgotPass() {
        cardLayout.show(contentPane, "Password");
        if (password.getLoginButton() != null) {
            this.getRootPane().setDefaultButton(password.getLoginButton());
        }
    }

    public void showCharacter(Long id) {
        System.out.print(GameData.token);
        cPanel = new CharacterGalleryPanel(id);
        contentPane.add(cPanel, "Character");
        cardLayout.show(contentPane, "Character");
    }

    public void goBack() {
        cardLayout.show(contentPane, previousScreen);
    }
}
package com.game;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AccessFrame loginFrame = new AccessFrame();
            loginFrame.setVisible(true);
        });
    }
}
package com.game.ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class CustomScrollBarUI extends BasicScrollBarUI {

    private final Dimension THUMB_SIZE = new Dimension(10, 10);

    @Override
    protected JButton createDecreaseButton(int orientation) {
        JButton button = new JButton();
        button.setPreferredSize(THUMB_SIZE);
        button.setVisible(false); // Ẩn nút mũi tên
        return button;
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        JButton button = new JButton();
        button.setPreferredSize(THUMB_SIZE);
        button.setVisible(false); // Ẩn nút mũi tên
        return button;
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        g.setColor(new Color(30, 30, 35)); // Màu nền của track tối hơn
        g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(80, 80, 90));
        g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 5, 5); // Bo góc cho thumb

        g2.setColor(new Color(120, 120, 130));
        g2.drawRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width - 1, thumbBounds.height - 1, 5, 5);
        g2.dispose();
    }
}
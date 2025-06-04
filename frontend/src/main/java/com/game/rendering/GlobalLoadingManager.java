package com.game.rendering;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class GlobalLoadingManager extends JWindow {
    private JProgressBar progressBar;
    private List<Image> loadingImages = new ArrayList<>();
    private int currentImageIndex = 0;
    private Timer imageSwitchTimer;
    private float opacity = 1.0f;

    private volatile boolean loading = true;

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public boolean isLoading() {
        return loading;
    }

    public GlobalLoadingManager(Window parent) {
        super(parent);
        setSize(parent.getSize());
        setLocation(parent.getLocation());
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));
        setLayout(null);

        // Load loading images
        loadingImages.add(new ImageIcon(getClass().getResource("/assets/image.png")).getImage());
        loadingImages.add(new ImageIcon(getClass().getResource("/assets/vatlieu.png")).getImage());
        loadingImages.add(new ImageIcon(getClass().getResource("/assets/image.png")).getImage());

        BackgroundPanel bgPanel = new BackgroundPanel();
        bgPanel.setBounds(0, 0, getWidth(), getHeight());
        bgPanel.setLayout(null);
        setContentPane(bgPanel);

        // Thiết lập progressBar: rộng 80% chiều rộng cửa sổ, cao 25px
        int barWidth = (int)(getWidth() * 0.8);
        int barHeight = 25;
        int barX = (getWidth() - barWidth) / 2;
        // Thanh nằm trong phần dưới 20% chiều cao cửa sổ, căn giữa phần đó
        int bottomHeight = (int)(getHeight() * 0.1);
        int barY = getHeight() - bottomHeight + (bottomHeight - barHeight) / 2;

        progressBar = new JProgressBar(0, 100);
        progressBar.setBounds(barX, barY, barWidth, barHeight);
        progressBar.setStringPainted(true);
        bgPanel.add(progressBar);

        // Timer đổi ảnh loading
        imageSwitchTimer = new Timer(500, e -> {
            currentImageIndex = (currentImageIndex + 1) % loadingImages.size();
            repaint();
        });
    }

    public void startLoading(Runnable taskAfterLoad) {
        loading = true;
        setVisible(true);
        imageSwitchTimer.start();

        Timer progressTimer = new Timer(30, null);
        progressTimer.addActionListener(e -> {
            int val = progressBar.getValue();

            if (!loading) {
                // Nếu load xong (loading == false)
                if (val < 100) {
                    // Tăng nhanh lên 100%
                    progressBar.setValue(Math.min(val + 5, 100));
                } else {
                    // Đạt 100% thì dừng, ẩn loading
                    progressTimer.stop();
                    imageSwitchTimer.stop();
                    fadeOut(() -> {
                        setVisible(false);
                        dispose();
                        taskAfterLoad.run();
                    });
                }
            } else {
                // Nếu vẫn đang load
                if (val < 99) {
                    progressBar.setValue(val + 1);
                } else {
                    // Giữ ở 99% chờ loading thành false
                }
            }
        });
        progressTimer.start();
    }

    private void fadeOut(Runnable afterFade) {
        Timer fadeTimer = new Timer(30, null);
        fadeTimer.addActionListener(e -> {
            opacity -= 0.05f;
            if (opacity <= 0f) {
                fadeTimer.stop();
                afterFade.run();
            } else {
                setOpacity(Math.max(opacity, 0f));
            }
        });
        fadeTimer.start();
    }

    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

            // Nền đen toàn phần
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (!loadingImages.isEmpty()) {
                // Kích thước ảnh: rộng 80% chiều rộng cửa sổ, cao 80% chiều cao cửa sổ
                int imgWidth = (int)(getWidth() * 0.8);
                int imgHeight = (int)(getHeight() * 0.8);

                // Vị trí ảnh: căn giữa theo chiều ngang, nằm ở đầu (top) cửa sổ
                int imgX = (getWidth() - imgWidth) / 2;
                int imgY = (getHeight() - imgHeight - (int)(getHeight() * 0.1)) / 2;

                g2.drawImage(loadingImages.get(currentImageIndex), imgX, imgY, imgWidth, imgHeight, this);
            }

            g2.dispose();
        }
    }
}

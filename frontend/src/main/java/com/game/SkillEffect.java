package com.game;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class SkillEffect {
    int x, y; // Vị trí của hiệu ứng
    int duration; // Thời gian tồn tại của hiệu ứng
    int damage; // Sát thương gây ra
    BufferedImage[] frames; // Các khung hình của hiệu ứng
    int frameIndex = 0; // Khung hình hiện tại
    int frameDelay = 5; // Độ trễ giữa các khung hình
    int frameTick = 0; // Bộ đếm thời gian cho khung hình
    String direction;
    Color debugColor;
    Rectangle skillBox;

    public SkillEffect(int x, int y, int duration, int damage, BufferedImage[] frames, String direction,  Rectangle box, Color debugColor) {
        this.x = x;
        this.y = y;
        this.duration = duration;
        this.damage = damage;
        this.frames = frames;
        this.direction = direction; // Lưu hướng của hiệu ứng
        this.skillBox = box;
        this.debugColor = debugColor;
    }

    public void update() {
        // Giảm thời gian tồn tại
        duration--;

        // Cập nhật khung hình
        if (frames != null && frames.length > 0) { // Kiểm tra frames không null
            frameTick++;
            if (frameTick >= frameDelay) {
                frameTick = 0;
                frameIndex = (frameIndex + 1) % frames.length;
            }
        }
    }

    public void draw(Graphics g, int camX, int camY) {
        if (frames != null && frames.length > 0) {
            Graphics2D g2d = (Graphics2D) g;
            BufferedImage frame = frames[frameIndex];

            // Tính toán vị trí vẽ
            int drawX = x - camX;
            int drawY = y - camY;

            // Tạo AffineTransform để xoay ảnh
            AffineTransform transform = new AffineTransform();
            double scale = 1.8;
            transform.scale(scale, scale);
            transform.translate(drawX, drawY);

            // Xoay ảnh dựa trên hướng
            switch (direction) {
                case "right" -> transform.rotate(Math.toRadians(0), frame.getWidth() / 2.0, frame.getHeight() / 2.0);
                case "left" -> transform.rotate(Math.toRadians(180), frame.getWidth() / 2.0, frame.getHeight() / 2.0);
                case "up" -> transform.rotate(Math.toRadians(270), frame.getWidth() / 2.0, frame.getHeight() / 2.0);
                case "down" -> transform.rotate(Math.toRadians(90), frame.getWidth() / 2.0, frame.getHeight() / 2.0);
            }

            // Vẽ ảnh đã xoay
            g2d.drawImage(frame, transform, null);
        }
        // Vẽ vùng sát thương debug
        // if (debugColor != null) {
        //     Graphics2D g2d = (Graphics2D) g;
        //     double scale = 1.8;
        //     g2d.scale(scale, scale);

        //     Rectangle debugBox = new Rectangle(
        //         skillBox.x - camX,            // Dùng x từ SkillEffect (damageBoxX từ Player)
        //         skillBox.y - camY,            // Dùng y từ SkillEffect (damageBoxY từ Player)
        //         skillBox.width,
        //         skillBox.height
        //     );
            
        //     g2d.setColor(new Color(debugColor.getRed(), debugColor.getGreen(), debugColor.getBlue(), 100));
        //     g2d.fill(debugBox);
            
        //     g2d.setColor(debugColor);
        //     g2d.draw(debugBox);
        // }
    }

    public boolean isExpired() {
        return duration <= 0;
    }
}
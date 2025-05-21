package com.game;

import java.util.ArrayList;
import java.awt.Point;

public class MapData {
    public String mapBackground;    // đường dẫn đến ảnh nền
    public String collisionLayer;   // đường dẫn đến ảnh vật liệu
    public ArrayList<EnemyData> enemies; // danh sách quái vật

    public MapData(String mapBackground, String collisionLayer) {
        this.mapBackground = mapBackground;
        this.collisionLayer = collisionLayer;
        this.enemies = new ArrayList<>();
    }

    // Class con để lưu thông tin quái vật
    public static class EnemyData {
        public int x, y;
        public int width, height;
        public Long health;
        public Long monsterId;
        public boolean isBoss = false;
        public String name;

        public EnemyData(int x, int y, int width, int height, Long health, Long monsterId, String name) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.health = health;
            this.monsterId = monsterId;
            this.name = name;
        }
    }
}
package com.game;

import java.awt.image.BufferedImage;

public class Item {
    private String name;
    private BufferedImage icon;
    private int id;

    public Item(int id, String name, BufferedImage icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    public String getName() { return name; }
    public BufferedImage getIcon() { return icon; }
    public int getId() { return id; }
}

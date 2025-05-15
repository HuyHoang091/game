package com.game;

import java.util.ArrayList;

public class Inventory {
    private final int MAX_SLOTS = 20;
    private ArrayList<Item> items;

    public Inventory() {
        items = new ArrayList<>();
    }

    public boolean addItem(Item item) {
        if (items.size() < MAX_SLOTS) {
            items.add(item);
            return true;
        }
        return false;
    }

    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
        }
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public Item getItemInSlot(int row, int col, int columns) {
        int index = row * columns + col;
        if (index >= 0 && index < items.size()) {
            return items.get(index);
        }
        return null;
    }    
}

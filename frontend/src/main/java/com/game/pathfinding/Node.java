package com.game.pathfinding;

public class Node {
    public int x, y;
    public int gCost; // Distance from start
    public int hCost; // Distance to target
    public Node parent;
    
    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public int getFCost() {
        return gCost + hCost;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return x == node.x && y == node.y;
    }
}
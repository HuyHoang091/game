package com.game.pathfinding;

import java.util.*;
import com.game.Player;
import java.awt.image.BufferedImage;

public class AStar {
    private static final int TILE_SIZE = 32;
    private static final int[][] DIRECTIONS = {
        {0, 1},  // down
        {1, 0},  // right
        {0, -1}, // up
        {-1, 0}  // left
    };

    public static List<Node> findPath(int startX, int startY, int targetX, int targetY, Player player) {
        // Convert coordinates to tile-based
        startX /= TILE_SIZE;
        startY /= TILE_SIZE;
        targetX /= TILE_SIZE;
        targetY /= TILE_SIZE;

        BufferedImage collisionImage = player.collisionImage;
        int mapWidth = collisionImage.getWidth() / TILE_SIZE;
        int mapHeight = collisionImage.getHeight() / TILE_SIZE;

        // Boundary check
        if (startX < 0 || startX >= mapWidth || startY < 0 || startY >= mapHeight ||
            targetX < 0 || targetX >= mapWidth || targetY < 0 || targetY >= mapHeight) {
            return null;
        }

        Node startNode = new Node(startX, startY);
        Node targetNode = new Node(targetX, targetY);

        PriorityQueue<Node> openSet = new PriorityQueue<>(
            Comparator.comparingInt(Node::getFCost)
        );
        Set<Node> closedSet = new HashSet<>();
        Map<Node, Node> cameFrom = new HashMap<>();
        Map<Node, Integer> gScore = new HashMap<>();
        
        gScore.put(startNode, 0);
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.x == targetNode.x && current.y == targetNode.y) {
                return reconstructPath(cameFrom, current);
            }

            closedSet.add(current);

            for (int[] direction : DIRECTIONS) {
                int newX = current.x + direction[0];
                int newY = current.y + direction[1];

                // Skip if out of bounds
                if (newX < 0 || newX >= mapWidth || newY < 0 || newY >= mapHeight) {
                    continue;
                }

                // Check collision using player's collisionImage
                int pixel = collisionImage.getRGB(newX * TILE_SIZE, newY * TILE_SIZE);
                if (((pixel >> 24) & 0xFF) != 0) {
                    continue; // Skip if blocked
                }

                Node neighbor = new Node(newX, newY);
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                int tentativeGScore = gScore.get(current) + 1;

                if (!gScore.containsKey(neighbor) || tentativeGScore < gScore.get(neighbor)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeGScore);
                    neighbor.gCost = tentativeGScore;
                    neighbor.hCost = getManhattanDistance(neighbor, targetNode);
                    
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return null;
    }

    private static int getManhattanDistance(Node a, Node b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private static List<Node> reconstructPath(Map<Node, Node> cameFrom, Node current) {
        List<Node> path = new ArrayList<>();
        path.add(current);
        
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(0, current);
        }
        
        return path;
    }
}
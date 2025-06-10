package com.game.model;

import lombok.Data;

@Data
public class GameMap {
    private Long id;

    private String name;
    private int level;
    private String background;
    private String collisionlayer;
    private String preview;
    private Long enemyId;
    private Long bossId;
}
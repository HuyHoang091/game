package com.game.model;

import lombok.Data;

@Data
public class GameItem {
    private Long id;

    private String name;
    private int level;
    private String type;
    private String mota;
    private Long thuoctinhId;
    private String icon;
}
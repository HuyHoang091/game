package com.game.model;

import lombok.Data;

@Data
public class GameSkill {
    private Long id;

    private String name;
    private String className;
    private int levelRequired;
    private int maxLevel;
    private int manaCost;
    private String mota;
    private int damage;
    private double cooldown;
    private String cauhinh;
    private String icon;
}
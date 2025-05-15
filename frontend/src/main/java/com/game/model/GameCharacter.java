package com.game.model;

import lombok.Data;

@Data
public class GameCharacter {
    private Long id;

    private Long userId;
    private String name;
    private int level;
    private int exp;
    private String className;
}
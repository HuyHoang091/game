package com.game.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameCharacter {
    private Long id;

    private Long userId;
    private String name;
    private int level;
    private int skillPoint;
    private int gold;
    private int exp;
    private String className;
}
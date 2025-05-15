package com.game.model;

import lombok.Data;

@Data
public class GameCharacterSkill {
    private Long id;

    private Long characterId;
    private Long skillId;
    private int level;
    private int slot;
}
package com.game.model;

import lombok.Data;

@Data
public class GameMonster {
    private Long id;

    private String name;
    private int level;
    private Long skillId;
    private Long hp;
    private String cauhinh;
    private Long mapId;
    private int expReward;
    private String behavior;
}
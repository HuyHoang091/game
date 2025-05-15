package com.game.model;

import lombok.Data;

@Data
public class GameItemInstance {
    private Long id;

    private Long itemId;
    private int atk;
    private int def;
    private int hp;
    private int mp;
    private Double critRate;
    private Double critDmg;
}
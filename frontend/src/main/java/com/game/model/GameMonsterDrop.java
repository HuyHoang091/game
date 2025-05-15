package com.game.model;

import lombok.Data;

@Data
public class GameMonsterDrop {
    private Long id;

    private Long monsterId;
    private Long itemId;
    private Double dropRate;
}
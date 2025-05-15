package com.game.model;

import lombok.Data;

@Data
public class GameInventory {
    private Long id;

    private Long characterId;
    private Long itemId;
    private Long itemInstanceId;
    private int quantity;
    private boolean equipped;
}
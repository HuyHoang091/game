package com.game.model;

import lombok.Data;

@Data
public class GameThuocTinh {
    private Long id;

    private String name;
    private Double atk;
    private Double def;
    private Double hp;
    private Double mp;
    private Double critRate;
    private Double critDmg;
}
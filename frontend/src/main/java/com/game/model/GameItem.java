package com.game.model;

import lombok.Data;

@Data
public class GameItem {
    private Long id;

    private String name;
    private String type;
    private String mota;
    private String thuoctinh;
    private String icon;
}
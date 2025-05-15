package com.game.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "item_instance")
public class ItemInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long itemId;
    private int atk;
    private int def;
    private int hp;
    private int mp;
    private Double critRate;
    private Double critDmg;
}
package com.game.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "inventory")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long characterId;
    private Long itemId;
    private Long itemInstanceId;
    private int quantity;
    private boolean equipped;
}
package com.game.Model;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "map")
public class Map {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int level;
    private String background;
    private String collisionlayer;
    private String preview;
    private Long enemyId;
    private Long bossId;
}
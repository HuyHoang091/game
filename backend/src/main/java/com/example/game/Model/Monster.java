package com.game.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "monsters")
public class Monster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int level;
    private Long skillId;
    private int hp;
    private String cauhinh;
    private Long mapId;
    private int expReward;
    private String behavior;
}
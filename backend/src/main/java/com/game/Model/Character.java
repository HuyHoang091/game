package com.game.Model;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "characters")
public class Character {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String name;
    private int level;
    private int skillPoint;
    private int gold;
    private int exp;
    private String className;
}
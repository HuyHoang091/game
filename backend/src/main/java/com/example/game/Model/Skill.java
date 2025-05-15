package com.game.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "skills")
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String className;
    private int levelRequired;
    private int manaCost;
    private String mota;
    private int damage;
    private String cauhinh;
}
package com.game.Model;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "monsters_drop")
public class MonsterDrop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long monsterId;
    private Long itemId;
    private Double dropRate;
}
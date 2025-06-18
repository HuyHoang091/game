package com.game.Model;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "character_skills")
public class CharacterSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long characterId;
    private Long skillId;
    private int level;
    private int slot;
}
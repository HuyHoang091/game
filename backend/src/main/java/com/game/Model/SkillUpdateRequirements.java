package com.game.Model;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "skill_update_requirements")
public class SkillUpdateRequirements {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long skillId;
    private int level;
    private int levelRequired;
    private int pointRequired;
    private int goldRequired;
}
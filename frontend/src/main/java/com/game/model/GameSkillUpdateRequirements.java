package com.game.model;

import lombok.Data;

@Data
public class GameSkillUpdateRequirements {
    private Long id;

    private Long skillId;
    private int level;
    private int levelRequired;
    private int pointRequired;
    private int goldRequired;
}
package com.game.Repository;

import com.game.Model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    // List<Skill> findById(Long id);
}
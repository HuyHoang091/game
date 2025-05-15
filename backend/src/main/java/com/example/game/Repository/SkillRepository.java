package com.game.repository;

import com.game.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    // List<Skill> findById(Long id);
}
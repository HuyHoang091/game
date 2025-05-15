package com.game.repository;

import com.game.entity.SkillUpdateRequirements;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SkillUpdateRequirementsRepository extends JpaRepository<SkillUpdateRequirements, Long> {
    // List<SkillUpdateRequirements> findById(Long id);
}
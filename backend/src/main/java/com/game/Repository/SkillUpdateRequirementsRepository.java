package com.game.Repository;

import com.game.Model.SkillUpdateRequirements;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillUpdateRequirementsRepository extends JpaRepository<SkillUpdateRequirements, Long> {
    // List<SkillUpdateRequirements> findById(Long id);
}
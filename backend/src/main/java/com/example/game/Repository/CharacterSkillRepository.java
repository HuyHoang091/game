package com.game.repository;

import com.game.entity.CharacterSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CharacterSkillRepository extends JpaRepository<CharacterSkill, Long> {
    List<CharacterSkill> findByCharacterId(Long characterId);
}
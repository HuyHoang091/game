package com.game.Repository;

import com.game.Model.CharacterSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

public interface CharacterSkillRepository extends JpaRepository<CharacterSkill, Long> {
    List<CharacterSkill> findByCharacterId(Long characterId);
    Optional<CharacterSkill> findByCharacterIdAndSkillId(Long characterId, Long skillId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO character_skills (id, character_id, skill_id, level, slot) " +
                   "VALUES (:id, :characterId, :skillId, :level, :slot)", nativeQuery = true)
    void insert(@Param("id") Long id,
                              @Param("characterId") Long characterId,
                              @Param("skillId") Long skillId,
                              @Param("level") int level,
                              @Param("slot") int slot);
}
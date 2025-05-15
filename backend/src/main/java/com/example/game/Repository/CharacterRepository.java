package com.game.repository;

import com.game.entity.Character;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CharacterRepository extends JpaRepository<Character, Long> {
    List<Character> findByUserId(Long userId);
}
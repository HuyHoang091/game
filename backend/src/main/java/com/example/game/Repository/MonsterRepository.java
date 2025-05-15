package com.game.repository;

import com.game.entity.Monster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MonsterRepository extends JpaRepository<Monster, Long> {
    // List<Monster> findById(Long id);
}
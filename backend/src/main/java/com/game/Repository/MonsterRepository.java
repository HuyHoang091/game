package com.game.Repository;

import com.game.Model.Monster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonsterRepository extends JpaRepository<Monster, Long> {
    // List<Monster> findById(Long id);
}
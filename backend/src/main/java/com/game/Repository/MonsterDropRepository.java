package com.game.Repository;

import com.game.Model.MonsterDrop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonsterDropRepository extends JpaRepository<MonsterDrop, Long> {
    // List<MonsterDrop> findById(Long id);
}
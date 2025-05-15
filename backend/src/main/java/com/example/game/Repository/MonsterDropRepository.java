package com.game.repository;

import com.game.entity.MonsterDrop;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MonsterDropRepository extends JpaRepository<MonsterDrop, Long> {
    // List<MonsterDrop> findById(Long id);
}
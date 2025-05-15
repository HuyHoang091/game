package com.game.repository;

import com.game.entity.Map;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MapRepository extends JpaRepository<Map, Long> {
    // List<Map> findById(Long id);
}
package com.game.Repository;

import com.game.Model.Map;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MapRepository extends JpaRepository<Map, Long> {
    // List<Map> findById(Long id);
}
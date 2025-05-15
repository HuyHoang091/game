package com.game.repository;

import com.game.entity.ItemInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItemInstanceRepository extends JpaRepository<ItemInstance, Long> {
    // List<ItemInstance> findByCharacterId(Long characterId);
}
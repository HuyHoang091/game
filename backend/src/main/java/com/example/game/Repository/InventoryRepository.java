package com.game.repository;

import com.game.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import javax.transaction.Transactional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByCharacterId(Long characterId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO inventory (id, character_id, item_id, item_instance_id, quantity, equipped) " +
                   "VALUES (:id, :characterId, :itemId, :itemInstanceId, :quantity, :equipped)", nativeQuery = true)
    void insert(@Param("id") Long id,
                              @Param("characterId") Long characterId,
                              @Param("itemId") Long itemId,
                              @Param("itemInstanceId") Long itemInstanceId,
                              @Param("quantity") int quantity,
                              @Param("equipped") boolean equipped);
}
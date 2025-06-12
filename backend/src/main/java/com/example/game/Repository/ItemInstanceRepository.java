package com.game.repository;

import com.game.entity.ItemInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import javax.transaction.Transactional;

public interface ItemInstanceRepository extends JpaRepository<ItemInstance, Long> {
    // List<ItemInstance> findByCharacterId(Long characterId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO item_instance (id, item_id, atk, def, hp, mp, crit_rate, crit_dmg) " +
                   "VALUES (:id, :itemId, :atk, :def, :hp, :mp, :critRate, :critDmg)", nativeQuery = true)
    void insert(@Param("id") Long id,
                              @Param("itemId") Long itemId,
                              @Param("atk") int atk,
                              @Param("def") int def,
                              @Param("hp") int hp,
                              @Param("mp") int mp,
                              @Param("critRate") Double critRate,
                              @Param("critDmg") Double critDmg);
}
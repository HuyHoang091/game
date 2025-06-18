package com.game.Repository;

import com.game.Model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
    // List<Item> findById(Long id);
}
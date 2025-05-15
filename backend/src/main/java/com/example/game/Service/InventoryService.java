package com.game.service;

import com.game.entity.Inventory;
import com.game.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InventoryService {
    @Autowired
    private InventoryRepository characterRepository;

    public List<Inventory> getAllCharactersSkill(Long character_id) {
        return characterRepository.findByCharacterId(character_id);
    }
}
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

    public List<Inventory> getAllInventory() {
        return characterRepository.findAll();
    }

    public Inventory createInventory(Inventory inventory) {
        return characterRepository.save(inventory);
    }

    public Inventory updateInventory(Long id, Inventory newChar) {
        return characterRepository.findById(id).map(existing -> {
            existing.setCharacterId(newChar.getCharacterId());
            existing.setItemId(newChar.getItemId());
            existing.setItemInstanceId(newChar.getItemInstanceId());
            existing.setQuantity(newChar.getQuantity());
            existing.setEquipped(newChar.isEquipped());
            return characterRepository.save(existing);
        }).orElse(null);
    }

    public boolean deleteInventory(Long id) {
        if (characterRepository.existsById(id)) {
            characterRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
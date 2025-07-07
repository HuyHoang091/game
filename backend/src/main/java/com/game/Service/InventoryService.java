package com.game.Service;

import com.game.Model.Inventory;
import com.game.Repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {
    @Autowired
    private InventoryRepository inventoryRepository;

    public List<Inventory> getAllInventoryCharacter(Long character_id) {
        return inventoryRepository.findByCharacterId(character_id);
    }

    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public Inventory createInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    public Inventory updateInventory(Long id, Inventory newChar) {
        return inventoryRepository.findById(id).map(existing -> {
            existing.setCharacterId(newChar.getCharacterId());
            existing.setItemId(newChar.getItemId());
            existing.setItemInstanceId(newChar.getItemInstanceId());
            existing.setQuantity(newChar.getQuantity());
            existing.setEquipped(newChar.isEquipped());
            return inventoryRepository.save(existing);
        }).orElse(null);
    }

    public void updateListInventory(List<Inventory> characters) {
        for (Inventory character : characters) {
            Optional<Inventory> existing = inventoryRepository.findById(character.getId());
            if (existing.isPresent()) {
                inventoryRepository.save(character); // update
            } else {
                inventoryRepository.insert(character.getId(), character.getCharacterId(), character.getItemId(), character.getItemInstanceId(), character.getQuantity(), character.isEquipped()); // insert
            }
        }
    }

    public boolean deleteInventory(Long id) {
        if (inventoryRepository.existsById(id)) {
            inventoryRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
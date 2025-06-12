package com.game.service;

import com.game.entity.ItemInstance;
import com.game.repository.ItemInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ItemInstanceService {
    @Autowired
    private ItemInstanceRepository characterRepository;

    public List<ItemInstance> getAllItemInstance() {
        return characterRepository.findAll();
    }

    public ItemInstance createItemInstance(ItemInstance iteminstance) {
        return characterRepository.save(iteminstance);
    }

    public ItemInstance updateItemInstance(Long id, ItemInstance newChar) {
        return characterRepository.findById(id).map(existing -> {
            existing.setItemId(newChar.getItemId());
            existing.setAtk(newChar.getAtk());
            existing.setDef(newChar.getDef());
            existing.setHp(newChar.getHp());
            existing.setMp(newChar.getMp());
            existing.setCritRate(newChar.getCritRate());
            existing.setCritDmg(newChar.getCritDmg());
            return characterRepository.save(existing);
        }).orElse(null);
    }

    public void updateListItemInstance(List<ItemInstance> characters) {
        for (ItemInstance character : characters) {
            Optional<ItemInstance> existing = characterRepository.findById(character.getId());
            if (existing.isPresent()) {
                characterRepository.save(character); // update
            } else {
                characterRepository.insert(character.getId(), character.getItemId(), character.getAtk(), character.getDef(), character.getHp(), character.getMp(), character.getCritRate(), character.getCritDmg()); // insert
            }
        }
    }

    public boolean deleteItemInstance(Long id) {
        if (characterRepository.existsById(id)) {
            characterRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
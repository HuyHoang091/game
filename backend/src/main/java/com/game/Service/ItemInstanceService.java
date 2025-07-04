package com.game.Service;

import com.game.Model.ItemInstance;
import com.game.Repository.ItemInstanceRepository;
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

    public boolean isValidItemInstance(ItemInstance item) {
        Long id = item.getId();
        if (id == null || id == 0) return true;

        ItemInstance base = characterRepository.findById(id).orElse(null);
        if (base == null) return true;

        boolean valid = item.getAtk() == base.getAtk()
            && item.getDef() == base.getDef()
            && item.getHp() == base.getHp()
            && item.getMp() == base.getMp()
            && equalsDouble(item.getCritRate(), base.getCritRate())
            && equalsDouble(item.getCritDmg(), base.getCritDmg());

        return valid;
    }

    private boolean equalsDouble(double a, double b) {
        return Math.abs(a - b) < 0.0001;
    }
}
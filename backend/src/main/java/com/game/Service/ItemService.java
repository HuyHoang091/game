package com.game.Service;

import com.game.Model.Item;
import com.game.Repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ItemService {
    @Autowired
    private ItemRepository characterRepository;

    public List<Item> getAllItem() {
        return characterRepository.findAll();
    }

    public Item createItem(Item item) {
        return characterRepository.save(item);
    }

    public Item updateItem(Long id, Item newChar) {
        return characterRepository.findById(id).map(existing -> {
            existing.setName(newChar.getName());
            existing.setType(newChar.getType());
            existing.setMota(newChar.getMota());
            existing.setThuoctinh(newChar.getThuoctinh());
            existing.setIcon(newChar.getIcon());
            return characterRepository.save(existing);
        }).orElse(null);
    }

    public boolean deleteItem(Long id) {
        if (characterRepository.existsById(id)) {
            characterRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
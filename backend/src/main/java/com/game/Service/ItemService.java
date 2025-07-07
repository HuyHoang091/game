package com.game.Service;

import com.game.Model.Item;
import com.game.Repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;

    public List<Item> getAllItem() {
        return itemRepository.findAll();
    }

    public Item createItem(Item item) {
        return itemRepository.save(item);
    }

    public Item updateItem(Long id, Item newChar) {
        return itemRepository.findById(id).map(existing -> {
            existing.setName(newChar.getName());
            existing.setType(newChar.getType());
            existing.setMota(newChar.getMota());
            existing.setThuoctinh(newChar.getThuoctinh());
            existing.setIcon(newChar.getIcon());
            return itemRepository.save(existing);
        }).orElse(null);
    }

    public boolean deleteItem(Long id) {
        if (itemRepository.existsById(id)) {
            itemRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
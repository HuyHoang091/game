package com.game.service;

import com.game.entity.Item;
import com.game.repository.ItemRepository;
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
}
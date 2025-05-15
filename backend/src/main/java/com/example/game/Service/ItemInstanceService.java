package com.game.service;

import com.game.entity.ItemInstance;
import com.game.repository.ItemInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ItemInstanceService {
    @Autowired
    private ItemInstanceRepository characterRepository;

    public List<ItemInstance> getAllItemInstance() {
        return characterRepository.findAll();
    }
}
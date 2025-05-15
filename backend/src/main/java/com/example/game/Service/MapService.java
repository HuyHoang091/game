package com.game.service;

import com.game.entity.Map;
import com.game.repository.MapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MapService {
    @Autowired
    private MapRepository characterRepository;

    public List<Map> getAllMap() {
        return characterRepository.findAll();
    }
}
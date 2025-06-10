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

    public Map createMap(Map Map) {
        return characterRepository.save(Map);
    }

    public Map updateMap(Long id, Map newChar) {
        return characterRepository.findById(id).map(existing -> {
            existing.setName(newChar.getName());
            existing.setLevel(newChar.getLevel());
            existing.setBackground(newChar.getBackground());
            existing.setCollisionlayer(newChar.getCollisionlayer());
            existing.setPreview(newChar.getPreview());
            existing.setEnemyId(newChar.getEnemyId());
            existing.setBossId(newChar.getBossId());
            return characterRepository.save(existing);
        }).orElse(null);
    }

    public boolean deleteMap(Long id) {
        if (characterRepository.existsById(id)) {
            characterRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
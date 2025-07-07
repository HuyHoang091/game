package com.game.Service;

import com.game.Model.Map;
import com.game.Repository.MapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MapService {
    @Autowired
    private MapRepository mapRepository;

    public List<Map> getAllMap() {
        return mapRepository.findAll();
    }

    public Map createMap(Map Map) {
        return mapRepository.save(Map);
    }

    public Map updateMap(Long id, Map newChar) {
        return mapRepository.findById(id).map(existing -> {
            existing.setName(newChar.getName());
            existing.setLevel(newChar.getLevel());
            existing.setBackground(newChar.getBackground());
            existing.setCollisionlayer(newChar.getCollisionlayer());
            existing.setPreview(newChar.getPreview());
            existing.setEnemyId(newChar.getEnemyId());
            existing.setBossId(newChar.getBossId());
            return mapRepository.save(existing);
        }).orElse(null);
    }

    public boolean deleteMap(Long id) {
        if (mapRepository.existsById(id)) {
            mapRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
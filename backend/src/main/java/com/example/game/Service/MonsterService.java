package com.game.service;

import com.game.entity.Monster;
import com.game.repository.MonsterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MonsterService {
    @Autowired
    private MonsterRepository characterRepository;

    public List<Monster> getAllMonster() {
        return characterRepository.findAll();
    }

    public Monster createMonster(Monster Monster) {
        return characterRepository.save(Monster);
    }

    public Monster updateMonster(Long id, Monster newChar) {
        return characterRepository.findById(id).map(existing -> {
            existing.setName(newChar.getName());
            existing.setLevel(newChar.getLevel());
            existing.setHp(newChar.getHp());
            existing.setCauhinh(newChar.getCauhinh());
            existing.setMapId(newChar.getMapId());
            existing.setExpReward(newChar.getExpReward());
            existing.setBehavior(newChar.getBehavior());
            return characterRepository.save(existing);
        }).orElse(null);
    }

    public boolean deleteMonster(Long id) {
        if (characterRepository.existsById(id)) {
            characterRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
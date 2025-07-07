package com.game.Service;

import com.game.Model.Monster;
import com.game.Repository.MonsterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MonsterService {
    @Autowired
    private MonsterRepository monsterRepository;

    public List<Monster> getAllMonster() {
        return monsterRepository.findAll();
    }

    public Monster createMonster(Monster Monster) {
        return monsterRepository.save(Monster);
    }

    public Monster updateMonster(Long id, Monster newChar) {
        return monsterRepository.findById(id).map(existing -> {
            existing.setName(newChar.getName());
            existing.setLevel(newChar.getLevel());
            existing.setHp(newChar.getHp());
            existing.setCauhinh(newChar.getCauhinh());
            existing.setExpReward(newChar.getExpReward());
            existing.setBehavior(newChar.getBehavior());
            return monsterRepository.save(existing);
        }).orElse(null);
    }

    public boolean deleteMonster(Long id) {
        if (monsterRepository.existsById(id)) {
            monsterRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
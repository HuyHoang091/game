package com.game.Service;

import com.game.Model.MonsterDrop;
import com.game.Repository.MonsterDropRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MonsterDropService {
    @Autowired
    private MonsterDropRepository monsterDropRepository;

    public List<MonsterDrop> getAllMonsterDrop() {
        return monsterDropRepository.findAll();
    }

    public MonsterDrop createMonsterDrop(MonsterDrop MonsterDrop) {
        return monsterDropRepository.save(MonsterDrop);
    }

    public MonsterDrop updateMonsterDrop(Long id, MonsterDrop newChar) {
        return monsterDropRepository.save(newChar);
    }

    public boolean deleteMonsterDrop(Long id) {
        if (monsterDropRepository.existsById(id)) {
            monsterDropRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
package com.game.Service;

import com.game.Model.MonsterDrop;
import com.game.Repository.MonsterDropRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MonsterDropService {
    @Autowired
    private MonsterDropRepository characterRepository;

    public List<MonsterDrop> getAllMonsterDrop() {
        return characterRepository.findAll();
    }

    public MonsterDrop createMonsterDrop(MonsterDrop MonsterDrop) {
        return characterRepository.save(MonsterDrop);
    }

    public MonsterDrop updateMonsterDrop(Long id, MonsterDrop newChar) {
        return characterRepository.save(newChar);
    }

    public boolean deleteMonsterDrop(Long id) {
        if (characterRepository.existsById(id)) {
            characterRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
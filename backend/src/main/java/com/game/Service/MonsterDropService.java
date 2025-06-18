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
}
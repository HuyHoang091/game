package com.game.service;

import com.game.entity.MonsterDrop;
import com.game.repository.MonsterDropRepository;
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
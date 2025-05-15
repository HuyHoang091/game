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
}
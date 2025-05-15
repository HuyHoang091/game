package com.game.service;

import com.game.entity.Skill;
import com.game.repository.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SkillService {
    @Autowired
    private SkillRepository characterRepository;

    public List<Skill> getAllSkill() {
        return characterRepository.findAll();
    }
}
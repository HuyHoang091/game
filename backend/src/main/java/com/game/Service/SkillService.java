package com.game.Service;

import com.game.Model.Skill;
import com.game.Repository.SkillRepository;
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
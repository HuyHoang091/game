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

    public Skill createSkill(Skill Skill) {
        return characterRepository.save(Skill);
    }

    public Skill updateSkill(Long id, Skill newChar) {
        return characterRepository.save(newChar);
    }

    public boolean deleteSkill(Long id) {
        if (characterRepository.existsById(id)) {
            characterRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
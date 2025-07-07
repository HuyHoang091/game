package com.game.Service;

import com.game.Model.Skill;
import com.game.Repository.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SkillService {
    @Autowired
    private SkillRepository skillRepository;

    public List<Skill> getAllSkill() {
        return skillRepository.findAll();
    }

    public Skill createSkill(Skill Skill) {
        return skillRepository.save(Skill);
    }

    public Skill updateSkill(Long id, Skill newChar) {
        return skillRepository.save(newChar);
    }

    public boolean deleteSkill(Long id) {
        if (skillRepository.existsById(id)) {
            skillRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
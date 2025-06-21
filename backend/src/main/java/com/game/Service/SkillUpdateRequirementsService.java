package com.game.Service;

import com.game.Model.SkillUpdateRequirements;
import com.game.Repository.SkillUpdateRequirementsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SkillUpdateRequirementsService {
    @Autowired
    private SkillUpdateRequirementsRepository characterRepository;

    public List<SkillUpdateRequirements> getAllSkillUpdateRequirements() {
        return characterRepository.findAll();
    }

    public SkillUpdateRequirements createSkillUpdateRequirements(SkillUpdateRequirements SkillUpdateRequirements) {
        return characterRepository.save(SkillUpdateRequirements);
    }

    public SkillUpdateRequirements updateSkillUpdateRequirements(Long id, SkillUpdateRequirements newChar) {
        return characterRepository.save(newChar);
    }

    public boolean deleteSkillUpdateRequirements(Long id) {
        if (characterRepository.existsById(id)) {
            characterRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
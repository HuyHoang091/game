package com.game.Service;

import com.game.Model.SkillUpdateRequirements;
import com.game.Repository.SkillUpdateRequirementsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SkillUpdateRequirementsService {
    @Autowired
    private SkillUpdateRequirementsRepository skillUpdateRequirementsRepository;

    public List<SkillUpdateRequirements> getAllSkillUpdateRequirements() {
        return skillUpdateRequirementsRepository.findAll();
    }

    public SkillUpdateRequirements createSkillUpdateRequirements(SkillUpdateRequirements SkillUpdateRequirements) {
        return skillUpdateRequirementsRepository.save(SkillUpdateRequirements);
    }

    public SkillUpdateRequirements updateSkillUpdateRequirements(Long id, SkillUpdateRequirements newChar) {
        return skillUpdateRequirementsRepository.save(newChar);
    }

    public boolean deleteSkillUpdateRequirements(Long id) {
        if (skillUpdateRequirementsRepository.existsById(id)) {
            skillUpdateRequirementsRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
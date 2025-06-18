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
}
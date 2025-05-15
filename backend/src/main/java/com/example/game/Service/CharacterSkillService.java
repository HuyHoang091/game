package com.game.service;

import com.game.entity.CharacterSkill;
import com.game.repository.CharacterSkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CharacterSkillService {
    @Autowired
    private CharacterSkillRepository characterRepository;

    public List<CharacterSkill> getAllCharactersSkill(Long character_id) {
        return characterRepository.findByCharacterId(character_id);
    }
}
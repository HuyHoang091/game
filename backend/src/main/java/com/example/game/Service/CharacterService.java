package com.game.service;

import com.game.entity.Character;
import com.game.repository.CharacterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CharacterService {
    @Autowired
    private CharacterRepository characterRepository;

    public List<Character> getCharacters(Long user_id) {
        return characterRepository.findByUserId(user_id);
    }

    public List<Character> getAllCharacter() {
        return characterRepository.findAll();
    }

    public Character createCharacter(Character character) {
        return characterRepository.save(character);
    }

    public Character updateCharacter(Long id, Character newChar) {
        return characterRepository.findById(id).map(existing -> {
            existing.setName(newChar.getName());
            existing.setLevel(newChar.getLevel());
            existing.setSkillPoint(newChar.getSkillPoint());
            existing.setGold(newChar.getGold());
            existing.setExp(newChar.getExp());
            return characterRepository.save(existing);
        }).orElse(null);
    }
    
    public boolean deleteCharacter(Long id) {
        if (characterRepository.existsById(id)) {
            characterRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
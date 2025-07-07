package com.game.Service;

import com.game.Model.CharacterSkill;
import com.game.Repository.CharacterSkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CharacterSkillService {
    @Autowired
    private CharacterSkillRepository characterSkillRepository;

    public List<CharacterSkill> getAllCharactersSkill(Long character_id) {
        return characterSkillRepository.findByCharacterId(character_id);
    }

    public List<CharacterSkill> getAllCharacterSkill() {
        return characterSkillRepository.findAll();
    }

    public CharacterSkill createCharacterSkill(CharacterSkill characterSkill) {
        return characterSkillRepository.save(characterSkill);
    }

    public CharacterSkill updateCharacterSkill(Long id, CharacterSkill newChar) {
        return characterSkillRepository.findById(id).map(existing -> {
            existing.setCharacterId(newChar.getCharacterId());
            existing.setSkillId(newChar.getSkillId());
            existing.setLevel(newChar.getLevel());
            existing.setSlot(newChar.getSlot());
            return characterSkillRepository.save(existing);
        }).orElse(null);
    }

    public void updateListCharacterSkill(List<CharacterSkill> characters) {
        for (CharacterSkill character : characters) {
            Optional<CharacterSkill> existing = characterSkillRepository
                    .findByCharacterIdAndSkillId(character.getCharacterId(), character.getSkillId());
            if (existing.isPresent()) {
                characterSkillRepository.save(character); // update
            } else {
                characterSkillRepository.insert(character.getId(), character.getCharacterId(), character.getSkillId(),
                        character.getLevel(), character.getSlot()); // insert
            }
        }
    }

    public boolean deleteCharacterSkill(Long id) {
        if (characterSkillRepository.existsById(id)) {
            characterSkillRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
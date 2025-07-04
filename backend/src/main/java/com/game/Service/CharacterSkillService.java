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
    private CharacterSkillRepository characterRepository;

    public List<CharacterSkill> getAllCharactersSkill(Long character_id) {
        return characterRepository.findByCharacterId(character_id);
    }

    public List<CharacterSkill> getAllCharacterSkill() {
        return characterRepository.findAll();
    }

    public CharacterSkill createCharacterSkill(CharacterSkill characterSkill) {
        return characterRepository.save(characterSkill);
    }

    public CharacterSkill updateCharacterSkill(Long id, CharacterSkill newChar) {
        return characterRepository.findById(id).map(existing -> {
            existing.setCharacterId(newChar.getCharacterId());
            existing.setSkillId(newChar.getSkillId());
            existing.setLevel(newChar.getLevel());
            existing.setSlot(newChar.getSlot());
            return characterRepository.save(existing);
        }).orElse(null);
    }

    public void updateListCharacterSkill(List<CharacterSkill> characters) {
        for (CharacterSkill character : characters) {
            Optional<CharacterSkill> existing = characterRepository
                    .findByCharacterIdAndSkillId(character.getCharacterId(), character.getSkillId());
            if (existing.isPresent()) {
                characterRepository.save(character); // update
            } else {
                characterRepository.insert(character.getId(), character.getCharacterId(), character.getSkillId(),
                        character.getLevel(), character.getSlot()); // insert
            }
        }
    }

    public boolean deleteCharacterSkill(Long id) {
        if (characterRepository.existsById(id)) {
            characterRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
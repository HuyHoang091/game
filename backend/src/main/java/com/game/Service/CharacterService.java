package com.game.Service;

import com.game.Model.Character;
import com.game.Repository.CharacterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

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
        return characterRepository.save(newChar);
    }

    public void updateListCharacters(List<Character> characters) {
        for (Character character : characters) {
            Optional<Character> existing = characterRepository.findById(character.getId());
            if (existing.isPresent()) {
                characterRepository.save(character); // update
            } else {
                characterRepository.save(character); // insert
            }
        }
    }
    
    public boolean deleteCharacter(Long id) {
        if (characterRepository.existsById(id)) {
            characterRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
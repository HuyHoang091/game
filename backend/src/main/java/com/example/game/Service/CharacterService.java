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
}
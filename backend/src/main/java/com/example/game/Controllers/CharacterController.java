package com.game.controller;

import com.game.entity.Character;
import com.game.service.CharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/characters")
public class CharacterController {

    @Autowired
    private CharacterService characterService;

    @GetMapping("/{user_id}")
    public ResponseEntity<List<Character>> getUserById(@PathVariable Long user_id) {
        List<Character> characters = characterService.getCharacters(user_id);
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }
}

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

    @GetMapping("/")
    public ResponseEntity<List<Character>> getAllCharacter() {
        List<Character> characters = characterService.getAllCharacter();
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/")
    public ResponseEntity<Character> createCharacter(@RequestBody Character character) {
        Character created = characterService.createCharacter(character);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Character> updateCharacter(@PathVariable Long id, @RequestBody Character character) {
        Character updated = characterService.updateCharacter(id, character);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharacter(@PathVariable Long id) {
        boolean deleted = characterService.deleteCharacter(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/batch")
    public ResponseEntity<Void> updateCharacters(@RequestBody List<Character> characters) {
        characterService.updateListCharacters(characters);
        return ResponseEntity.ok().build();
    }
}

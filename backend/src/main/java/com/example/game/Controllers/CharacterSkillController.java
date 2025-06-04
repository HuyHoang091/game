package com.game.controller;

import com.game.entity.CharacterSkill;
import com.game.service.CharacterSkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/character_skills")
public class CharacterSkillController {

    @Autowired
    private CharacterSkillService characterService;

    @GetMapping("/{character_id}")
    public ResponseEntity<List<CharacterSkill>> getAllCharactersSkill(@PathVariable Long character_id) {
        List<CharacterSkill> characters = characterService.getAllCharactersSkill(character_id);
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/")
    public ResponseEntity<List<CharacterSkill>> getAllCharacterSkill() {
        List<CharacterSkill> characters = characterService.getAllCharacterSkill();
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/")
    public ResponseEntity<CharacterSkill> createCharacterSkill(@RequestBody CharacterSkill characterSkill) {
        CharacterSkill create = characterService.createCharacterSkill(characterSkill);
        return ResponseEntity.ok(create);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CharacterSkill> updateCharacterSkill(@PathVariable Long id, @RequestBody CharacterSkill characterSkill) {
        CharacterSkill update = characterService.updateCharacterSkill(id, characterSkill);
        if(update != null){
            return ResponseEntity.ok(update);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CharacterSkill> deleteCharacterSkill(@PathVariable Long id) {
        boolean delete = characterService.deleteCharacterSkill(id);
        if(delete){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
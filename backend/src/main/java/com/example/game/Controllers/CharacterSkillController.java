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
}
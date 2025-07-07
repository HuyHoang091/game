package com.game.Controllers;

import com.game.Model.CharacterSkill;
import com.game.Service.CharacterSkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/character_skills")
public class CharacterSkillController {

    @Autowired
    private CharacterSkillService characterSkillService;

    @PreAuthorize("#user_id == principal.id or hasRole('ADMIN')")
    @GetMapping("/{user_id}/{character_id}")
    public ResponseEntity<List<CharacterSkill>> getAllCharactersSkill(@PathVariable Long user_id, @PathVariable Long character_id) {
        List<CharacterSkill> characters = characterSkillService.getAllCharactersSkill(character_id);
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<List<CharacterSkill>> getAllCharacterSkill() {
        List<CharacterSkill> characters = characterSkillService.getAllCharacterSkill();
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/")
    public ResponseEntity<?> createCharacterSkill(@RequestBody CharacterSkill characterSkill) {
        CharacterSkill create = characterSkillService.createCharacterSkill(characterSkill);
        if (create != null) {
            return ResponseEntity.ok("Thêm mới thành công!");
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCharacterSkill(@PathVariable Long id, @RequestBody CharacterSkill characterSkill) {
        CharacterSkill update = characterSkillService.updateCharacterSkill(id, characterSkill);
        if(update != null){
            return ResponseEntity.ok("Cập nhật thành công!");
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCharacterSkill(@PathVariable Long id) {
        boolean delete = characterSkillService.deleteCharacterSkill(id);
        if(delete){
            return ResponseEntity.ok("Xóa thành công!");
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/batch")
    public ResponseEntity<Void> updateCharacters(@RequestBody List<CharacterSkill> characters) {
        characterSkillService.updateListCharacterSkill(characters);
        return ResponseEntity.ok().build();
    }
}
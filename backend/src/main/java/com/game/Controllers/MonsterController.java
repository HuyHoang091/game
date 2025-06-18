package com.game.Controllers;

import com.game.Model.Monster;
import com.game.Service.MonsterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/monster")
public class MonsterController {

    @Autowired
    private MonsterService characterService;

    @GetMapping("/")
    public ResponseEntity<List<Monster>> getAllMonster() {
        List<Monster> characters = characterService.getAllMonster();
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/")
    public ResponseEntity<Monster> createMonster(@RequestBody Monster Monster) {
        Monster create = characterService.createMonster(Monster);
        if (create != null) {
            return ResponseEntity.ok(create);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Monster> updateMonster(@PathVariable Long id, @RequestBody Monster Monster) {
        Monster update = characterService.updateMonster(id, Monster);
        if (update != null) {
            return ResponseEntity.ok(update);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Monster> deleteMonster(@PathVariable Long id) {
        boolean delete = characterService.deleteMonster(id);
        if (delete) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
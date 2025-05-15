package com.game.controller;

import com.game.entity.Monster;
import com.game.service.MonsterService;
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
}
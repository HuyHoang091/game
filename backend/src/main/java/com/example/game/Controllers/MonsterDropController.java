package com.game.controller;

import com.game.entity.MonsterDrop;
import com.game.service.MonsterDropService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/monsterdrop")
public class MonsterDropController {

    @Autowired
    private MonsterDropService characterService;

    @GetMapping("/")
    public ResponseEntity<List<MonsterDrop>> getAllMonsterDrop() {
        List<MonsterDrop> characters = characterService.getAllMonsterDrop();
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }
}
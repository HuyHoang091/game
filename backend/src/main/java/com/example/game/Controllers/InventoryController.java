package com.game.controller;

import com.game.entity.Inventory;
import com.game.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService characterService;

    @GetMapping("/{character_id}")
    public ResponseEntity<List<Inventory>> getAllCharactersSkill(@PathVariable Long character_id) {
        List<Inventory> characters = characterService.getAllCharactersSkill(character_id);
        if (characters != null && !characters.isEmpty()) {
            return ResponseEntity.ok(characters);
        }
        return ResponseEntity.notFound().build();
    }
}